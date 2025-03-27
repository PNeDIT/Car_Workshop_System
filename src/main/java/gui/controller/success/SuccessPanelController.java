package gui.controller.success;

import java.util.List;

import gui.controller.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, UNLESS YOU WANT TO IMPLEMENT
 * ADVANCED OPERATIONS! <br>
 * <br>
 * Controller for handling layouts showing the success of operations of the user
 * like a successful log in or reservation of tickets.
 */
public class SuccessPanelController extends Controller {
	// class attributes
	private String nextScene;

	// FXML components
	@FXML
	private Label headingLabel;
	@FXML
	private Label informationTextLabel;
	@FXML
	private ButtonBar buttonBar;
	@FXML
	private Button actionButton;

	/**
	 * Sets the text of the heading and information box from provided data and
	 * handles event for showing button bar.
	 */
	@Override
	public <T> void initializeSceneData(List<T> data) {
		headingLabel.setText((String) data.get(0));
		informationTextLabel.setText((String) data.get(1));
		informationTextLabel.setWrapText(true);

		buttonBar.setVisible(true);
		if (sceneNavigator.getPreviousScene() != null) {
			actionButton.setText((String) data.get(2));
		} else {
			// only if more data is given, activate button bar
			if (data.size() > 2) {
				actionButton.setText((String) data.get(2));
				nextScene = (String) data.get(3);
			} else {
				buttonBar.setVisible(false);
			}
		}
	}

	/**
	 * Loads the new scene (specified in <code>nextScene</code>) to the MainWindow
	 * when the button was clicked.
	 */
	@FXML
	public void onActionButtonClicked() {
		if (sceneNavigator.getPreviousScene() != null) {
			sceneNavigator.loadSceneToMainWindow(sceneNavigator.getPreviousScene(),
					sceneNavigator.getDataPreviousScene());

			sceneNavigator.setPreviousScene(null);
			sceneNavigator.setDataPreviousScene(null);
		} else {
			sceneNavigator.loadSceneToMainWindow(nextScene, null);
		}
	}
}
