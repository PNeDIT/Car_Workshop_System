package rest_client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import model.User;
import utils.StringNames;
import utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class RestClient {

    /**
     * --------------------------------------------------------------------
     * ---------------- DO NOT CHANGE THE FOLLOWING CODE! -----------------
     * --------------------------------------------------------------------
     */

    private static RestClient instance;

    private User user;

    /**
     * Private constructor, so that no objects can be created from the outside.
     */
    private RestClient() {
        // set base url
        String SERVER_URL = "http://localhost:4569";
        Unirest.config().defaultBaseUrl(SERVER_URL);
    }

    public static RestClient getRestClient() {
        return getRestClient(false);
    }

    public static RestClient getRestClient(boolean skipServerTest) {
        if (instance == null) {
            instance = new RestClient();
        }
        if (!skipServerTest) {
            // Test if RestServer is reachable and show helpful error message if not
            try {
                Unirest.get("/test").asEmpty();
            } catch (UnirestException e) {
                Logger logger = Logger.getLogger("RestServer test");
                logger.warning("RestServer is unreachable - did you start it?");
                throw new RuntimeException("RestServer is unreachable - did you start it?");
            }
        }
        return instance;
    }

    // GET and SET user

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Converts the string in JSON format to a <code>JsonObject</code>.
     *
     * @param jsonString the string in JSON format
     * @return the <code>JsonObject</code>
     */
    private JsonObject mapStringToJsonObject(String jsonString) {
        return new Gson().fromJson(jsonString, JsonArray.class).get(0).getAsJsonObject();
    }

    /**
     * Converts the string representation of an array of JSON objects into a list of
     * <code>JsonObject</code>.
     *
     * @param jsonString the string in JSON format
     * @return the list of <code>JsonObject</code>
     */
    private List<JsonObject> mapStringToJsonObjectList(String jsonString) {
        JsonArray jsonArray = new Gson().fromJson(jsonString, JsonArray.class);

        List<JsonObject> jsonList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonList.add(jsonArray.get(i).getAsJsonObject());
        }
        return jsonList;
    }

    /*
     * ------------------------- CUSTOMER REQUESTS ------------------------
     */

    /**
     * Makes a REST request to the server. Retrieves the user id from the
     * corresponding database table by providing only the email address of the user
     * (is unique).
     *
     * @param email the email of the user
     * @return the result as a <code>JsonObject</code>
     */
    public List<JsonObject> getUserInfoByMail(String email) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/customers")
                .queryString(StringNames.email, email)
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

    /**
     * Makes a REST request to the server. Checks in the corresponding table if the
     * provided user credentials (email and password) are a valid entry, this means
     * if the password belongs to the email address.
     *
     * @param email    the email of the user
     * @param password the corresponding password of the same user
     * @return <code>true</code> if credentials are; <code>false</code> otherwise
     */
    public boolean validClientCredentials(String email, String password) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/customers")
                .queryString(StringNames.email, email)
                .queryString(StringNames.password, password)
                .asJson();
        return jsonResponse.getStatus() == 200;
    }

    /**
     * Makes a REST request to the server. Tries to create a new user entry with the
     * provided information in the customer table.
     *
     * @param firstname the first name of the user
     * @param lastname  the last name of the user
     * @param email     the email of the user
     * @return <code>true</code> if user was created successfully;
     * <code>false</code> otherwise
     */
    public boolean createNewUser(String firstname, String lastname, String email, String password, int security_question_id, String security_answer, String vehicle_registration_number, String phone_number) {
        HttpResponse<JsonNode> jsonResponse = Unirest
                .post("/customer/create")
                .queryString(StringNames.firstname, firstname)
                .queryString(StringNames.lastname, lastname)
                .queryString(StringNames.email, email)
                .queryString(StringNames.password, password)
                .queryString(StringNames.security_question_id, security_question_id)
                .queryString(StringNames.security_answer, security_answer)
                .queryString(StringNames.vehicle_registration_number, vehicle_registration_number)
                .queryString(StringNames.phone_number, phone_number)
                .asJson();

        return jsonResponse.getStatus() == 201;
    }

    /**
     * Fetches all appointments for a specific user.
     * @return A list of appointments in JSON format, or an empty list if no appointments are found.
     * @throws UnirestException if there is an issue with the API call.
     */
    public List<JsonObject> getAppointmentsForUser() throws UnirestException {

        HttpResponse<JsonNode> jsonResponse = Unirest.get("/customer/appointments")
                .queryString(StringNames.customer_id, user.getId())
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

    /**
     * Retrieve all security questions.
     *
     * @return A list of security questions as <code>JsonObject</code>, each containing
     *         the question ID and the question text.
     */
    public List<JsonObject> getSecurityQuestions() {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get("/security-questions")
                    .asJson();

            if (jsonResponse.getStatus() != 200) {
                System.err.println("Failed to fetch security questions. Status: " + jsonResponse.getStatus());
                return Collections.emptyList();
            }

            List<JsonObject> questions = mapStringToJsonObjectList(jsonResponse.getBody().toString());
            return questions;
        } catch (Exception e) {
            System.err.println("Error fetching security questions: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /*
     * --------------------------------------------------------
     * -------------------------- END -------------------------
     * --------------------------------------------------------
     */

    /*
     * TODO: For each endpoint of the RestServer, create a method in this class that
     * makes a request to this endpoint. After the response of the server has been
     * received, check the status of it and depending on the outcome, return a
     * respective value. You can use the method "mapStringToJsonObject" and
     * "mapStringToJsonObjectList" to map the response to a JsonObject or a list of
     * JsonObjects respectively.
     *
     * Look at the implementation of the request methods in the Cinema Case if you are
     * unclear how you should write the function.
     *
     * Sensitive data like the reservations of one specific customer should not be
     * available for all people but only for that specific customer who must provide
     * his/her correct credentials in order to retrieve this information. For
     * realizing this concept, authorization can be used and must be included for
     * several request to the REST server.
     * For requests to endpoints that query the "reservation" table (might be a different
     * name for your application), make sure you also include the
     * authorization string (you can simply use .header("Authorization",
     * user.getAuthorization())) in the request), as shown in the Cinema Case.
     *
     * Remember to use the query parameter keys from the StringNames class and don't
     * hardcode them.
     *
     * You can use the following template as a starting point for each request
     * method. Keep in mind that you might need to change the REST method (get,
     * post, put, delete).
     */

    public List<JsonObject> requestEndpoint(int queryParam1, String queryParam2) {
        HttpResponse<JsonNode> jsonResponse = Unirest
                .get("/endpoint")
                .queryString(StringNames.query1, queryParam1)
                .queryString(StringNames.query2, queryParam2)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

// workshop requests --------------------------------------------------------------------------------------------------

// view workshops
    /**
     * Makes a REST request to the server. Retrieves all available workshops as a list
     * from the corresponding database table.
     *
     * @return the result as a list of <code>JsonObject</code>
     */
    public List<JsonObject> requestAllWorkshops() {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/workshops")
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return Collections.emptyList();
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());

    }

// request information about a specific workshop
    /**
     * Makes a REST request to the server. Retrieves all information of the workshop
     * with the given id from the corresponding database table.
     *
     * @param workshop_id the id of the workshop
     * @return the result as a <code>JsonObject</code>
     */
    public List<JsonObject> requestWorkshopInformation(int workshop_id) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/workshops")
                .queryString(StringNames.workshop_id, workshop_id)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }


// service requests ----------------------------------------------------------------------------------------------------

// view all available services of a workshop

    /**
     * Makes a REST request to the server. Retrieves all services of one specific
     * workshop with the given id from the corresponding database table.
     *
     * @param workshop_id the id of the workshop
     * @return the result as a list of <code>JsonObject</code>
     */
    public List<JsonObject> requestAllServicesOfWorkshop(int workshop_id) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/services")
                .queryString(StringNames.workshop_id, workshop_id)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

// request information about a specific service
    /**
     * Makes a REST request to the server. Retrieves all information of one specific
     * service, identified by id, in one specific workshop, also identified by id, from
     * the corresponding database tables.
     *
     * @param workshop_id the id of the workshop
     * @param service_id the id of the specific service
     * @return the result as a list of <code>JsonObject</code>
     */
    public List<JsonObject> requestServiceInformationOfWorkshop(int workshop_id, int service_id) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/services")
                .queryString(StringNames.workshop_id, workshop_id)
                .queryString(StringNames.service_id, service_id)
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            return null;
        }
        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

// appointment requests-------------------------------------------------------------------------------------------------

    //create a new appointment

    /**
     * Makes a REST request to the server. Tries to create a new appointment entry
     * for the currently logged in user.
     *
     * @param workshop_id       the ID of the workshop where the appointment will take place
     * @param service_id        the ID of the service being provided during the appointment
     * @param scheduledTime     the date and time when the appointment is scheduled
     * @param paymentMethod     the payment method used for paying for  the appointment
     * @return <code>true</code> if appointment was created successfully;
     * <code>false</code> otherwise
     */
    //makeAppointment?customer_id=value&workshop_id=value&service_id=value&scheduledTime=value&paymentMethod=value
    public boolean createNewAppointment(int workshop_id, int service_id, Date scheduledTime, String paymentMethod) {
        try {
            // Format the Date into the expected string format using your Utils class
            String formattedScheduledTime = Utils.yearMonthDayTimeFormat.format(scheduledTime);

            HttpResponse<JsonNode> jsonResponse = Unirest
                    .post("/appointment/create")
                    .queryString(StringNames.customer_id, user.getId())
                    .queryString(StringNames.workshop_id, workshop_id)
                    .queryString(StringNames.service_id, service_id)
                    .queryString(StringNames.scheduledTime, formattedScheduledTime)
                    .queryString(StringNames.paymentMethod, paymentMethod)
                    .header(StringNames.authorization, user.getAuthorization())
                    .asJson();

            if (jsonResponse.getBody() != null) {
                System.out.println("- Response Body: " + jsonResponse.getBody().toString());
            } else {
                System.out.println("- Response Body is null");
            }

            if (jsonResponse.getStatus() == 500) {
                System.err.println("RestClient - Server error (500) occurred");
                return false;
            }

            boolean success = jsonResponse.getStatus() == 201;
            System.out.println("RestClient - Operation " + (success ? "successful" : "failed") + " (Status: " + jsonResponse.getStatus() + ")");
            return success;
        } catch (Exception e) {
            System.err.println("\nRestClient - Error creating appointment:");
            System.err.println("- Error message: " + e.getMessage());
            System.err.println("- Error class: " + e.getClass().getName());
            e.printStackTrace(); // Print full stack trace for debugging
            return false;
        }
    }


    // delete an appointment

    /**
     * Makes a REST request to the server. Tries to delete an appointment entry with
     * the given appointment id.
     *
     * @param appointment_id the id of the appointment
     * @return <code>true</code> if appointment was deleted successfully;
     * <code>false</code> otherwise
     */
    public boolean deleteAppointment(int appointment_id) {
        HttpResponse<JsonNode> jsonResponse = Unirest.delete("/appointment/delete")
                .queryString(StringNames.appointment_id, appointment_id)
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
        return jsonResponse.getStatus() == 201;
    }

    /**
     * Makes a REST request to the server. Tries to modify an appointment entry with
     * a given appointment id. The user can modify the scheduled time and the payment method.
     *
     * @param appointment_id the id of the appointment
     * @param scheduledTime     the date and time when the appointment is scheduled
     * @param paymentMethod     the payment method used for paying for  the appointment
     * @return <code>true</code> if appointment was modified successfully;
     * <code>false</code> otherwise
     */
    public boolean modifyAppointment(int appointment_id, Date scheduledTime, String paymentMethod) {
        // Format the Date into the expected string format using your Utils class
        String formattedScheduledTime = Utils.yearMonthDayTimeFormat.format(scheduledTime);
        HttpResponse<JsonNode> jsonResponse = Unirest.put("/appointment/modify")
                .queryString(StringNames.appointment_id, appointment_id)
                .queryString(StringNames.scheduledTime, formattedScheduledTime)
                .queryString(StringNames.paymentMethod, paymentMethod)
                .header(StringNames.authorization, user.getAuthorization())
                .asJson();
        return jsonResponse.getStatus() == 201;
    }

    // get details of an appointment
    /**
     * Fetches details of a specific appointment by its ID.
     *
     * @param appointment_id The ID of the appointment.
     * @return A JsonObject containing appointment details, or null if the request fails.
     * @throws UnirestException if there is an issue with the API call.
     */
    public List<JsonObject> getAppointmentDetails(int appointment_id) throws UnirestException {

        HttpResponse<JsonNode> jsonResponse = Unirest.get("/appointments")
                .queryString("appointment_id", appointment_id)
                .header("Authorization", user.getAuthorization())
                .asJson();
        if (jsonResponse.getStatus() != 200) {
            System.err.println("Failed to fetch appointment details for ID: " + appointment_id);
            return null;
        }

        return mapStringToJsonObjectList(jsonResponse.getBody().toString());
    }

    /**
     * Fetches available appointment slots for the specified date and service.
     *
     * @param scheduledTime The requested date to check for available appointments.
     * @param workshop_id The ID of the workshop where the service is offered.
     * @param service_id The ID of the service to check availability for.
     * @return A list of strings representing available appointment slots (e.g., "10:00", "10:45").
     * @throws UnirestException if there is an issue with the API call.
     */
    public List<String> getAvailableAppointments(Date scheduledTime, int workshop_id, int service_id) throws UnirestException {
        // Format the scheduled time to "yyyy-MM-dd HH:mm"
        String formattedDate = Utils.yearMonthDayTimeFormat.format(scheduledTime);

        HttpResponse<JsonNode> jsonResponse = Unirest.get("/appointments/available")
                .queryString("scheduledTime", formattedDate)
                .queryString("workshop_id", workshop_id)
                .queryString("service_id", service_id)
                .asJson();

        if (jsonResponse.getStatus() != 200) {
            System.err.println("Failed to fetch available appointments for scheduled time: " + formattedDate + " for workshop: " + workshop_id + " and service: " + service_id);
            return new ArrayList<>();
        }

        // Parse the JSON response into a list of available appointments
        JsonArray availableAppointments = new Gson().fromJson(jsonResponse.getBody().toString(), JsonArray.class);

        List<String> slots = new ArrayList<>();
        for (int i = 0; i < availableAppointments.size(); i++) {
            slots.add(availableAppointments.get(i).getAsString());
        }
        return slots;
    }

// technician requests-------------------------------------------------------------------------------------------------

    //List<JsonObject> technicians = restClient.requestTechniciansForService(workshop_id, //service_id);

    // request information about a specific service
    /**
     * Makes a REST request to the server. Retrieves all information of one specific
     * service, identified by id, in one specific workshop, also identified by id, from
     * the corresponding database tables.
     *
     * @param workshop_id the id of the workshop
     * @param //service_id the id of the specific service
     * @return the result as a list of <code>JsonObject</code>
     */
    public List<JsonObject> requestTechniciansForService(int workshop_id) {
        HttpResponse<JsonNode> jsonResponse = Unirest.get("/technicians")
                .queryString(StringNames.workshop_id, workshop_id)
                .asJson();
                
        if (jsonResponse.getStatus() != 200) {
            System.err.println("Failed to fetch technicians. Status: " + jsonResponse.getStatus());
            return Collections.emptyList();
        }
        
        List<JsonObject> technicians = mapStringToJsonObjectList(jsonResponse.getBody().toString());
        return technicians;
    }
}


