package model;

import java.util.Date;

public class Appointment {

    // ids
    private int appointment_id;
    private int workshop_id;
    private int service_id;
    private int technician_id;

    /*
     * TODO: define all attributes a reservation has with its correct data type.
     * These information should later correspond to the table view that is
     * initialized in the class ProfilePanelController. Thus, the amount of columns
     * you have in the layout file ProfilePanel.fxml must be the same as the amount
     * of attributes you define here.
     *
     * The order in which you define your attributes does not play a role here.
     */
    // appointments attributes
    private String workshop;
    private String service;
    private String technician;
    private Date scheduledTime;
    private Date createdAt;
    private Date modifiedAt;
    private String appointmentStatus;
    private String paymentMethod;
    private String paymentStatus;

    /*
     * TODO: define a constructor which assigns the respective value to each class
     * attribute.
     *
     * Car Workshop: there are 13 attributes in total, thus the constructor has 13
     * parameters and assigns them accordingly.
     */
    public Appointment(int appointment_id, int workshop_id, int service_id, int technician_id, String workshop, String service, String technician,
                       Date scheduledTime, Date createdAt, Date modifiedAt, String appointmentStatus, String paymentMethod, String paymentStatus) {
        super();
        this.appointment_id = appointment_id;
        this.workshop_id = workshop_id;
        this.service_id = service_id;
        this.technician_id = technician_id;
        this.workshop = workshop;
        this.service = service;
        this.technician = technician;
        this.scheduledTime = scheduledTime;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.appointmentStatus = appointmentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
    }

    /*
     * TODO: change the strings inside the String-array that gets returned. The
     * array represents the order of columns in the appointment table view and will
     * serve as a connection between a class attribute and the FXML-column that is
     * defined in the layout file.
     *
     * The order of the strings in the array !MUST! be the same as the order of the
     * columns from the table view in the layout file ProfilePanel.fxml.
     *
     * Each string must correspond to a previously defined class attribute
     * maintaining the exact same spelling. E.g. there is a class attribute
     * "cinema", thus in the array the string is named "cinema".
     *
     * Cinema Case: In ProfilePanel.fxml the first column is the date column.
     * Therefore, in the array the first entry is "date". "date" also corresponds to
     * the class attribute date, maintaining the same spelling.
     *
     * @return the array with attributes defining the order of columns in the table
     *         view
     */

    /**
     * @return the array with attributes defining the order of columns in the table
     * view
     */
    public static String[] getVariableNames() {
        return new String[]{"workshop", "service", "technician", "scheduledTime", "createdAt", "modifiedAt", "appointmentStatus", "paymentMethod", "paymentStatus"};
    }

    // GETTER and SETTER
    public int getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(int appointment_id) {
        this.appointment_id = appointment_id;
    }

    public int getWorkshop_id() {
        return workshop_id;
    }

    public void setWorkshop_id(int workshop_id) {
        this.workshop_id = workshop_id;
    }

    public int getService_id() {
        return service_id;
    }

    public void setService_id(int service_id) {
        this.service_id = service_id;
    }

    public int getTechnician_id() {
        return technician_id;
    }

    public void setTechnician_id(int technician_id) {
        this.technician_id = technician_id;
    }

    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    public Date getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Date scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String isAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String isPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /*
     * TODO: autogenerate a toString() method with all your class attributes:
     * 1. Right click in code window
     * 2. Select "Generate"
     * 3. Select "toString()"
     * 4. Make sure all attributes are selected, and button "add @Override" is selected
     * 5. Click OK
     */
    @Override
    public String toString() {
        return "Appointment{" +
                "appointment_id=" + appointment_id +
                ", workshop_id=" + workshop_id +
                ", service_id=" + service_id +
                ", technician_id=" + technician_id +
                ", workshop='" + workshop + '\'' +
                ", service='" + service + '\'' +
                ", technician='" + technician + '\'' +
                ", scheduledTime=" + scheduledTime +
                ", createdAt=" + createdAt +
                ", modifiedAt=" + modifiedAt +
                ", appointmentStatus=" + appointmentStatus +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus=" + paymentStatus +
                '}';
    }

    /*
     * TODO: autogenerate a equals() method with all your class attributes:
     * 1. Right click in code window
     * 2. Select "Generate"
     * 3. Select "equals() and hashCode()"
     * 4. Select IntelliJ Default as Template
     * 5. Click Next
     * 6. Select all attributes twice
     * 7. Select attributes that must not be null
     * 8. Click ok
     *
     * Note: we don't need the hashCode function. You can either delete or ignore it
     */

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Appointment that = (Appointment) o;
        return appointment_id == that.appointment_id && workshop_id == that.workshop_id && service_id == that.service_id && technician_id == that.technician_id && appointmentStatus == that.appointmentStatus && paymentStatus == that.paymentStatus && workshop.equals(that.workshop) && service.equals(that.service) && technician.equals(that.technician) && scheduledTime.equals(that.scheduledTime) && createdAt.equals(that.createdAt) && modifiedAt.equals(that.modifiedAt) && paymentMethod.equals(that.paymentMethod);
    }
}
