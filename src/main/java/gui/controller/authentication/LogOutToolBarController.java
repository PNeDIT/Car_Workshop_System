package gui.controller.authentication;

import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Controls the tool bar when a user is logged in.
 */
public class LogOutToolBarController extends Controller {

    // FXML components (linked to UI elements in the scene)
    @FXML
    private Button profileButton;
    @FXML
    private Button logOutButton;

    /**
     * Triggered when the user clicks the profile button. <br>
     * <br>
     * Loads the profile scene to the GUI.
     */
    @FXML
    public void onProfileButtonClicked() {
        sceneNavigator.loadSceneToMainWindow(sceneNavigator.PROFILE, null);
    }

    /**
     * Triggered when the user clicks the log out button. <br>
     * <br>
     * Logs out the user by resetting the user object, switching the toolbar,
     * and displaying a logout confirmation message.
     */
    @FXML
    public void onLogOutButtonClicked() {
        restClient.setUser(null); // Clear the current user session
        sceneNavigator.loadToolBar(sceneNavigator.LOG_IN_BAR); // Switch toolbar to login state

        // Prepare logout confirmation message
        List<String> data = new ArrayList<>();
        data.add("Goodbye!");
        data.add("You are now successfully logged out.");

        // Load the success panel displaying logout confirmation
        sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, data);
    }
}
