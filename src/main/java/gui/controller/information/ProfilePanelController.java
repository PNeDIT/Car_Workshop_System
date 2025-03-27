package gui.controller.information;

import com.google.gson.JsonObject;
import gui.controller.Controller;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import model.Appointment;
import utils.Utils;

import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Controller for managing user profile and displaying service reservations.
 */
public class ProfilePanelController extends Controller implements Initializable {

    // ------------------ Class Attributes ------------------
    private final int tableRowHeight = 30; // Height of each table row
    private TableColumn<Appointment, Date> scheduledTimeColumn;
    // ------------------------------------------------------

    // ------------------ FXML Components ------------------
    @FXML
    private Label firstnameLabel;
    @FXML
    private Label lastnameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label loyaltyTokensLabel;

    @FXML
    private TableView<Appointment> appointmentsTableView;
    @FXML
    private GridPane buttonGridPane;
    // ------------------------------------------------------

    /**
     * Refreshes the loyalty tokens display with the current value from the user object
     */
    public void refreshLoyaltyTokens() {
        if (loyaltyTokensLabel != null && restClient.getUser() != null) {
            loyaltyTokensLabel.setText(String.valueOf(restClient.getUser().getLoyaltyTokens()));
        }
    }

    /**
     * Initializes the table view that shows all appointments of the user who is
     * currently logged in. Initializes the properties of the table columns at
     * first. Next, request all appointments of the user via the REST client. If
     * client has currently no appointments, shows a message. Otherwise, adds every
     * <code>Appointment</code> as one row to the table, sorts the appointment by
     * scheduledTime date and time and sets the height and style of each row.
     */
    public void initializeAppointmentTableView() {
        initializeTableColumns();

        // Retrieve appointments via REST client
        List<JsonObject> appointments = restClient.getAppointmentsForUser();

        // Handle no appointments scenario
        if (appointments == null || appointments.isEmpty()) {
            Label label = new Label("No appointments available at the moment.");
            label.setStyle("-fx-font-style: italic;");
            appointmentsTableView.setPlaceholder(label);
            appointmentsTableView.setPrefHeight(100);
        } else { // Populate the appointment table
            for (JsonObject appointmentJson : appointments) {
                addAppointmentToTable(appointmentJson);
            }
            appointmentsTableView.getSortOrder().add(scheduledTimeColumn);
            appointmentsTableView.sort();

            setTableRowAndHeaderHeight();
            styleRows();
        }
    }

    /**
     * Iterates over every table column, which were previously already created in
     * the fxml layout. For every column
     * {@link javafx.scene.control.TableColumn#setCellValueFactory(javafx.util.Callback)}
     * is called which connects the table column with an attribute from the
     * <code>Appointment</code> class.<br>
     * Adds further properties for specific columns:
     * <ul>
     * <li><b>date:</b> register column for default sorting; add
     * {@link javafx.scene.control.TableColumn#setCellFactory(javafx.util.Callback)}
     * to show date in correct format</li>
     * <li><b>time:</b> register column for default sorting</li>
     * <li><b>movie/cinema:</b> add
     * {@link javafx.scene.control.TableColumn#setCellFactory(javafx.util.Callback)}
     * to add tooltip showing the complete movie/cinema name</li>
     * </ul>
     */
    public void initializeTableColumns() {
        // get all table columns from fxml layout
        ObservableList<TableColumn<Appointment, ?>> columns = appointmentsTableView.getColumns();
        String[] variableNames = Appointment.getVariableNames();

        // Set table properties for horizontal scrolling
        appointmentsTableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // iterate over each column and set factory to variable in POJO
        for (int i = 0; i < columns.size(); i++) {
            TableColumn<Appointment, ?> column = columns.get(i);
            column.setCellValueFactory(new PropertyValueFactory<>(variableNames[i]));

            // Set minimum and preferred widths for columns
            switch (variableNames[i]) {
                case "workshop":
                case "service":
                    column.setMinWidth(150);
                    column.setPrefWidth(200);
                    break;
                case "technician":
                    column.setMinWidth(120);
                    column.setPrefWidth(150);
                    break;
                case "scheduledTime":
                case "createdAt":
                case "modifiedAt":
                    column.setMinWidth(140);
                    column.setPrefWidth(160);
                    break;
                case "appointmentStatus":
                case "paymentStatus":
                    column.setMinWidth(100);
                    column.setPrefWidth(120);
                    break;
                case "paymentMethod":
                    column.setMinWidth(120);
                    column.setPrefWidth(140);
                    break;
            }

            switch (variableNames[i]) {
                case "workshop":
                case "service":
                case "technician":
                case "appointmentStatus":
                case "paymentMethod":
                case "paymentStatus":
                    // show tooltip, as the text might not fit into the column width
                    TableColumn<Appointment, String> stringCol = (TableColumn<Appointment, String>) column;
                    stringCol.setCellFactory(tableColumn -> new TableCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(item);
                            setTooltip(new Tooltip(item));
                        }
                    });
                    break;
                case "scheduledTime":
                    scheduledTimeColumn = (TableColumn<Appointment, Date>) column;
                    scheduledTimeColumn.setSortType(TableColumn.SortType.ASCENDING);
                    // set another cell factory, so that scheduledTime is shown in correct format
                    scheduledTimeColumn.setCellFactory(tableColumn -> new TableCell<>() {
                        @Override
                        protected void updateItem(Date item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                try {
                                    setText(Utils.yearMonthDayTimeFormat.format(item));
                                } catch (Exception e) {
                                    System.err.println("Error formatting date: " + e.getMessage());
                                    setText("Invalid Date");
                                }
                            }
                        }
                    });
                    break;
                case "createdAt":
                case "modifiedAt":
                    // Handle date columns with proper date formatting
                    TableColumn<Appointment, Date> dateCol = (TableColumn<Appointment, Date>) column;
                    dateCol.setCellFactory(tableColumn -> new TableCell<>() {
                        @Override
                        protected void updateItem(Date item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                try {
                                    setText(Utils.yearMonthDayTimeFormat.format(item));
                                    setTooltip(new Tooltip(Utils.yearMonthDayTimeFormat.format(item)));
                                } catch (Exception e) {
                                    System.err.println("Error formatting date: " + e.getMessage());
                                    setText("Invalid Date");
                                }
                            }
                        }
                    });
                    break;
            }
        }
    }

    /**
     * Makes request to REST client to extract the appointment information with appointment id.
     * This way the Appointment id's: appointment_id, workshop_id, service_id and technician_id are extracted as well as the
     * Appointment: attributes workshop, service, technician, scheduledTime,
     * createdAt, modifiedAt, appointmentStatus, paymentMethod, paymentStatus.
     *     Creates new
     * <code>Appointment</code> object which is added to the table view. The mapping
     * of the attributes of <code>Appointment</code> to the columns was previously
     * defined.
     *
     * @param json the reservation in JSON format
     */
    private void addAppointmentToTable(JsonObject json) {
        // get appointment details for current appointment from server
        List<JsonObject> appointmentDetails = restClient.getAppointmentDetails(json.get("id").getAsInt());


        try {
            // extract various ids
            int workshop_id = appointmentDetails.get(0).get("workshop_id").getAsInt();
            int service_id = appointmentDetails.get(0).get("service_id").getAsInt();
            int technician_id = appointmentDetails.get(0).get("technician_id").getAsInt();

            // extract dates with null checks
            Date scheduledTime = null;
            Date createdAt = null;
            Date modifiedAt = null;

            try {
                if (appointmentDetails.get(0).has("scheduledTime")
                        && !appointmentDetails.get(0).get("scheduledTime").isJsonNull()) {

                    String scheduledTimeStr = appointmentDetails.get(0).get("scheduledTime").getAsString();
                    // Clean up any "?AM" / "?PM":
                    scheduledTimeStr = scheduledTimeStr
                            .replace("?AM", " AM")
                            .replace("?PM", " PM");

                    scheduledTimeStr = scheduledTimeStr.replaceAll("[^\\x20-\\x7E]", "")
                            .replaceAll("(?<=\\d)(AM|PM)", " $1");
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.ENGLISH);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    try {
                        // Parse the original string into a Date object
                        Date date = inputFormat.parse(scheduledTimeStr);
                        // Format it into the desired format
                        String formattedDate = outputFormat.format(date);
                        scheduledTime = Utils.parseStringToDate(formattedDate, Utils.yearMonthDayTimeFormat);
                    } catch (ParseException e) {
                        System.err.println("Error parsing scheduledTime: " + scheduledTimeStr);
                        e.printStackTrace();
                    }
                }

                if (appointmentDetails.get(0).has("createdAt") && !appointmentDetails.get(0).get("createdAt").isJsonNull()) {
                    String createdAtStr = appointmentDetails.get(0).get("createdAt").getAsString();
                    // Clean up any "?AM" / "?PM":
                    createdAtStr = createdAtStr
                            .replace("?AM", " AM")
                            .replace("?PM", " PM");

                    createdAtStr = createdAtStr.replaceAll("[^\\x20-\\x7E]", "")
                            .replaceAll("(?<=\\d)(AM|PM)", " $1");
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.ENGLISH);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    try {
                        // Parse the original string into a Date object
                        Date date = inputFormat.parse(createdAtStr);
                        // Format it into the desired format
                        String formattedDate = outputFormat.format(date);
                        createdAt = Utils.parseStringToDate(formattedDate, Utils.yearMonthDayTimeFormat);
                    } catch (ParseException e) {
                        System.err.println("Error parsing createdAt: " + createdAtStr);
                        e.printStackTrace();
                        createdAt = new Date(); // Fallback to current date if parsing fails
                    }
                } else {
                    createdAt = new Date(); // Default to current date if not present
                }

                if (appointmentDetails.get(0).has("modifiedAt") && !appointmentDetails.get(0).get("modifiedAt").isJsonNull()) {
                    String modifiedAtStr = appointmentDetails.get(0).get("modifiedAt").getAsString();
                    // Clean up any "?AM" / "?PM":
                    modifiedAtStr = modifiedAtStr
                            .replace("?AM", " AM")
                            .replace("?PM", " PM");

                    modifiedAtStr = modifiedAtStr.replaceAll("[^\\x20-\\x7E]", "")
                            .replaceAll("(?<=\\d)(AM|PM)", " $1");
                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy, hh:mm:ss a", Locale.ENGLISH);
                    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

                    try {
                        // Parse the original string into a Date object
                        Date date = inputFormat.parse(modifiedAtStr);
                        // Format it into the desired format
                        String formattedDate = outputFormat.format(date);
                        modifiedAt = Utils.parseStringToDate(formattedDate, Utils.yearMonthDayTimeFormat);
                    } catch (ParseException e) {
                        System.err.println("Error parsing modifiedAt: " + modifiedAtStr);
                        e.printStackTrace();
                        modifiedAt = new Date(); // Fallback to current date if parsing fails
                    }
                } else {
                    modifiedAt = new Date(); // Default to current date if not present
                }
            } catch (Exception e) {
                System.err.println("Error parsing dates: " + e.getMessage());
            }

            // Extract booleans with default values
            String appointmentStatus = appointmentDetails.get(0).has("appointmentStatus") ?
                    appointmentDetails.get(0).get("appointmentStatus").getAsString() : "false";
            String paymentStatus = appointmentDetails.get(0).has("paymentStatus") ?
                    appointmentDetails.get(0).get("paymentStatus").getAsString() : "false";

            Appointment appointment = new Appointment(
                    json.get("id").getAsInt(), workshop_id, service_id, technician_id,
                    appointmentDetails.get(0).get("workshopName").getAsString(),
                    appointmentDetails.get(0).get("serviceName").getAsString(),
                    appointmentDetails.get(0).get("technicianName").getAsString(),
                    scheduledTime, createdAt, modifiedAt,
                    appointmentStatus,
                    appointmentDetails.get(0).get("paymentMethod").getAsString(),
                    paymentStatus
            );

            appointmentsTableView.getItems().add(appointment);
        } catch (Exception e) {
            System.err.println("Error adding appointment to table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Defines two <code>PseudoClass</code> objects that define the appearance of
     * the tables rows which contain an apointment from the past. The style is
     * defined in <code>application.css</code> with the tag
     * <code>.table-view .table-row-cell:pastAppointment/pastAppointmentSelected .text</code>.<br>
     * Adds a
     * {@link javafx.scene.control.TableView#setRowFactory(javafx.util.Callback)} to
     * the table view to handle the event of changing the row color of past
     * appointments. In the <code>updateItem</code> method it checks if the
     * appointment is <code>null</code> in which case the table view is only
     * refreshed. If the appointment is from the past, changes the pseudo class to
     * one of the previously defined pseudo classes, depending on whether the
     * appointment is selected or not (if the appointment is in the past and
     * selected, the row gets a lighter grey color).
     */
    private void styleRows() {
        // define pseudo class for past appointments
        final PseudoClass pastAppointmentClass = PseudoClass.getPseudoClass("pastAppointment");
        final PseudoClass pastAppointmentSelectedClass = PseudoClass.getPseudoClass("pastAppointmentSelected");
        appointmentsTableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Appointment item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    // refresh view so that after resorting the rows, the coloring is still correct
                    appointmentsTableView.refresh();
                } else {
                    // Clear any existing pseudo classes
                    pseudoClassStateChanged(pastAppointmentClass, false);
                    pseudoClassStateChanged(pastAppointmentSelectedClass, false);

                    // Only check past date if scheduledTime is not null
                    Date scheduledTime = item.getScheduledTime();
                    if (scheduledTime != null && Utils.isPastDate(scheduledTime)) {
                        // if current item is from the past, set pseudo class to true, check if this
                        // item is the selected one; if yes, set other pseudo class to true
                        Appointment selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
                        if (item.equals(selectedAppointment)) {
                            pseudoClassStateChanged(pastAppointmentSelectedClass, true);
                        } else {
                            pseudoClassStateChanged(pastAppointmentClass, true);
                        }
                    }
                }
            }
        });
    }

    /**
     * Deletes the selected appointment and refreshes the profile view.
     *
     * @param button      the delete button.
     * @param appointment the appointment to be deleted.
     */
    public void addDeleteButtonListener(Button button, Appointment appointment) {
        button.setOnAction(event -> {
            try {
                boolean success = restClient.deleteAppointment(appointment.getAppointment_id());
                if (success) {
                    sceneNavigator.loadSceneToMainWindow(sceneNavigator.PROFILE, null);
                } else {
                    System.err.println("Failed to delete appointment: " + appointment.getAppointment_id());
                }
            } catch (Exception e) {
                System.err.println("Error deleting appointment: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Adds an edit listener to allow modification of reservations.
     *
     * @param button      the edit button.
     * @param appointment the appointment to be edited.
     */
    public void addEditButtonListener(Button button, Appointment appointment) {
        button.setOnAction(event -> {
            try {
                sceneNavigator.setPreviousScene(sceneNavigator.PROFILE);

                String[] data = {
                        appointment.getAppointment_id() + "",
                        appointment.getWorkshop_id() + "",
                        appointment.getService_id() + "",
                        appointment.getTechnician_id() + "",
                        appointment.getWorkshop(),
                        appointment.getService(),
                        appointment.getTechnician(),
                        Utils.yearMonthDayTimeFormat.format(appointment.getScheduledTime()),
                        Utils.yearMonthDayTimeFormat.format(appointment.getCreatedAt()),
                        Utils.yearMonthDayTimeFormat.format(appointment.getModifiedAt()),
                        appointment.isAppointmentStatus() + "",
                        appointment.getPaymentMethod(),
                        appointment.isPaymentStatus() + "",
                };

                sceneNavigator.loadSceneToMainWindow(sceneNavigator.APPOINTMENT, Arrays.asList(data));
            } catch (Exception e) {
                System.err.println("Error handling edit button click: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Is called when the user clicks on a table row. <br>
     * <br>
     * Determines which row was clicked (index and item). Computes the position
     * where the delete and modify button should be added for the selected
     * appointment and adds both buttons. If the selected appointment is in the
     * past, both buttons get disabled.
     */
    @FXML
    public void onAppointmentItemClicked() {
        // clear all elements in button grid pane
        buttonGridPane.getChildren().clear();

        // get index and content of clicked row
        int index = appointmentsTableView.getSelectionModel().getSelectedIndex();
        Appointment appointment = appointmentsTableView.getSelectionModel().getSelectedItem();

        if (appointment != null) {
            // add empty pane in button grid pane so that buttons will be added right underneath the pane
            Pane pane = new Pane();
            int height = (index + 1) * tableRowHeight - 3; // -3 because of button padding
            pane.setMinHeight(height);
            pane.setMaxHeight(height);
            buttonGridPane.add(pane, 0, 0);

            // add buttons for edit and delete with picture and respective click listener
            Button editButton = addButtonWithIcon("/images/edit_icon.png", 0, 1, 0, appointment);
            Button deleteButton = addButtonWithIcon("/images/delete_icon.png", 1, 1, 1, appointment);


//            if (Utils.isPastDate(appointment.getScheduledTime())) {
//                editButton.setDisable(true);
//                deleteButton.setDisable(true);
//            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        firstnameLabel.setText(restClient.getUser().getFirstName());
        lastnameLabel.setText(restClient.getUser().getLastName());
        emailLabel.setText(restClient.getUser().getEmail());
        loyaltyTokensLabel.setText(String.valueOf(restClient.getUser().getLoyaltyTokens()));

        initializeAppointmentTableView();
    }

    private void setTableRowAndHeaderHeight() {
        int tableItemCount = Bindings.size(appointmentsTableView.getItems()).get() + 1;
        appointmentsTableView.setFixedCellSize(tableRowHeight);

        appointmentsTableView.prefHeightProperty()
                .bind(appointmentsTableView.fixedCellSizeProperty().multiply(tableItemCount));
        appointmentsTableView.minHeightProperty().bind(appointmentsTableView.prefHeightProperty());
        appointmentsTableView.maxHeightProperty().bind(appointmentsTableView.prefHeightProperty());

        appointmentsTableView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            Pane header = (Pane) appointmentsTableView.lookup("TableHeaderRow");
            header.prefHeightProperty().bind(appointmentsTableView.prefHeightProperty().divide(tableItemCount));
        });
    }

    /**
     * Creates a new button with the specified image on it. Adds a listener to the
     * button which is dependent on the type of button that is created (either
     * delete or edit button). Then adds the button to the button grid (right next
     * to the table view and right beneath the previously added empty pane in
     * {@link #onAppointmentItemClicked()})
     *
     * @param path           the path to the image
     * @param col            the number for the column
     * @param row            the number for the row
     * @param buttonTyp      0 = EditButton, 1 = DeleteButton, 2 = ShowTicketButton
     * @param appointment    the <code>Appointment</code> object on which was clicked
     */
    public Button addButtonWithIcon(String path, int col, int row, int buttonTyp, Appointment appointment) {
        try {
            ImageView imageview = new ImageView(new Image(path));
            imageview.setFitWidth(15);
            imageview.setPreserveRatio(true);

            Button button = new Button();
            button.setGraphic(imageview);
            button.setPadding(new Insets(5, 5, 5, 5));
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMaxHeight(Double.MAX_VALUE);
            button.setStyle("-fx-background-color: #333333; -fx-cursor: hand; -fx-text-fill: white; " +
                    "-fx-background-radius: 0; -fx-background-insets: 0; " +
                    "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            button.setFocusTraversable(false);
            button.setPickOnBounds(true);
            button.setMouseTransparent(false);

            // Add hover effect
            button.setOnMouseEntered(e -> {
                button.setStyle("-fx-background-color: #555555; -fx-cursor: hand; -fx-text-fill: white; " +
                        "-fx-background-radius: 0; -fx-background-insets: 0; " +
                        "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            });
            button.setOnMouseExited(e -> {
                button.setStyle("-fx-background-color: #333333; -fx-cursor: hand; -fx-text-fill: white; " +
                        "-fx-background-radius: 0; -fx-background-insets: 0; " +
                        "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            });
            button.setOnMousePressed(e -> {
                button.setStyle("-fx-background-color: #222222; -fx-cursor: hand; -fx-text-fill: white; " +
                        "-fx-background-radius: 0; -fx-background-insets: 0; " +
                        "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            });
            button.setOnMouseReleased(e -> {
                button.setStyle("-fx-background-color: #333333; -fx-cursor: hand; -fx-text-fill: white; " +
                        "-fx-background-radius: 0; -fx-background-insets: 0; " +
                        "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
            });

            // add button click listener depending on type of button
            switch (buttonTyp) {
                case 0:
                    button.setTooltip(new Tooltip("Edit Appointment"));
                    addEditButtonListener(button, appointment);
                    break;
                case 1:
                    button.setTooltip(new Tooltip("Delete Appointment"));
                    addDeleteButtonListener(button, appointment);
                    break;
            }

            buttonGridPane.add(button, col, row);
            GridPane.setFillWidth(button, true);
            GridPane.setFillHeight(button, true);
            buttonGridPane.toFront();
            button.toFront();

            return button;
        } catch (Exception e) {
            System.err.println("Error creating button: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
