package gui.controller.information;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class MainWindowController extends Controller implements Initializable {

    // Stores workshop IDs for tracking selected workshops
    private List<Integer> workshopIds = new ArrayList<>();
    private int lastClickedWorkshopIndex = -1; // Prevents reloading the same workshop repeatedly

    // FXML components
    @FXML
    private ListView<String> workshopListView; // List of workshops
    @FXML
    private AnchorPane informationPanel; // Panel for displaying workshop details
    @FXML
    private AnchorPane toolBar; // Toolbar panel

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneNavigator.setMainWindowController(this);

        // Retrieve all workshops from the REST client and initialize the ListView
        List<JsonObject> workshopList = restClient.requestAllWorkshops(); // Fetch workshops
        initializeWorkshopListView(workshopList);
    }

    /**
     * Initializes the ListView with workshops.
     * If no workshops are available, displays a message instead.
     *
     * @param workshopList the list of workshops as JSON objects
     */
    public void initializeWorkshopListView(List<JsonObject> workshopList) {
        // Reset variables
        workshopIds = new ArrayList<>();
        workshopListView.getSelectionModel().clearSelection();
        lastClickedWorkshopIndex = -1;

        if (workshopList.isEmpty()) {
            // Display message if no workshops are available
            List<String> data = new ArrayList<>();
            data.add("No Workshops");
            data.add("Currently, there are no workshops available. Please try again later.");
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, data);

            workshopListView.setItems(FXCollections.observableArrayList()); // Empty ListView
        } else {
            // Populate ListView with workshop names
            ObservableList<String> obsList = FXCollections.observableArrayList();
            for (JsonObject workshop : workshopList) {
                obsList.add(workshop.get("name").getAsString()); // Extract workshop name
                workshopIds.add(workshop.get("id").getAsInt()); // Store corresponding ID
            }
            workshopListView.setItems(obsList);
        }
    }

    /**
     * Handles the selection of a workshop from the list.
     * Loads the information panel with details about the selected workshop.
     */
    @FXML
    public void onWorkshopListItemClicked() {
        int clickedIndex = workshopListView.getSelectionModel().getSelectedIndex();

        // Ensure a valid selection and avoid unnecessary reloads
        if (clickedIndex != -1 && clickedIndex != lastClickedWorkshopIndex) {
            lastClickedWorkshopIndex = clickedIndex;
            int workshop_id = workshopIds.get(clickedIndex);

            // Load the selected workshop's details into the main window
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.INFO, Collections.singletonList(workshop_id));
        }
    }

    /**
     * Sets the given node (FXML layout) to the information panel (right side of the window).
     *
     * @param node the loaded FXML layout
     */
    public void setPanelScene(Node node) {
        informationPanel.getChildren().setAll(node);
        setAllAnchors(node, 0);
    }

    /**
     * Sets the given node (FXML layout) to the toolbar panel (top of the window).
     *
     * @param node the loaded FXML layout
     */
    public void setToolBar(Node node) {
        toolBar.getChildren().setAll(node);
        setAllAnchors(node, 0);
    }

    /**
     * Sets all anchor constraints of the given node to the specified value.
     * This ensures the node is properly stretched within its container.
     *
     * @param node  the node to modify
     * @param value the margin offset from all sides (top, bottom, left, right)
     */
    private void setAllAnchors(Node node, double value) {
        AnchorPane.setBottomAnchor(node, value);
        AnchorPane.setTopAnchor(node, value);
        AnchorPane.setLeftAnchor(node, value);
        AnchorPane.setRightAnchor(node, value);
    }
}
