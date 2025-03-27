package gui;

import java.io.IOException;
import java.util.List;

import gui.controller.Controller;
import gui.controller.information.MainWindowController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * DO NOT CHANGE THE CONTENT OF THE CLASS, EXCEPT YOU WANT TO ADD ADDITIONAL
 * LAYOUTS IN THE LAYOUT PATHS SECTION! <br>
 * <br>
 * Handles the layouts and allows to substitute one part of the GUI by another
 * layout or loads a layout to the complete GUI window.
 */
public class SceneNavigator {

	// --------------------------- LAYOUT PATHS ---------------------------
	public final String BASE_PATH = "/layouts/";

	public final String MAIN_WINDOW = BASE_PATH + "information/MainWindow.fxml";
	public final String INFO = BASE_PATH + "information/WorkshopInformationPanel.fxml";
	public final String APPOINTMENT = BASE_PATH + "information/AppointmentPanel.fxml";
	public final String PROFILE = BASE_PATH + "information/ProfilePanel.fxml";

	public final String LOG_IN_DIALOG = BASE_PATH + "authentication/LogInDialog.fxml";
	public final String SIGN_UP_DIALOG = BASE_PATH + "authentication/SignUpDialog.fxml";
	public final String LOG_IN_BAR = BASE_PATH + "authentication/LogInToolBar.fxml";
	public final String LOG_OUT_BAR = BASE_PATH + "authentication/LogOutToolBar.fxml";

	public final String SUCCESS_PANEL = BASE_PATH + "success/SuccessPanel.fxml";
	public final String STYLE = "/style.css";

	// --------------------------------------------------------------------

	// controller and scene info
	private MainWindowController mainWindowController;
	private String previousScene;
	private List<String> dataPreviousScene;

	// window size
	private double currentWidth = 1125;
	private double currentHeigth = 750;

	private static SceneNavigator instance;

	/**
	 * Private constructor, so that no objects can be created from the outside.
	 */
	private SceneNavigator() {

	}

	public static SceneNavigator getSceneNavigator() {
		if (instance == null) {
			instance = new SceneNavigator();
		}
		return instance;
	}

	/**
	 * Loads the fxml file that contains a tool bar (specifically, either LOG_IN_BAR
	 * or LOG_OUT_BAR) to the top of MAIN_WINDOW .
	 *
	 * @param fxml the layout file
	 */
	public void loadToolBar(String fxml) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxml));
			mainWindowController.setToolBar(loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the layout to the information panel (located at the right) of
	 * MAIN_WINDOW. The data is passed to the corresponding controller of the
	 * layout. In the controller the data is then used to initialize labels, buttons
	 * and other components that are present in the layout.
	 *
	 * @param <T>  the data type the list has
	 * @param fxml the layout file
	 * @param data the data that is passed
	 */
	public <T> void loadSceneToMainWindow(String fxml, List<T> data) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxml));
			mainWindowController.setPanelScene(loader.load());

			// if data not null, get respective controller and initialize with data
			if (data != null) {
				Controller controller = loader.getController();
				controller.initializeSceneData(data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the layout to the complete stage, this means not only a part of
	 * MAIN_WINDOW is replaced but MAIN_WINDOW is completely replaced by either
	 * LOG_IN_DIALOG or SIGN_UP_DIALOG or the other way around.
	 *
	 * @param fxml  the layout file
	 * @param stage the stage which is modified
	 */
	public void loadCompleteWindow(String fxml, Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxml));
			Parent parent = loader.load();
			Scene scene = new Scene(parent, currentWidth, currentHeigth);
			scene.getStylesheets().add(SceneNavigator.class.getResource(STYLE).toExternalForm());

			// for same window size after complete Scene change, after window was resized
			scene.widthProperty().addListener(new ChangeListener<>() {
				@Override
				public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth,
									Number newSceneWidth) {
					currentWidth = newSceneWidth.doubleValue();
				}
			});
			scene.heightProperty().addListener(new ChangeListener<>() {
				@Override
				public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight,
									Number newSceneHeight) {
					currentHeigth = newSceneHeight.doubleValue();
				}
			});

			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens up a new popup window on top of the main window
	 *
	 * @param fxml  the layout file
	 * @param title the title of the window
	 * @param data the data that is passed
	 */
	public <T> void openPopupWindow(String fxml, String title, List<T> data) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
			Parent parent = loader.load();
			Scene scene = new Scene(parent);

			Stage stage = new Stage();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setScene(scene);
			stage.setTitle(title);
			stage.show();

			Controller controller = loader.getController();
			controller.initializeSceneData(data);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// GETTER AND SETTER

	public void setMainWindowController(MainWindowController mainWindowController) {
		this.mainWindowController = mainWindowController;
	}

	public double getCurrentWidth() {
		return currentWidth;
	}

	public void setCurrentWidth(double currentWidth) {
		this.currentWidth = currentWidth;
	}

	public double getCurrentHeigth() {
		return currentHeigth;
	}

	public void setCurrentHeigth(double currentHeigth) {
		this.currentHeigth = currentHeigth;
	}

	public String getPreviousScene() {
		return previousScene;
	}

	public void setPreviousScene(String previousScene) {
		this.previousScene = previousScene;
	}

	public List<String> getDataPreviousScene() {
		return dataPreviousScene;
	}

	public void setDataPreviousScene(List<String> dataPreviousScene) {
		this.dataPreviousScene = dataPreviousScene;
	}

}
