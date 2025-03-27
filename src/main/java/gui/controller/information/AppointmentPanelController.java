package gui.controller.information;

import gui.controller.Controller;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import kong.unirest.UnirestException;
import utils.Utils;
import com.google.gson.JsonObject;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;
import java.time.ZoneId;

import java.text.ParseException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Controller for managing reservations of workshop services.
 */
public class AppointmentPanelController extends Controller {

    // ------------------ class attributes ------------------
    // only used, if previous scene was profile scene
    private int appointment_id;
    private Date oldScheduledTime;

    // variables for storing current ids
    private int workshop_id;
    private int service_id;
    private int technician_id;

    // ------------------ FXML components ------------------
    @FXML
    private Label appointmentLabel;
    @FXML
    private Label workshopLabel;
    @FXML
    private Label serviceLabel;
    @FXML
    private Label selectedTechnicianLabel;
    @FXML
    private Label scheduledTimeLabel;
    @FXML
    private Label paymentMethodLabel;
    @FXML
    private TableView<String> appointmentsTableView;
    @FXML
    private TableColumn<String, String> column;

    @FXML
    private Button reserveButton;
    @FXML
    private Label noModificationLabel;

    @FXML
    private HBox noAccountBox;
    @FXML
    private Button createAccountButton;
    @FXML
    private Button logInButton;

    @FXML
    private DatePicker datePicker;
    // ------------------------------------------------------

    /**
     * Initializes the scene with data from the previous scene and configures UI components.
     *
     * @param data List containing the necessary reservation data.
     */
    @Override
    public <T> void initializeSceneData(List<T> data) {
        appointmentsTableView.setVisible(true);
        appointmentsTableView.setManaged(true);

        // Initialize reserve button
        reserveButton.setDisable(false);

        if (restClient.getUser() == null) {
            noAccountBox.setVisible(true);
            reserveButton.setDisable(true);
        } else {
            noAccountBox.setVisible(false);
            reserveButton.setDisable(false);
        }

        workshopLabel.setText(String.valueOf(data.get(4)));
        serviceLabel.setText(String.valueOf(data.get(5)));
        selectedTechnicianLabel.setText(String.valueOf(data.get(6)));
        scheduledTimeLabel.setText(String.valueOf(data.get(7)));
// Define valid payment methods
        String[] validMethods = {"Cash", "cash", "Credit Card", "Credit card", "credit card",
                "Debit Card", "Debit card", "debit card", "PayPal", "paypal",
                "ApplePay", "applepay", "Not selected"};

// Ensure `data` is not null and process safely
        Optional<String> foundPaymentMethod = data.stream()
                .filter(Objects::nonNull) // Ignore null elements
                .map(Object::toString) // Convert all elements to String
                .filter(item -> Arrays.asList(validMethods).contains(item)) // Check if it's a valid method
                .findFirst();

// Set the payment method label, defaulting to "Not selected" if not found
        paymentMethodLabel.setText(foundPaymentMethod.orElse("Not selected"));

        workshop_id = (data.get(1) instanceof Integer) ? (Integer) data.get(1) : Integer.parseInt(data.get(1).toString());
        service_id = (data.get(2) instanceof Integer) ? (Integer) data.get(2) : Integer.parseInt(data.get(2).toString());
        technician_id = (data.get(3) instanceof Integer) ? (Integer) data.get(3) : Integer.parseInt(data.get(3).toString());

        if (sceneNavigator.getPreviousScene() != null
                && sceneNavigator.getPreviousScene().equals(sceneNavigator.PROFILE)) {
            appointment_id = Integer.parseInt((String) data.get(0));
            try {
                if (data.get(8) != null) { // Only parse if oldScheduledTime is not null
                    oldScheduledTime = Utils.yearMonthDayTimeFormat.parse((String) data.get(8));
                }
            } catch (ParseException e) {
                System.err.println("Invalid old scheduled time format: " + data.get(8));
            }
            appointmentLabel.setText("Modify Service Appointment");
            reserveButton.setText("Update Appointment");
        } else {
            // New appointment case
            appointment_id = -1;
            oldScheduledTime = null; // Ensure it stays null for new appointments
        }

        // Set up the table column
        column = new TableColumn<>("Available Appointments:");
        column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
        column.setPrefWidth(800); // Make the column wider

        appointmentsTableView.getColumns().clear();
        appointmentsTableView.getColumns().add(column);

        // Add selection listener to enable/disable reserve button
        appointmentsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            reserveButton.setDisable(newSelection == null);
        });

        // Initialize date picker with current date
        datePicker.setValue(LocalDate.now());

        // Add listener for date changes
        datePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Convert LocalDate to Date at start of day
                Date selectedDate = Date.from(newValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
                loadAvailableAppointments(selectedDate, workshop_id, service_id);
            }
        });

        loadAvailableAppointments(oldScheduledTime != null ? oldScheduledTime : new Date(), workshop_id, service_id);
    }

    /**
     * Loads and displays available appointment slots for the given date, workshop, and service.
     *
     * @param scheduledTime The requested date and time for checking available appointments.
     * @param workshop_id   The ID of the workshop where the service is offered.
     * @param service_id    The ID of the service to check availability for.
     */
    @FXML
    private void loadAvailableAppointments(Date scheduledTime, int workshop_id, int service_id) {
        try {
            List<String> availableSlots = restClient.getAvailableAppointments(scheduledTime, workshop_id, service_id);

            // Convert the slots to the selected date
            LocalDate selectedDate = datePicker.getValue();
            List<String> adjustedSlots = new ArrayList<>();

            for (String slot : availableSlots) {
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

            Platform.runLater(() -> {
                appointmentsTableView.getItems().clear();
                appointmentsTableView.getItems().addAll(adjustedSlots);
                appointmentsTableView.refresh();
            });
        } catch (UnirestException e) {
            System.err.println("Failed to load available appointments: " + e.getMessage());
        }
    }

    /**
     * Handles the reservation or modification of slots when the reserve button is clicked.
     */
    @FXML
    public void onReserveButtonClicked() {
        String slotSelected = appointmentsTableView.getSelectionModel().getSelectedItem();

        if (slotSelected == null) {
            noModificationLabel.setText("No appointment slot selected!");
            return;
        }
        List<String> controllerData = new ArrayList<>();

        // Convert oldScheduledTime (Date) to String using the same format as slotSelected
        String oldScheduledTimeString = (oldScheduledTime != null) ? Utils.yearMonthDayTimeFormat.format(oldScheduledTime) : "";
        if (sceneNavigator.getPreviousScene() != null
                && sceneNavigator.getPreviousScene().equals(sceneNavigator.PROFILE)) {
            if (slotSelected.equals(oldScheduledTimeString)) {
                noModificationLabel.setText("No changes were made.");
            } else {
                try {
                    Date slotSelectedAsDate = Utils.yearMonthDayTimeFormat.parse(slotSelected);
                    String formattedSlot = Utils.yearMonthDayTimeFormat.format(slotSelectedAsDate);
                    Date parsedBackDate = Utils.yearMonthDayTimeFormat.parse(formattedSlot);

                    boolean success = restClient.modifyAppointment(appointment_id, parsedBackDate, paymentMethodLabel.getText());
                    if (success) {
                        controllerData.add("Appointment Updated Successfully");
                        controllerData.add("Your service reservation was successfully updated.");
                        controllerData.add("Continue");
                        controllerData.add(sceneNavigator.PROFILE); // Add target scene for redirection
                        sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
                    } else {
                        noModificationLabel.setText("Failed to update appointment. Please try again.");
                    }
                } catch (ParseException e) {
                    System.err.println("Invalid date format: " + slotSelected);
                    noModificationLabel.setText("Invalid date format. Please try again.");
                } catch (UnirestException e) {
                    System.err.println("Failed to modify appointment: " + e.getMessage());
                    noModificationLabel.setText("Failed to update appointment. Please try again.");
                }
            }
        } else {
            try {
                Date slotSelectedAsDate = Utils.yearMonthDayTimeFormat.parse(slotSelected);

                // Create a date 2 hours before the selected slot for testing
                Calendar cal = Calendar.getInstance();
                cal.setTime(slotSelectedAsDate);
                cal.add(Calendar.HOUR_OF_DAY, -2);  // subtract 2 hours
                Date comparisonDate = cal.getTime();

                // Check if the selected time is in the past using our test comparison date
                if (slotSelectedAsDate.before(comparisonDate)) {
                    noModificationLabel.setText("Cannot create appointment in the past!");
                    return;
                }

                // Hardcode payment method for testing
                String paymentMethod = "Cash";  // Valid payment method

                boolean success = restClient.createNewAppointment(workshop_id, service_id, slotSelectedAsDate, paymentMethod);

                if (success) {
                    controllerData.add("Appointment Successful");
                    controllerData.add("Your service reservation has been successfully created.");
                    controllerData.add("Continue");
                    controllerData.add(sceneNavigator.PROFILE);

                    // Refresh the user's loyalty tokens from the server
                    List<JsonObject> userInfo = restClient.getUserInfoByMail(restClient.getUser().getEmail());
                    if (userInfo != null && !userInfo.isEmpty()) {
                        int updatedTokens = userInfo.get(0).get("loyalty_tokens").getAsInt();
                        restClient.getUser().setLoyaltyTokens(updatedTokens);
                    }

                    sceneNavigator.loadSceneToMainWindow(sceneNavigator.SUCCESS_PANEL, controllerData);
                } else {
                    noModificationLabel.setText("Failed to create appointment. Please try again.");
                }
            } catch (ParseException e) {
                System.err.println("Invalid date format: " + slotSelected);
                e.printStackTrace();
                noModificationLabel.setText("Invalid date format. Please try again.");
            } catch (UnirestException e) {
                System.err.println("Failed to create appointment: " + e.getMessage());
                e.printStackTrace();
                noModificationLabel.setText("Failed to create appointment. Please try again.");
            } catch (Exception e) {
                System.err.println("Unexpected error during appointment creation: " + e.getMessage());
                e.printStackTrace();
                noModificationLabel.setText("An unexpected error occurred. Please try again.");
            }
        }
    }

    /**
     * Sets the appointment scene (the current scene) as the previous scene in the
     * SceneNavigator and also sets the currently selected data (workshop, service,
     * technician, scheduledTime, paymentMethod) as the date of the previous scene. This way, the user
     * will return to the appointment scene (after log in/sign up) and see the same
     * data as before.
     */
    public void setPreviousSceneInformation() {
        sceneNavigator.setPreviousScene(sceneNavigator.APPOINTMENT);

        String oldScheduledTimeString = (oldScheduledTime != null) ? Utils.yearMonthDayTimeFormat.format(oldScheduledTime) : "";

        String[] data = {
                appointment_id + "", workshop_id + "", service_id + "", technician_id + "", workshopLabel.getText(), serviceLabel.getText(), selectedTechnicianLabel.getText(), scheduledTimeLabel.getText(),
                oldScheduledTimeString, paymentMethodLabel.getText()
        };
        sceneNavigator.setDataPreviousScene(Arrays.asList(data));
    }

    /**
     * Cancels the reservation process and returns to the previous scene.
     */
    @FXML
    public void onCancelButtonClicked() {
        if (sceneNavigator.getPreviousScene() != null
                && sceneNavigator.getPreviousScene().equals(sceneNavigator.PROFILE)) {
            sceneNavigator.setPreviousScene(null);
            sceneNavigator.setDataPreviousScene(null);
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.PROFILE, null);
        } else {
            sceneNavigator.loadSceneToMainWindow(sceneNavigator.INFO, Collections.singletonList(workshop_id));
        }
    }

    /**
     * Handles the creation of a new account and returns to the reservation scene upon completion.
     */
    @FXML
    public void onCreateAccountButtonClicked() {
        setPreviousSceneInformation();
        sceneNavigator.loadCompleteWindow(sceneNavigator.SIGN_UP_DIALOG,
                (Stage) createAccountButton.getScene().getWindow());
    }

    /**
     * Handles the login process and returns to the reservation scene upon successful login.
     */
    @FXML
    public void onLogInButtonClicked() {
        setPreviousSceneInformation();
        sceneNavigator.loadCompleteWindow(sceneNavigator.LOG_IN_DIALOG, (Stage) logInButton.getScene().getWindow());
    }
}
//finished