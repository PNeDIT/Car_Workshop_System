package rest_server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.json.JsonMapper;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import model.DatabaseConnector;
import org.jetbrains.annotations.NotNull;
import utils.StringNames;
import utils.Utils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class RestServer {

    // reach server under: http://localhost:4569/ (simply type it in your web browser)

    private static DataValidation dataVal;
    private final Javalin javalinApp;
    private DatabaseConnector dbConnector;


    public RestServer(DatabaseConnector dbConnector, DataValidation dataValidation) {
        this(dbConnector, dataValidation, 4569);
    }

    public RestServer(DatabaseConnector dbConnector, DataValidation dataValidation, int port) {
        this.dbConnector = dbConnector;
        dataVal = dataValidation;

        Gson gson = new Gson();
        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public @NotNull String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return gson.toJson(obj, type);
            }

            @Override
            public <T> @NotNull T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return gson.fromJson(json, targetType);
            }
        };
        this.javalinApp = Javalin.create(config -> config.jsonMapper(gsonMapper)).start(port);
        defineRoutes();
    }

    public static void main(String[] args) {
        DatabaseConnector dbConnector = new DatabaseConnector("reservation_system");
        new RestServer(dbConnector, new DataValidation(dbConnector));
    }

    public void setDbConnectorAndDataValidator(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
        dataVal = new DataValidation(dbConnector);
    }

    public void stopServer() {
        javalinApp.stop();
    }

    public void defineRoutes() {

        javalinApp.get("test", context -> {
            context.result("Test successfull, server is reachable!");
        });

        javalinApp.get("/workshops", context -> {
            String workshop_id = context.queryParam(StringNames.workshop_id);

            if (workshop_id == null) { // without query parameters
                /*
                 * SELECT *
                 * FROM workshops
                 */
                List<Map<String, Object>> res = dbConnector.executeSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.WORKSHOPS},
                        null, null, null);
                context.status(200);
                context.json(res);
            } else { // with query parameters
                // data validation
                if (!dataVal.isValidId(workshop_id)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }
                /*
                 * SELECT *
                 * FROM workshops
                 * WHERE id = workshop_id
                 */
                List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.WORKSHOPS}, null, "id = ?", new String[]{workshop_id});

                // check for empty result set
                if (queryResult.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Workshop with id " + workshop_id + " not found."});
                    return;
                }
                context.status(200);
                context.json(queryResult);
            }
        });

        // Service routes
        javalinApp.get("/services", context -> {
            String workshop_id = context.queryParam(StringNames.workshop_id);
            String service_id = context.queryParam(StringNames.service_id);
            List<Map<String, Object>> queryResult;

            // data validation
            if ((workshop_id != null && !dataVal.isValidId(workshop_id))
                    || (service_id != null && !dataVal.isValidId(service_id))) {
                context.status(400);
                context.json(new String[]{"Id(s) must be integer and greater than 0."});
                return;
            }

            if (workshop_id == null && service_id == null) { // without query parameters
                /*
                 * SELECT *
                 * FROM service
                 */
                queryResult = dbConnector.executeSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.SERVICES},
                        null, null, null);
            } else if (workshop_id != null && service_id == null) { // get all services in one workshop
                /* *********??>?****()()()----CHANGEEEEEEEEEEEE
                 * SELECT mp.id as moviePlaytimeId, m.id as movieId, m.name, m.length, m.genre
                 * FROM movie_playtimes as mp, movies as m, date_playtimes as dp, dates as d
                 * WHERE mp.cinemaId = cinemaId and mp.movieId = m.id and mp.id = dp.moviePlaytimeId and dp.dateId = d.id
                 * 			and dp.freeSeats > 0 and (( d.date = CURDATE() and mp.startTime > CURTIME() ) OR d.date > CURDATE())
                 *********??>?****()()()----CHANGEEEEEEEEEEEE
                 */
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"s.*"},
                        new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES}, new String[]{"ws", "s"},
                        "ws.workshop_id = ? and ws.service_id = s.id", new String[]{workshop_id});
            } else if (workshop_id == null && service_id != null) { // get all workshops that offer the same service
                /*
                 *********??>?****()()()----CHANGEEEEEEEEEEEE
                 * SELECT c.*
                 * FROM movie_playtimes as mp, cinemas as c
                 * WHERE mp.movieId = movieId AND mp.cinemaId = c.id
                 * *********??>?****()()()----CHANGEEEEEEEEEEEE
                 */
                queryResult = dbConnector.executeSelectQuery(new String[]{"w.*"},
                        new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.WORKSHOPS},
                        new String[]{"ws", "w"}, "ws.service_id = ? and ws.workshop_id = w.id", new String[]{service_id});
            } else { // get additional information, i.e. playtimes, of one movie in one cinema *********??>?****()()()----CHANGEEEEEEEEEEEE
                /*
                 *********??>?****()()()----CHANGEEEEEEEEEEEE
                 * SELECT dp.id as datePlaytimeId, mp.startTime, d.date, m.name, m.length, m.genre
                 * FROM movie_playtimes as mp, date_playtimes as dp, dates as d, movies as m
                 * WHERE m.id = movieId and m.id = mp.movieId and mp.cinemaId = cinemaId and mp.id = dp.moviePlaytimeId and dp.dateId = d.id
                 * 			and dp.freeSeats > 0 and (( d.date = CURDATE() and mp.startTime > CURTIME()) OR d.date > CURDATE())
                 * *********??>?****()()()----CHANGEEEEEEEEEEEE
                 */
                queryResult = dbConnector.executeSelectQuery(
                        new String[]{"s.*"},
                        new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES},
                        new String[]{"ws", "s"},
                        "ws.service_id = ? and ws.workshop_id = ? and ws.service_id = s.id",
                        new String[]{service_id, workshop_id});
            }
            // check for empty result set
            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"Given id(s) not found or no entries with this id(s)."});
                return;
            }
            context.status(200);
            context.json(queryResult);
        });

        // Customer routes
        javalinApp.get("/customers", context -> {
            String email = context.queryParam(StringNames.email);
            String password = context.queryParam(StringNames.password);
            String authString = context.header(StringNames.authorization);
            List<Map<String, Object>> queryResult;

            // data validation
            if (email != null && !Utils.isValidEmailAddress(email)) {
                context.status(400);
                context.json(new String[]{"Email address format not correct."});
                return;
            }
            if (password != null && !Utils.isValidPassword(password)) {
                context.status(400);
                context.json(new String[]{"Password format not correct."});
                return;
            }

            if (email == null && password == null) {
                context.status(400);
                context.json(new String[]{"At least e-mail is required as parameter."});
                return;
            } else if (email != null && password == null) { // get customer information by email
                /*
                 * SELECT *
                 * FROM customers
                 * WHERE email = email
                 */
                queryResult = dbConnector.executeSelectQuery(
                    new String[]{"id", "firstname", "lastname", "email", "tokens"},
                    new String[]{DatabaseConnector.CUSTOMERS},
                    null,
                    "email = ?",
                    new String[]{email}
                );
                if (queryResult.isEmpty()) { // email not found
                    context.status(404);
                    context.json(new String[]{"E-mail not found."});
                    return;
                } else {
                    // check if user is authorized
                    String customer_id = String.valueOf(queryResult.get(0).get("id"));
                    if (authString == null || !dataVal.isUserAuthorized(authString, customer_id)) {
                        context.status(401);
                        context.json(new String[]{"User is not authorized to perform this action."});
                        return;
                    }
                }
            } else if (email == null && password != null) {
                context.status(400);
                context.json(new String[]{"E-mail required when password is given."});
                return;
            } else { // check client credentials
                /*
                 * SELECT *
                 * FROM customers
                 * WHERE email = email and password = password
                 */
                queryResult = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.CUSTOMERS}, null, "email = ? and password = ?",
                        new String[]{email, password});

                // If login successful, update loyalty tokens based on appointment history
                if (!queryResult.isEmpty()) {
                    String customer_id = String.valueOf(queryResult.get(0).get("id"));

                    // Count total appointments for this user
                    List<Map<String, Object>> appointmentCount = dbConnector.executeSelectQuery(
                            new String[]{"COUNT(*) as count"},
                            new String[]{DatabaseConnector.APPOINTMENTS},
                            null,
                            "customer_id = ?",
                            new String[]{customer_id}
                    );

                    if (!appointmentCount.isEmpty()) {
                        int totalAppointments = ((Number) appointmentCount.get(0).get("count")).intValue();

                        // Update loyalty tokens in database
                        dbConnector.executeUpdateQuery(
                                DatabaseConnector.CUSTOMERS,
                                new String[]{"loyalty_tokens = ?"},
                                new String[]{String.valueOf(totalAppointments)},
                                "id = ?",
                                new String[]{customer_id}
                        );

                        // Update the query result with new token count
                        queryResult.get(0).put("loyalty_tokens", totalAppointments);
                    }
                }
            }
            // check for empty result set
            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"E-mail not found or no valid credentials given."});
                return;
            }
            context.status(200);
            context.json(queryResult);
        });

        javalinApp.post("/customer/create", context -> {
            String firstname = context.queryParam(StringNames.firstname);
            String lastname = context.queryParam(StringNames.lastname);
            String email = context.queryParam(StringNames.email);
            String password = context.queryParam(StringNames.password);
            String security_question_id = context.queryParam(StringNames.security_question_id);
            String security_question;
            String security_answer = context.queryParam(StringNames.security_answer);
            String vehicle_registration_number = context.queryParam(StringNames.vehicle_registration_number);
            String phone_number = context.queryParam(StringNames.phone_number);

            if (firstname != null && lastname != null && email != null && password != null && security_question_id != null && security_answer != null) {
                // data validation
                if (!Utils.isAlpha(firstname) || !Utils.isAlpha(lastname)) {
                    context.status(400);
                    context.json(new String[]{"Firstname and lastname can only contain letters."});
                    return;
                }
                if (!Utils.isValidEmailAddress(email)) {
                    context.status(400);
                    context.json(new String[]{"Email address format not correct."});
                    return;
                }
                if(vehicle_registration_number != null){
                    if(!Utils.isValidVehicleRegistrationNumber(vehicle_registration_number)){
                        context.status(400);
                        context.json(new String[]{"Vehicle registration number is invalid."});
                        return;
                    }
                }
                if(phone_number != null){
                    if(!Utils.isValidPhoneNumber(phone_number)){
                        context.status(400);
                        context.json(new String[]{"Phone number is invalid."});
                        return;
                    }
                }
                if (!Utils.isValidPassword(password)) {
                    context.status(400);
                    context.json(new String[]{"Password format not correct."});
                    return;
                }
                // check if security_question_id exists
                List<Map<String, Object>> questionValidation = dbConnector.executeSelectQuery(
                        new String[]{"security_question"},
                        new String[]{DatabaseConnector.CUSTOMERS},
                        null, "security_question_id = ?", new String[]{security_question_id}
                );
                if (questionValidation.isEmpty()) {
                    context.status(400);
                    context.json(new String[]{"Invalid security question provided."});
                    return;
                }
                security_question = (String) questionValidation.get(0).get("security_question");

                if (security_answer.trim().isEmpty()) {
                    context.status(400);
                    context.json(new String[]{"Security answer cannot be empty."});
                    return;
                }

                /*
                 * INSERT INTO customers (firstName, lastName, email, vehicle_registration_number, phone_number, password, security_question_id, security_question, security_answer)
                 * VALUES (firstName, lastName, email, vehicle_registration_number, phone_number, password, security_question_id, security_question, security_answer)
                 */
                if (!dbConnector.executeInsertQuery(DatabaseConnector.CUSTOMERS,
                        new String[]{StringNames.firstname, StringNames.lastname, StringNames.email, StringNames.vehicle_registration_number,
                                StringNames.phone_number,StringNames.password, StringNames.security_question_id, StringNames.security_question, StringNames.security_answer},
                        new String[]{firstname, lastname, email, vehicle_registration_number, phone_number, password, security_question_id, security_question, security_answer})) {
                    context.status(400);
                    context.json(new String[]{"E-mail address does already exist."});
                    return;
                }

                context.status(201);
                context.json(new String[]{"Customer created successfully!"});
            } else {
                context.status(400);
                context.json(new String[]{"For creating a customer, firstname, lastname, " +
                        "e-mail, password, security question, and security answer are required!"});
            }
        });

        // Appointment routes
        javalinApp.get("/customer/appointments", context -> {
            String customer_id = context.queryParam(StringNames.customer_id);
            String authString = context.header(StringNames.authorization);

            if (customer_id != null) {
                // data validation
                if (!dataVal.isValidId(customer_id)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }

                // check if user is authorized
                if (authString == null || !dataVal.isUserAuthorized(authString, customer_id)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to view this information."});
                    return;
                }
                /*
                 * SELECT *
                 * FROM appointments
                 * WHERE customer_id = customer_id
                 */
                List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.APPOINTMENTS}, null, "customer_id = ?",
                        new String[]{customer_id});

                // check for empty result set
                if (queryResult.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Customer with id " + customer_id + " not found or has no appointments."});
                    return;
                }
                context.status(200);
                context.json(queryResult);
            } else {
                context.status(400);
                context.json(new String[]{"For retrieving all appointments of a customer, a customer id must be given."});
            }
        });

        javalinApp.post("/appointment/create", context -> {
            try {
                // retrieve parameters
                String customer_id = context.queryParam(StringNames.customer_id);
                String workshop_id = context.queryParam(StringNames.workshop_id);
                String service_id = context.queryParam(StringNames.service_id);
                String technician_id;
                String scheduledTime = context.queryParam(StringNames.scheduledTime);
                String createdAt = Utils.yearMonthDayTimeFormat.format(new Date()); // Current date and time
                String modifiedAt = createdAt;
                String appointmentStatus = "false";
                String paymentMethod = context.queryParam(StringNames.paymentMethod);
                String paymentStatus = "false";
                String authString = context.header(StringNames.authorization);

                if (customer_id != null && workshop_id != null && service_id != null && scheduledTime != null && paymentMethod != null) {
                    // validate scheduled time data
                    Date parsedScheduledTime;
                    try {
                        parsedScheduledTime = Utils.yearMonthDayTimeFormat.parse(scheduledTime);
                        
                        // Add validation for current/future time
                        Date currentTime = new Date();
                        if (parsedScheduledTime.before(currentTime)) {
                            context.status(400);
                            context.json(new String[]{"Appointment time must be in the future."});
                            return;
                        }
                    } catch (ParseException e) {
                        System.err.println("Failed to parse scheduled time: " + e.getMessage());
                        context.status(400);
                        context.json(new String[]{"Invalid scheduledTime format. Please use the expected format: yyyy-MM-dd HH:mm"});
                        return;
                    }

                    // Check authorization
                    if (authString == null || !dataVal.isUserAuthorized(authString, customer_id)) {
                        System.err.println("Authorization failed for customer ID: " + customer_id);
                        context.status(401);
                        context.json(new String[]{"User is not authorized to perform this action."});
                        return;
                    }

                    // Check technician assignment
                    List<Map<String, Object>> technicianValidation = dbConnector.executeSelectQuery(
                            new String[]{"id"},
                            new String[]{DatabaseConnector.TECHNICIANS},
                            null, "workshop_id = ?", new String[]{workshop_id}
                    );
                    if (technicianValidation.isEmpty()) {
                        System.err.println("No technician found for workshop ID: " + workshop_id);
                        context.status(400);
                        context.json(new String[]{"Error, technician couldn't be assigned."});
                        return;
                    }
                    technician_id = String.valueOf(technicianValidation.get(0).get("id"));

                    // Insert the appointment with try-catch
                    try {
                        boolean insertSuccess = dbConnector.executeInsertQuery(DatabaseConnector.APPOINTMENTS,
                                new String[]{StringNames.customer_id, StringNames.workshop_id, StringNames.service_id,
                                        StringNames.technician_id, StringNames.scheduledTime, StringNames.createdAt,
                                        StringNames.modifiedAt, StringNames.appointmentStatus, StringNames.paymentMethod,
                                        StringNames.paymentStatus},
                                new String[]{customer_id, workshop_id, service_id, technician_id, scheduledTime,
                                        createdAt, modifiedAt, String.valueOf(appointmentStatus), paymentMethod,
                                        String.valueOf(paymentStatus)});

                        if (!insertSuccess) {
                            System.err.println("Failed to insert appointment into database");
                            // Print the actual SQL that would be executed
                            System.err.println("Column names: " + String.join(", ", new String[]{StringNames.customer_id,
                                    StringNames.workshop_id, StringNames.service_id, StringNames.technician_id,
                                    StringNames.scheduledTime, StringNames.createdAt, StringNames.modifiedAt,
                                    StringNames.appointmentStatus, StringNames.paymentMethod, StringNames.paymentStatus}));
                            System.err.println("Values: " + String.join(", ", new String[]{customer_id, workshop_id,
                                    service_id, technician_id, scheduledTime, createdAt, modifiedAt,
                                    String.valueOf(appointmentStatus), paymentMethod, String.valueOf(paymentStatus)}));
                            context.status(500);
                            context.json(new String[]{"Failed to create appointment in database."});
                            return;
                        }

                        // Update tokens after successful appointment creation
                        List<Map<String, Object>> currentTokens = dbConnector.executeSelectQuery(
                            new String[]{"tokens"},
                            new String[]{DatabaseConnector.CUSTOMERS},
                            null,
                            "id = ?",
                            new String[]{customer_id}
                        );

                        int tokens = currentTokens.get(0).get("tokens") != null ? 
                            ((Number) currentTokens.get(0).get("tokens")).intValue() : 0;
                        
                        // Add 1 token for the new appointment
                        dbConnector.executeUpdateQuery(
                            DatabaseConnector.CUSTOMERS,
                            new String[]{"tokens = ?"},
                            new String[]{String.valueOf(tokens + 1)},
                            "id = ?",
                            new String[]{customer_id}
                        );

                        // Return updated user info in response
                        List<Map<String, Object>> updatedUser = dbConnector.executeSelectQuery(
                            new String[]{"id", "firstname", "lastname", "email", "tokens"},
                            new String[]{DatabaseConnector.CUSTOMERS},
                            null,
                            "id = ?",
                            new String[]{customer_id}
                        );

                        context.status(201);
                        context.json(updatedUser);
                    } catch (Exception e) {
                        System.err.println("Database error while creating appointment: " + e.getMessage());
                        e.printStackTrace();
                        context.status(500);
                        context.json(new String[]{"Internal server error while creating appointment."});
                    }
                } else {
                    System.err.println("Missing required parameters");
                    context.status(400);
                    context.json(new String[]{"For creating a new appointment, customer id, " +
                            "workshop id, service id, scheduled time and payment method are required."});
                }
            } catch (Exception e) {
                System.err.println("Unexpected error in appointment creation: " + e.getMessage());
                e.printStackTrace();
                context.status(500);
                context.json(new String[]{"Internal server error: " + e.getMessage()});
            }
        });

        javalinApp.delete("/appointment/delete", context -> {
            String appointment_id = context.queryParam(StringNames.appointment_id);
            String authString = context.header(StringNames.authorization);

            if (appointment_id != null) {
                // data validation
                if (!dataVal.isValidId(appointment_id)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }

                // get all information associated with appointment id
                /*
                 * SELECT *
                 * FROM appointments
                 * WHERE id = appointment_id
                 */
                List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.APPOINTMENTS}, null, "id = ?",
                        new String[]{appointment_id});
                // check for empty result set
                if (result.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Appointment with id " + appointment_id + " not found."});
                    return;
                }

                // check if user is authorized
                String customer_id = String.valueOf(result.get(0).get(StringNames.customer_id));
                if (authString == null || !dataVal.isUserAuthorized(authString, customer_id)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to perform this action."});
                    return;
                }

                // delete appointment
                /*
                 * DELETE FROM appointments
                 * WHERE id = appointment_id
                 */
                dbConnector.executeDeleteQuery(DatabaseConnector.APPOINTMENTS, "id = ?",
                        new String[]{appointment_id});

                context.status(201);
                context.json(new String[]{"Appointment successfully deleted."});
            } else {
                context.status(400);
                context.json(new String[]{"For deleting an appointment, the appointment id is required."});
            }
        });

        javalinApp.put("/appointment/modify", context -> {
            String appointment_id = context.queryParam(StringNames.appointment_id);
            String scheduledTime = context.queryParam(StringNames.scheduledTime);
            String paymentMethod = context.queryParam(StringNames.paymentMethod);
            String authString = context.header(StringNames.authorization);

            if (appointment_id != null && scheduledTime != null && paymentMethod != null) {
                // validate scheduled time data
                Date parsedScheduledTime;
                try {
                    parsedScheduledTime = Utils.yearMonthDayTimeFormat.parse(scheduledTime);
                    
                    // Add validation for current/future time
                    Date currentTime = new Date();
                    if (parsedScheduledTime.before(currentTime)) {
                        context.status(400);
                        context.json(new String[]{"Appointment time must be in the future."});
                        return;
                    }
                } catch (ParseException e) {
                    context.status(400);
                    context.json(new String[]{"Invalid scheduledTime format. Please use the expected format: yyyy-MM-dd HH:mm"});
                    return;
                }
                // data validation
                if (!dataVal.isValidId(appointment_id)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }
                /*
                 * SELECT *
                 * FROM appointments
                 * WHERE id = appointment_id
                 */
                List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"},
                        new String[]{DatabaseConnector.APPOINTMENTS}, null, "id = ?",
                        new String[]{appointment_id});
                // check for empty result set
                if (result.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Appointment with id " + appointment_id + " not found."});
                    return;
                }

                String customer_id = String.valueOf(result.get(0).get(StringNames.customer_id));
                // check if user is authorized
                if (authString == null || !dataVal.isUserAuthorized(authString, customer_id)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to perform this action."});
                    return;
                }

                String service_id = String.valueOf(result.get(0).get(StringNames.service_id));
                // Retrieve the duration of the service from the database table services
                List<Map<String, Object>> getDuration = dbConnector.executeSelectQuery(new String[]{"duration"},
                        new String[]{DatabaseConnector.SERVICES},
                        null, "id = ?", new String[]{service_id}
                );

                Integer serviceDuration = null;
                if (getDuration != null && !getDuration.isEmpty()) {
                    serviceDuration = (Integer) getDuration.get(0).get("duration"); // Retrieve duration from getDuration
                }

                if (serviceDuration == null) {
                    context.status(400).json(new String[]{"Invalid service_id or service not found."});
                    return;
                }

                String workshop_id = String.valueOf(result.get(0).get(StringNames.workshop_id));
                // Retrieve all appointments for a specific workshop and specific service from the database, plus the duration of the service
                List<Map<String, Object>> existingAppointments = dbConnector.executeSelectQuery(
                        new String[]{"a.scheduledTime", "s.duration"},
                        new String[]{DatabaseConnector.APPOINTMENTS, DatabaseConnector.SERVICES},
                        new String[]{"a", "s"},
                        "a.workshop_id = ? and a.service_id = ? and a.service_id = s.id",
                        new String[]{workshop_id, service_id}
                );

                // Check if there are any existing appointments
                if (existingAppointments != null && !existingAppointments.isEmpty()) {
                    for (Map<String, Object> existing : existingAppointments) {
                        // Retrieve the scheduled time and duration of the existing appointment
                        Object scheduledTimeObj = existing.get("scheduledTime");
                        Date existingScheduledTime = null;

                        if (scheduledTimeObj instanceof java.sql.Timestamp) {
                            existingScheduledTime = new Date(((java.sql.Timestamp) scheduledTimeObj).getTime());
                        } else if (scheduledTimeObj instanceof String) {
                            existingScheduledTime = Utils.yearMonthDayTimeFormat.parse((String) scheduledTimeObj);
                        }

                        Integer existingDuration = (Integer) existing.get("duration");

                        if (existingScheduledTime == null || existingDuration == null) {
                            continue;
                        }

                        // Calculate the end time of the existing appointment
                        Date existingEndTime = new Date(existingScheduledTime.getTime() + existingDuration * 60000);

                        // Calculate the end time of the current appointment
                        Date currentEndTime = new Date(parsedScheduledTime.getTime() + serviceDuration * 60000);

                        // Check if there is any overlap
                        if (!(parsedScheduledTime.after(existingEndTime) || currentEndTime.before(existingScheduledTime))) {
                            context.status(400).json(new String[]{"Scheduled time conflicts with an existing appointment."});
                            return;
                        }
                    }
                }
                String modifiedAt = Utils.yearMonthDayTimeFormat.format(new Date()); // Current date and time of the modification

                // data validation
                if (!dataVal.isValidPaymentMethod(paymentMethod)) {
                    context.status(400);
                    context.json(new String[]{"Payment method must be either Cash, Credit/Debit Card, PayPal or ApplePay"});
                    return;
                }

                // update appointment and update modifiedAt date and time
                /*
                 * UPDATE appointments
                 * SET scheduledTime = ?, modifiedAt = ?, paymentMethod = ?
                 * WHERE id = ?
                 */
                boolean updateSuccess = dbConnector.executeUpdateQuery(
                        DatabaseConnector.APPOINTMENTS,
                        new String[]{"scheduledTime = ?", "modifiedAt = ?", "paymentMethod = ?"},
                        new String[]{scheduledTime, modifiedAt, paymentMethod},
                        "id = ?",
                        new String[]{appointment_id}
                );

                if (!updateSuccess) {
                    context.status(500);
                    context.json(new String[]{"Failed to update appointment in database."});
                    return;
                }

                context.status(201);
                context.json(new String[]{"Successfully modified."});

            } else {
                context.status(400);
                context.json(new String[]{"For modifying an appointment, apointment id, valid scheduled time and payment method are required."});
            }

        });

        javalinApp.get("/appointments", context -> {
            String appointment_id = context.queryParam(StringNames.appointment_id);
            String authString = context.header(StringNames.authorization);

            if (appointment_id != null) {
                // data validation
                if (!dataVal.isValidId(appointment_id)) {
                    context.status(400);
                    context.json(new String[]{"Id must be an integer and greater than 0."});
                    return;
                }
                /*
                 * SELECT a.*, w.name as workshopName, s.name as serviceName, t.name as technicianName
                 * FROM appointments as a, workshops as w, services as s, technicians as t
                 * WHERE a.id = appointment_id, a.workshop_id = w.id, a.service_id = s.id, a.technician_id = t.id
                 */
                List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(new String[]{"a.*", "w.name as workshopName", "s.name as serviceName", "t.name as technicianName"},
                        new String[]{DatabaseConnector.APPOINTMENTS, DatabaseConnector.WORKSHOPS, DatabaseConnector.SERVICES, DatabaseConnector.TECHNICIANS}, new String[]{"a", "w", "s", "t"},
                        "a.id = ? and a.workshop_id = w.id and a.service_id = s.id and a.technician_id = t.id",
                        new String[]{appointment_id});

                String customer_id = String.valueOf(queryResult.get(0).get(StringNames.customer_id));
                // check if user is authorized
                if (authString == null || !dataVal.isUserAuthorized(authString, customer_id)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to perform this action."});
                    return;
                }

                // check for empty result set
                if (queryResult.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Appointment with id " + appointment_id + " not found or does not exist."});
                    return;
                }
                context.status(200);
                context.json(queryResult);
            } else {
                context.status(400);
                context.json(new String[]{"For retrieving appointment details, an appointment id must be given."});
            }
        });

        javalinApp.get("/appointments/available", context -> {
            String scheduledTime = context.queryParam("scheduledTime");
            String workshop_id = context.queryParam("workshop_id");
            String service_id = context.queryParam("service_id");

            if (scheduledTime != null && workshop_id != null && service_id != null) {
                //validate data
                if (!dataVal.isValidId(workshop_id) || !dataVal.isValidId(service_id)) {
                    context.status(400);
                    context.json(new String[]{"Id's must be an integer and greater than 0."});
                } else {
                    // validate scheduled time data
                    Date parsedScheduledTime;
                    try {
                        parsedScheduledTime = Utils.yearMonthDayTimeFormat.parse(scheduledTime); // Change to desired format
                    } catch (ParseException e) {
                        context.status(400);
                        context.json(new String[]{"Invalid scheduledTime format. Please use the expected format: yyyy-MM-dd HH:mm"});
                        return;
                    }
                    // Fetch the service duration from the database
                    List<Map<String, Object>> getDuration = dbConnector.executeSelectQuery(
                            new String[]{"duration"},
                            new String[]{DatabaseConnector.SERVICES},
                            null, "id = ?", new String[]{service_id});

                    if (getDuration.isEmpty()) {
                        context.status(400).json(new String[]{"Invalid service_id or service not found."});
                        return;
                    }

                    Integer serviceDuration = (Integer) getDuration.get(0).get("duration");

                    // Extract the date part (yyyy-MM-dd)
                    String dateOnly = scheduledTime.substring(0, 10);

                    // Add working hours
                    String startOfDayString = dateOnly + " 09:00";
                    String endOfDayString = dateOnly + " 17:00";

                    // Parse back to Date objects if needed
                    Date startOfDay;
                    Date endOfDay;
                    try {
                        startOfDay = Utils.yearMonthDayTimeFormat.parse(startOfDayString); // Change to desired format
                        endOfDay = Utils.yearMonthDayTimeFormat.parse(endOfDayString);
                    } catch (ParseException e) {
                        context.status(400);
                        context.json(new String[]{"Invalid (start/end)OfDayformat. Please use the expected format: yyyy-MM-dd HH:mm"});
                        return;
                    }

                    // Fetch existing appointments for the date
                    List<Map<String, Object>> existingAppointments = dbConnector.executeSelectQuery(
                            new String[]{"a.scheduledTime", "s.duration"},
                            new String[]{DatabaseConnector.APPOINTMENTS, DatabaseConnector.SERVICES},
                            new String[]{"a", "s"},
                            "a.workshop_id = ? AND a.service_id = ? AND a.service_id = s.id",
                            new String[]{workshop_id, service_id}
                    );

                    // Generate available slots
                    List<String> availableSlots = new ArrayList<>();
                    Date currentSlot = startOfDay;

                    while (currentSlot.before(endOfDay)) {
                        boolean conflict = false;

                        // Check for conflicts with existing appointments
                        for (Map<String, Object> existing : existingAppointments) {
                            Object existingTimeObj = existing.get("scheduledTime");
                            String existingTimeStr = existingTimeObj.toString(); // Convert to string safely
                            Date existingScheduledTime = Utils.yearMonthDayTimeFormat.parse(existingTimeStr);
                            Integer existingDuration = (Integer) existing.get("duration");
                            Date existingEndTime = new Date(existingScheduledTime.getTime() + existingDuration * 60000);

                            Date currentEndSlot = new Date(currentSlot.getTime() + serviceDuration * 60000);
                            if (!(currentEndSlot.before(existingScheduledTime) || currentSlot.after(existingEndTime))) {
                                conflict = true;
                                break;
                            }
                        }

                        if (!conflict) {
                            availableSlots.add(Utils.yearMonthDayTimeFormat.format(currentSlot));
                        }

                        // Move to the next slot
                        currentSlot = new Date(currentSlot.getTime() + serviceDuration * 60000);
                    }

                    context.status(200);
                    // Respond with the available slots
                    context.json(availableSlots);
                }
            }
            else{
                context.status(400).json(new String[]{"Missing required parameters: scheduledTime, service_id, or workshop_id."});
                return;
            }
        });

        // ------------------------------------------------------------------------------------------------------------------------
        // TECHNICIAN REQUESTS

//        public List<JsonObject> requestTechniciansForService(int workshop_id) {
//            HttpResponse<JsonNode> jsonResponse = Unirest.get("/technicians")
//                    .queryString(StringNames.workshop_id, workshop_id)
//                    .asJson();
//            if (jsonResponse.getStatus() != 200) {
//                return null;
//            }
//            return mapStringToJsonObjectList(jsonResponse.getBody().toString());
//        }

        javalinApp.get("/technicians", context -> {
            String workshop_id = context.queryParam(StringNames.workshop_id);

            if (workshop_id == null) {
                context.status(400);
                context.json(new String[]{"workshop_id must be provided in order to get technicians"});
                return;
            }

            // data validation
            if (!dataVal.isValidId(workshop_id)) {
                context.status(400);
                context.json(new String[]{"Id must be an integer and greater than 0."});
                return;
            }

            List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                new String[]{"id", "name", "certifications", "experience"},
                new String[]{DatabaseConnector.TECHNICIANS},
                null,
                "workshop_id = ?",
                new String[]{workshop_id}
            );

            // check for empty result set
            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"No technicians found for workshop ID: " + workshop_id});
                return;
            }

            context.status(200);
            context.json(queryResult);
        });


        javalinApp.get("/security-questions", context -> {
            
            // Fetch distinct security questions from customers table
            List<Map<String, Object>> queryResult = dbConnector.executeSelectQuery(
                new String[]{"DISTINCT security_question_id, security_question"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "security_question IS NOT NULL",
                null
            );
            
            if (queryResult.isEmpty()) {
                context.status(404);
                context.json(new String[]{"No security questions found."});
                return;
            }

            context.status(200);
            context.json(queryResult);
        });

        // Add endpoint for getting tokens
        javalinApp.get("/customer/tokens", context -> {
            String customer_id = context.queryParam(StringNames.customer_id);
            String authString = context.header(StringNames.authorization);

            if (customer_id != null) {
                if (!dataVal.isUserAuthorized(authString, customer_id)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to perform this action."});
                    return;
                }

                List<Map<String, Object>> result = dbConnector.executeSelectQuery(
                    new String[]{"tokens"},
                    new String[]{DatabaseConnector.CUSTOMERS},
                    null,
                    "id = ?",
                    new String[]{customer_id}
                );

                if (result.isEmpty()) {
                    context.status(404);
                    context.json(new String[]{"Customer not found"});
                    return;
                }

                context.status(200);
                context.json(result);
            }
        });

        // Add endpoint for redeeming tokens
        javalinApp.put("/tokens/redeem", context -> {
            String customer_id = context.queryParam(StringNames.customer_id);
            String tokensToRedeem = context.queryParam(StringNames.tokens);
            String authString = context.header(StringNames.authorization);

            if (customer_id != null && tokensToRedeem != null) {
                if (!dataVal.isUserAuthorized(authString, customer_id)) {
                    context.status(401);
                    context.json(new String[]{"User is not authorized to perform this action."});
                    return;
                }

                List<Map<String, Object>> currentTokens = dbConnector.executeSelectQuery(
                    new String[]{"tokens"},
                    new String[]{DatabaseConnector.CUSTOMERS},
                    null,
                    "id = ?",
                    new String[]{customer_id}
                );

                int availableTokens = currentTokens.get(0).get("tokens") != null ? 
                    ((Number) currentTokens.get(0).get("tokens")).intValue() : 0;
                int requestedTokens = Integer.parseInt(tokensToRedeem);

                if (availableTokens < requestedTokens) {
                    context.status(400);
                    context.json(new String[]{"Not enough tokens available"});
                    return;
                }

                boolean success = dbConnector.executeUpdateQuery(
                    DatabaseConnector.CUSTOMERS,
                    new String[]{"tokens = ?"},
                    new String[]{String.valueOf(availableTokens - requestedTokens)},
                    "id = ?",
                    new String[]{customer_id}
                );

                if (success) {
                    context.status(200);
                    context.json(new String[]{"Tokens redeemed successfully"});
                } else {
                    context.status(500);
                    context.json(new String[]{"Failed to redeem tokens"});
                }
            }
        });
    }

}
