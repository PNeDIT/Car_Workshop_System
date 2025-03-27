package gui.controller.authentication;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS OR WORK ON THE EXTRA REQUIREMENTS! <br>
 * <br>
 * Handles the process of logging in.
 */
public class LogInDialogController extends Controller {

    // FXML components
    @FXML
    private TextField emailTextfield;
    @FXML
    private TextField passwordTextfield;
    @FXML
    private Label errorMessageLabel;

    /**
     * Is called when the user clicks the log in button.<br>
     * <br>
     * Retrieves information from textfields and checks if the user is allowed to
     * log in. If the user is allowed to, the scene is changed and shows the user a
     * message for the successful log in. If the user is not allowed to log in an
     * error message is displayed.
     */
    @FXML
    public void onLoginButtonClicked() {
        String email = emailTextfield.getText();
        String password = passwordTextfield.getText();
        // check if given user entered email and password
        if (email.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("All text fields must be filled out!");
        } else if (!restClient.validClientCredentials(email, password)) {
            // check if given user credentials are correct
            errorMessageLabel.setText("Invalid user data.");
        } else {
            // create user and pass to RestClient (split up necessary because of
            // authorization)
            User user = new User(email, password);
            restClient.setUser(user);
            List<JsonObject> clientInfo = restClient.getUserInfoByMail(email);
            user.setIdAndName(clientInfo.get(0).get("id").getAsInt(), clientInfo.get(0).get("firstName").getAsString(),
                    clientInfo.get(0).get("lastName").getAsString());

            // load main window and set panel to successful log in panel
            sceneNavigator.loadCompleteWindow(sceneNavigator.MAIN_WINDOW,
                    (Stage) emailTextfield.getScene().getWindow());

            List<String> controllerData = new ArrayList<>();
            controllerData.add("Welcome!");
            controllerData.add("You are now successfully logged in.");
            if (sceneNavigator.getPreviousScene() != null) {
                controllerData.add("To reservation");
            }
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
            sceneNavigator.loadToolBar(sceneNavigator.LOG_OUT_BAR);
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
        sceneNavigator.loadCompleteWindow(sceneNavigator.MAIN_WINDOW, (Stage) emailTextfield.getScene().getWindow());
        sceneNavigator.loadToolBar(sceneNavigator.LOG_IN_BAR);

        if (sceneNavigator.getPreviousScene() != null) {
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.getPreviousScene(),
                    sceneNavigator.getDataPreviousScene());

            sceneNavigator.setPreviousScene(null);
            sceneNavigator.setDataPreviousScene(null);
        }
    }

}
