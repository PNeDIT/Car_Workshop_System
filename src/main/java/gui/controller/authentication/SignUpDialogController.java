package gui.controller.authentication;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;
import utils.Utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Handles the process of signing up.
 */
public class SignUpDialogController extends Controller implements Initializable {

    // FXML components
    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField passwordRepeatTextField;
    @FXML
    private ComboBox<String> securityQuestionComboBox; // Displays security questions as text
    @FXML
    private TextField securityAnswerTextField;
    @FXML
    private TextField vehicleRegistrationNumberTextField;
    @FXML
    private TextField phoneNumberTextField;
    @FXML
    private Label errorMessageLabel;

    // Maps to store security question texts and their corresponding IDs
    private Map<String, Integer> securityQuestionMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSecurityQuestions();
    }

    /**
     * Fetch and load available security questions from the REST client into the ComboBox.
     */
    private void loadSecurityQuestions() {
        
        // Clear existing items
        securityQuestionComboBox.getItems().clear();
        securityQuestionMap.clear();

        // Fetch available security questions from the REST client
        List<JsonObject> questions = restClient.getSecurityQuestions();

        // Populate ComboBox with the question texts and map them to their IDs
        for (JsonObject questionObj : questions) {
            try {
                // Get the ID and question text
                int id = questionObj.get("security_question_id").getAsInt();
                String question = questionObj.get("security_question").getAsString();

                // Map question text to its ID
                securityQuestionMap.put(question, id);

                // Add question to ComboBox
                securityQuestionComboBox.getItems().add(question);
            } catch (Exception e) {
                System.err.println("Error processing question: " + questionObj);
                e.printStackTrace();
            }
        }

        // Set default value if questions are available
        if (!securityQuestionComboBox.getItems().isEmpty()) {
            securityQuestionComboBox.setValue(securityQuestionComboBox.getItems().get(0));
            System.out.println("Set default question: " + securityQuestionComboBox.getValue());
        } else {
            System.out.println("No security questions available to display");
        }

        // Add listener for selection changes
        securityQuestionComboBox.setOnAction(event -> {
            String selectedQuestion = securityQuestionComboBox.getValue();
            if (selectedQuestion != null && securityQuestionMap.containsKey(selectedQuestion)) {
                int selectedQuestionId = securityQuestionMap.get(selectedQuestion);
            }
        });
    }

    /**
     * Is called when the user clicks the sign up button. <br>
     * <br>
     * Retrieves information from all textfields and checks if all requirements for
     * the input are met. This also includes the attempt to create the user, as it
     * might be possible that the given email is already taken. If the new user
     * could be created, the scene is change to the MainWindow and a message is
     * displayed that the sign up was successful.
     */
    @FXML
    public void onSignupButtonClicked() {
        errorMessageLabel.setText("");
        // extract entered information
        String firstname = firstNameTextField.getText();
        String lastname = lastNameTextField.getText();
        String email = emailTextField.getText();
        String password = passwordTextField.getText();
        String passwordRepeat = passwordRepeatTextField.getText();
        String securityQuestion = securityQuestionComboBox.getValue();
        String securityAnswer = securityAnswerTextField.getText();
        String vehicleRegistrationNumber = vehicleRegistrationNumberTextField.getText();
        String phoneNumber = phoneNumberTextField.getText();


        // Error handling
        if (firstname.isEmpty() || lastname.isEmpty() || email.isEmpty() || password.isEmpty()
                || passwordRepeat.isEmpty() || securityQuestion == null || securityAnswer.isEmpty()) {
            errorMessageLabel.setText("All text fields must be filled out!");
        } else if (!Utils.isAlpha(firstname) || !Utils.isAlpha(lastname)) {
            errorMessageLabel.setText("First and last name are not allowed to contain numbers.");
        } else if (!Utils.isValidEmailAddress(email)) {
            errorMessageLabel.setText("No valid e-mail address.");
        } else if (!Utils.isValidPassword(password)) {
            errorMessageLabel.setText("The password must contain at least 8 characters.");
        } else if (!password.equals(passwordRepeat)) {
            errorMessageLabel.setText("The passwords do not correspond to each other.");
        } else if (!Utils.isValidVehicleRegistrationNumber(vehicleRegistrationNumber)) {
            errorMessageLabel.setText("Vehicle registration number does not match any EU registration pattern.");
        } else if (!Utils.isValidPhoneNumber(phoneNumber)) {
            errorMessageLabel.setText("Invalid phone number, must be 10 digits.");
        } else {
            // Retrieve the security question ID from the map
            int securityQuestionId = securityQuestionMap.get(securityQuestion);

            // Create the user
            if (!restClient.createNewUser(firstname, lastname, email, password, securityQuestionId, securityAnswer, vehicleRegistrationNumber, phoneNumber)) {
                errorMessageLabel.setText("E-mail address is already taken.");
            } else {
                // customer could be created successfully
                // create user and pass to RestClient (split up necessary because of
                // authorization)
                User user = new User(firstname, lastname, email, password);
                restClient.setUser(user);
                List<JsonObject> clientInfo = restClient.getUserInfoByMail(email);
                user.setId(clientInfo.get(0).get("id").getAsInt());

                // load main window and set panel to successful sign up panel
                sceneNavigator.loadCompleteWindow(sceneNavigator.MAIN_WINDOW,
                        (Stage) emailTextField.getScene().getWindow());

                List<String> controllerData = new ArrayList<>();
                controllerData.add("Welcome!");
                controllerData.add("You are now successfully registered.");
                if (sceneNavigator.getPreviousScene() != null) {
                    controllerData.add("To reservation");
                }
                sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
                sceneNavigator.loadToolBar(sceneNavigator.LOG_OUT_BAR);
            }
        }
    }

    /**
     * Is called when the user clicks the cancel button. <br>
     * <br>
     * Loads the MainWindow to the complete stage and the log in bar to the
     * MainWindow. If the previous scene is not <code>null</code>, this means the
     * user came from the reservation scene, the previous scene is loaded to the
     * MainWindow and afterwards reset to <code>null</code>. Otherwise, the previous
     * scene is <code>null</code> meaning the user came from the log in or sign up
     * scene.
     */
    @FXML
    public void onCancelButtonClicked() {
        sceneNavigator.loadCompleteWindow(sceneNavigator.MAIN_WINDOW, (Stage) emailTextField.getScene().getWindow());
        sceneNavigator.loadToolBar(sceneNavigator.LOG_IN_BAR);

        if (sceneNavigator.getPreviousScene() != null) {
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.getPreviousScene(),
                    sceneNavigator.getDataPreviousScene());

            sceneNavigator.setPreviousScene(null);
            sceneNavigator.setDataPreviousScene(null);
        }
    }
}
