package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, EXCEPT FOR THE TITLE OF THE GUI
 * WINDOW! <br>
 * <br>
 * Main class for starting the GUI window.
 */
public class RunGui extends Application {

	private final SceneNavigator sceneNavigator = SceneNavigator.getSceneNavigator();

	/**
	 * Creates the GUI window, sets the scene to the <code>MainWindow</code>, adds
	 * listeners for changing the width and height of the GUI window and sets
	 * properties of the GUI window.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(sceneNavigator.MAIN_WINDOW));
			Parent parent = loader.load();

			sceneNavigator.loadToolBar(sceneNavigator.LOG_IN_BAR);

			Scene scene = new Scene(parent, sceneNavigator.getCurrentWidth(), sceneNavigator.getCurrentHeigth());
			scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(sceneNavigator.STYLE)).toExternalForm());

			// for same window size after complete Scene change, after window was resized
			scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth)
					-> sceneNavigator.setCurrentWidth(newSceneWidth.doubleValue()));
			scene.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight)
					-> sceneNavigator.setCurrentHeigth(newSceneHeight.doubleValue()));

			// set features of stage
			primaryStage.setMinHeight(750);
			primaryStage.setMinWidth(1125);
			primaryStage.setTitle("Appointment System");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception ex) {
			Exception exception = ex;
			Logger logger = Logger.getLogger("RunGui");
			logger.log(Level.WARNING, exception.getMessage(), exception);
			while (exception.getCause() != null)
				exception = (Exception) exception.getCause();
			logger.warning(exception.getMessage());
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
