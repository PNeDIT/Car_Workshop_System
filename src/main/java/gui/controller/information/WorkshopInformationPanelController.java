package gui.controller.information;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import utils.Utils;

import java.text.ParseException;
import java.util.*;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Controller for displaying workshop information and managing service reservations.
 */
public class WorkshopInformationPanelController extends Controller {

    // ------------------ Class Attributes ------------------
    // variables for storing current ids
    private int workshop_id;
    private int service_id;
    private final List<Integer> service_ids = new ArrayList<>();

    // last clicked service
    private int lastClickedServiceIndex = -1;

    private String selectedAppointment;

    // store last clicked date and playtime index (corresponds to column and row
    // index)
    //private int lastClickedDateIndex = 0;
    //private int lastClickedTimeIndex = 0;
    // for storing all list views
    //private List<ListView<String>> timeListViews = new ArrayList<>();
    // ------------------------------------------------------

    // ------------------ FXML Components ------------------
    @FXML
    private Label workshopNameLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label contactInfoLabel;
    @FXML
    private ListView<String> serviceListView;

    @FXML
    private VBox serviceInformationPanel;
    @FXML
    private Label serviceNameLabel;
    @FXML
    private Label serviceDurationLabel;
    @FXML
    private Label servicePriceLabel;
    @FXML
    private Label serviceDescriptionLabel;

    @FXML
    private GridPane scheduleGridPane;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Button bookAppointmentButton;
    @FXML
    private Label messageLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> technicianComboBox;
    private Map<String, Integer> technicianMap = new HashMap<>();
    private int selectedTechnicianId = -1;
    // ------------------------------------------------------

    /**
     * Initializes the workshop information panel with data passed from the previous scene.
     */
    /**
     * Hides the <code>movieInformationPanel</code> because at the beginning no
     * movie is selected. Is shown later, when user selects a movie.<br>
     * Retrieves delivered data from previous scene and sets the text of all
     * labels.<br>
     * If currently the cinema has no movies to show, create a list with one dummy
     * JSON object containing a message which states that no movies are
     * available.<br>
     * At the end initializes the list view in which all movies are displayed.
     */
    @Override
    public <T> void initializeSceneData(List<T> data) {
        // at first hide service information panel
        serviceInformationPanel.setVisible(false);
        serviceInformationPanel.setMinHeight(0);
        serviceInformationPanel.setPrefHeight(0);

        setReservable(false);

        // Retrieve workshop ID passed from the previous scene
        workshop_id = (int) data.get(0);

        // Retrieve workshop and service data from the REST client
        List<JsonObject> workshopJson = restClient.requestWorkshopInformation(workshop_id);
        List<JsonObject> serviceJson = restClient.requestAllServicesOfWorkshop(workshop_id);

        // Update workshop details in the UI
        workshopNameLabel.setText(workshopJson.get(0).get("name").getAsString());
        locationLabel.setText(workshopJson.get(0).get("location").getAsString());
        contactInfoLabel.setText(workshopJson.get(0).get("contactInfo").getAsString());

        // Populate the service list view or display a "no services available" message
        if (serviceJson == null || serviceJson.isEmpty()) {
            ObservableList<String> obsList = createNoItemsAvailableList(serviceListView, "No services available.");
            serviceListView.setItems(obsList);
        } else {
            initializeServiceListView(serviceJson);
        }

        // Initialize date picker with current date and disable past dates
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });

        // Add listener for date changes
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && lastClickedServiceIndex != -1) {
                initializeScheduleView();
            }
        });
    }

    /**
     * Initializes the service list view with data retrieved from the REST client.
     */
    /**
     * Extracts all unique movie names from the list of movies. Determines the
     * corresponding ids for the movies and sets the items of
     * <code>movieListView</code> to the unique movies.
     *
     * @param serviceJson the list containing all services as <code>JsonObject</code>
     */
    private void initializeServiceListView(List<JsonObject> serviceJson) {
        List<String> uniqueServices = Utils.getUniqueItems(serviceJson, "name");
        for (String service : uniqueServices) {
            for (JsonObject json : serviceJson) {
                if (json.get("name").getAsString().equals(service)) {
                    service_ids.add(json.get("id").getAsInt());
                    break;
                }
            }
        }
        serviceListView.setItems(FXCollections.observableArrayList(uniqueServices));
    }

    /**
     * Handles selection of a service from the list and displays detailed information.
     */
    /**
     * Is called when the user clicks on an item in the
     * <code>movieListView</code>.<br>
     * <br>
     * Only if the user clicked not on an empty list item and the selected item is
     * different to the previously selected item, retrieves movie information and
     * initializes the <code>movieInformationPanel</code>, including the view for
     * showing the playtimes.
     */
    public void onServiceClicked() {
        int clickedIndex = serviceListView.getSelectionModel().getSelectedIndex();

        if (clickedIndex != -1 && clickedIndex != lastClickedServiceIndex) {
            lastClickedServiceIndex = clickedIndex;
            service_id = service_ids.get(clickedIndex);
            setReservable(false);

            // Load technicians before showing the panel
            loadTechniciansForService();
            
            // Retrieve service details and schedule
            String service = serviceListView.getSelectionModel().getSelectedItem();
            List<JsonObject> serviceDetailsJson = restClient.requestServiceInformationOfWorkshop(workshop_id, service_id);

            // Update UI with service details
            serviceNameLabel.setText(service);
            serviceDurationLabel.setText(serviceDetailsJson.get(0).get("duration").getAsString() + " mins");
            servicePriceLabel.setText("€" + serviceDetailsJson.get(0).get("price").getAsString());
            serviceDescriptionLabel.setText(serviceDetailsJson.get(0).get("description").isJsonNull() ? "No description available." : serviceDetailsJson.get(0).get("description").getAsString());

            // Initialize the schedule grid
            initializeScheduleView();

            serviceInformationPanel.setMinHeight(Control.USE_COMPUTED_SIZE);
            serviceInformationPanel.setPrefHeight(Control.USE_COMPUTED_SIZE);
            serviceInformationPanel.setVisible(true);

            Animation animation = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), new KeyValue(scrollPane.vvalueProperty(), 1)));
            animation.play();
        }
    }

    /**
     * Sets up the schedule grid with dates and times.
     */
    private void initializeScheduleView() {
        scheduleGridPane.getChildren().clear();
        scheduleGridPane.getColumnConstraints().clear();

        // Get selected date from date picker, or use current date if none selected
        LocalDate selectedDate = datePicker.getValue();
        Date queryDate = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<String> availableAppointments = restClient.getAvailableAppointments(queryDate, workshop_id, service_id);

        // Convert the slots to the selected date
        List<String> adjustedSlots = new ArrayList<>();
        for (String slot : availableAppointments) {
            try {
                // Parse the original time
                Date originalDate = Utils.yearMonthDayTimeFormat.parse(slot);
                Calendar cal = Calendar.getInstance();
                cal.setTime(originalDate);

                // Create new date with selected date but same time
                Calendar newCal = Calendar.getInstance();
                newCal.setTime(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                newCal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
                newCal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));

                // Format the new date
                String adjustedSlot = Utils.yearMonthDayTimeFormat.format(newCal.getTime());
                adjustedSlots.add(adjustedSlot);
            } catch (ParseException e) {
                System.err.println("Error adjusting date for slot: " + slot);
                e.printStackTrace();
            }
        }

        if (adjustedSlots.isEmpty()) {
            Label noAppointmentsLabel = new Label("No available appointments for selected date.");
            scheduleGridPane.add(noAppointmentsLabel, 0, 0);
        } else {
            int row = 0, col = 0;
            for (String appointment : adjustedSlots) {
                Button appointmentButton = new Button(appointment);
                appointmentButton.setOnAction(e -> onAppointmentSelected(appointment));
                scheduleGridPane.add(appointmentButton, col, row);
                col++;
                if (col > 2) {
                    col = 0;
                    row++;
                }
            }
        }
    }

    private void onAppointmentSelected(String dateTime) {
        selectedAppointment = dateTime; // Store selected appointment
        setReservable(true); // Enable the booking button
    }

    /**
     * Handles the reservation button click event.
     */
    /**
     * Is called when the reserve button is clicked.<br>
     * <br>
     * Retrieves all necessary information for the reservation scene: cinema, movie,
     * date, start time and cinema id, movie id, playtime id. Gathers information in
     * list and shows reservation scene with this information.
     */
    @FXML
    public void onBookAppointmentClicked() {
        if (selectedAppointment == null) {
            messageLabel.setText("Please select an appointment first.");
            return;
        }

        if (selectedTechnicianId == -1) {
            messageLabel.setText("Please select a technician.");
            return;
        }

        String workshopName = workshopNameLabel.getText();
        String serviceName = serviceListView.getSelectionModel().getSelectedItem();
        String technicianName = technicianComboBox.getValue();

        List<String> data = Arrays.asList(
                "-1", // No existing appointment ID (new booking)
                String.valueOf(workshop_id),
                String.valueOf(service_id),
                String.valueOf(selectedTechnicianId),
                workshopName,
                serviceName,
                technicianName,
                selectedAppointment,
                null, // oldScheduledTime
                "Not selected" // paymentMethod
        );

        sceneNavigator.loadSceneToMainWindow(sceneNavigator.APPOINTMENT, data);
    }

    /**
     * Enables or disables the reserve button based on the availability of the selection.
     */
    private void setReservable(boolean isReservable) {
        bookAppointmentButton.setDisable(!isReservable);
        messageLabel.setVisible(!isReservable);
    }

    /**
     * Is called if there are no items to be displayed in the given ListView. Sets a
     * cell factory to the ListView to change it appearance and creates a
     * <code>ObservableList</code> containing the message to display instead of
     * elements.
     *
     * @param <T>      the type of items in the ListView
     * @param listView the ListView on which to set properties
     * @param text     the text to show in the ListView
     * @return the observable list
     */
    private <T> ObservableList<String> createNoItemsAvailableList(ListView<T> listView, String text) {
        // disable that items are clickable
        listView.setMouseTransparent(true);
        listView.setFocusTraversable(false);

        // change appearance of cell: text is centered and height of item is equal to
        // height of list view
        listView.setCellFactory(lst -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                // Create the HBox
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                hBox.setPrefHeight(listView.getPrefHeight() - 10);

                // Create centered Label
                Label label = new Label((String) item);
                label.setStyle("-fx-font-style: italic;");
                label.setAlignment(Pos.CENTER);

                hBox.getChildren().add(label);
                setGraphic(hBox);
            }
        });
        ObservableList<String> message = FXCollections.observableArrayList();
        message.add(text);
        return message;
    }

    private void loadTechniciansForService() {
        // Clear existing items
        technicianComboBox.getItems().clear();
        technicianMap.clear();

        List<JsonObject> technicians = restClient.requestTechniciansForService(workshop_id);
        
        if (technicians != null && !technicians.isEmpty()) {
            for (JsonObject tech : technicians) {
                String techName = tech.get("name").getAsString();
                int techId = tech.get("id").getAsInt();
                String certifications = tech.get("certifications").getAsString();
                int experience = tech.get("experience").getAsInt();
                
                String displayText = techName + " - " + certifications + " (" + experience + " years)";
                technicianMap.put(displayText, techId);
                technicianComboBox.getItems().add(displayText);
            }
            
            // Set default selection
            technicianComboBox.setValue(technicianComboBox.getItems().get(0));
            selectedTechnicianId = technicianMap.get(technicianComboBox.getValue());
        }

        // Add listener for selection changes
        technicianComboBox.setOnAction(event -> {
            String selected = technicianComboBox.getValue();
            if (selected != null && technicianMap.containsKey(selected)) {
                selectedTechnicianId = technicianMap.get(selected);
            }
        });
    }
}
