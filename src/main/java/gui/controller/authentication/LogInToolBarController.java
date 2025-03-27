package gui.controller.authentication;

import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Controls the tool bar when no user is logged in.
 */
public class LogInToolBarController extends Controller implements Initializable {

    // FXML components (linked to UI elements in the scene)
    @FXML
    private Button logInButton;

    /**
     * Initializes the controller.
     * Currently, no specific initialization logic is required.
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        // No initialization required
    }

    /**
     * Triggered when the user clicks the log in button. <br>
     * <br>
     * Loads the log in scene to the GUI.
     */
    @FXML
    public void onLogInButtonClicked() {
        sceneNavigator.loadCompleteWindow(sceneNavigator.LOG_IN_DIALOG, (Stage) logInButton.getScene().getWindow());
    }

    /**
     * Triggered when the user clicks the sign up button. <br>
     * <br>
     * Loads the sign up scene to the GUI.
     */
    @FXML
    public void onSignUpButtonClicked() {
        sceneNavigator.loadCompleteWindow(sceneNavigator.SIGN_UP_DIALOG, (Stage) logInButton.getScene().getWindow());
    }
}
