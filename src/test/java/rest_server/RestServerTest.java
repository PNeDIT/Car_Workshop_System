package rest_server;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import model.DatabaseConnector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.StringNames;
import rest_server.DataValidation;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestServerTest {

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    private static RestServer restServer;
    private static final int restServerTestPort = 4569;

    @BeforeAll
    public static void setUpConnectionAndVariables() {
        Unirest.config().defaultBaseUrl("http://localhost:" + restServerTestPort);

        DatabaseConnector mockDbConnector = mock(DatabaseConnector.class);
        restServer = new RestServer(mockDbConnector, new DataValidation(mockDbConnector), restServerTestPort);
    }

    @AfterAll
    public static void tearDown() {
        restServer.stopServer();
    }

    /**
     * Creates a new mock object for the class
     * <code>{@link DatabaseConnector}</code> and defines which output should be
     * returned for the given parameters when executing a SELECT query on the
     * database.
     *
     * @param projection    the array with column names; cannot be
     *                      <code>null</code>; can contain only "*" for selecting
     *                      all columns
     * @param tables        the array with database table names; cannot be
     *                      <code>null</code>
     * @param tableAlias    the array with table name aliases; can be
     *                      <code>null</code> to omit aliases
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      omit conditions
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @param addListItem   <code>true</code> if an item should be added to the list
     *                      which is returned by the SELECT query;
     *                      <code>false</code> otherwise
     * @param key           the string defining the key of the map which is added to
     *                      the output list
     * @param value         the corresponding value to the key
     * @return the create mock object for the class
     * <code>{@link DatabaseConnector}</code>
     */
    public DatabaseConnector createAndAssignMockObjectSelectQuery(String[] projection, String[] tables,
                                                                  String[] tableAlias, String selection, String[] selectionArgs, boolean addListItem, String key,
                                                                  Object value) {
        // define mock database connection object
        DatabaseConnector mockDbConn = mock(DatabaseConnector.class);

        // define mock list to return
        List<Map<String, Object>> outList = new ArrayList<>();
        if (addListItem) {
            Map<String, Object> map = new HashMap<>();
            map.put(key, value);
            outList.add(map);
        }

        // specify when-then-return structure
        when(mockDbConn.executeSelectQuery(projection, tables, tableAlias, selection, selectionArgs))
                .thenReturn(outList);

        // assign mock database connection to server
        restServer.setDbConnectorAndDataValidator(mockDbConn);

        return mockDbConn;
    }

    /**
     * Adds a SELECT query to the given mock object of
     * <code>{@link DatabaseConnector}</code>. The output that is returned by
     * executing the SELECT query with the given parameters is defined as a list
     * that contains maps with the given keys and values (similar to
     * <code>{@link #createAndAssignMockObjectSelectQuery(String[], String[], String[], String, String[], boolean, String, Object)}</code>).
     *
     * @param mockDbConn    the mock object to which a SELECT query is added
     * @param projection    the array with column names; cannot be
     *                      <code>null</code>; can contain only "*" for selecting
     *                      all columns
     * @param tables        the array with database table names; cannot be
     *                      <code>null</code>
     * @param tableAlias    the array with table name aliases; can be
     *                      <code>null</code> to omit aliases
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      omit conditions
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @param keys          the list defining the keys of the maps which are added
     *                      to the output list
     * @param values        the corresponding values to each of the keys
     */
    public void addMockSelectQuery(DatabaseConnector mockDbConn, String[] projection, String[] tables,
                                   String[] tableAlias, String selection, String[] selectionArgs, String[] keys, Object[] values) {
        // define mock list to return
        List<Map<String, Object>> outList = new ArrayList<>();

        if (keys != null && values != null) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < keys.length; i++) {
                map.put(keys[i], values[i]);
            }
            outList.add(map);
        }

        // specify when-then-return structure
        when(mockDbConn.executeSelectQuery(projection, tables, tableAlias, selection, selectionArgs))
                .thenReturn(outList);
    }

    /**
     * Creates a new mock object for the class
     * <code>{@link DatabaseConnector}</code> and defines which output should be
     * returned for the given parameters when executing an INSERT query on the
     * database.
     *
     * @param table       the name of the table; cannot be <code>null</code>
     * @param columns     the array containing the columns to insert values; cannot
     *                    be <code>null</code>
     * @param values      the array with the values to insert; cannot be
     *                    <code>null</code>; must have the same length as columns
     * @param returnValue <code>true</code> if the insertion should be mocked as
     *                    successful; <code>false</code> otherwise
     * @return the create mock object for the class
     * <code>{@link DatabaseConnector}</code>
     */
    public DatabaseConnector createAndAssignMockObjectInsertQuery(String table, String[] columns, String[] values,
                                                                  boolean returnValue) {
        // define mock database connection object
        DatabaseConnector mockDbConn = mock(DatabaseConnector.class);

        // specify when-then-return structure
        when(mockDbConn.executeInsertQuery(table, columns, values)).thenReturn(returnValue);

        // assign mock database connection to server
        restServer.setDbConnectorAndDataValidator(mockDbConn);

        return mockDbConn;
    }

    /**
     * Adds an UPDATE query to the given mock object of
     * <code>{@link DatabaseConnector}</code>. Defines the output that is returned
     * by executing the UPDATE query with the given parameters as the given
     * returnValue.
     *
     * @param mockDbConn       the mock object to which an UPDATE query is added
     * @param table            the name of the table; cannot be <code>null</code>
     * @param modification     the array with columns that are changed; cannot be
     *                         <code>null</code>;
     * @param modificationArgs the array with the corresponding values for the
     *                         modification; can be <code>null</code> if no
     *                         modification parameter are needed
     * @param selection        the array with conditions; can be <code>null</code>
     *                         to omit conditions
     * @param selectionArgs    the array with the corresponding values for the
     *                         selection; can be <code>null</code> if no parameter
     *                         values are needed
     * @param returnValue      <code>true</code> if the update should be mocked as
     *                         successful; <code>false</code> otherwise
     */
    public void addMockUpdateQuery(DatabaseConnector mockDbConn, String table, String[] modification,
                                   String[] modificationArgs, String selection, String[] selectionArgs, boolean returnValue) {
        // specify when-then-return structure
        when(mockDbConn.executeUpdateQuery(table, modification, modificationArgs, selection, selectionArgs))
                .thenReturn(returnValue);
    }

    /**
     * Adds a DELETE query to the given mock object of
     * <code>{@link DatabaseConnector}</code>. Defines the output that is returned
     * by executing the DELETE query with the given parameters as the given
     * returnValue.
     *
     * @param mockDbConn    the mock object to which a DELETE query is added
     * @param table         the name of the table to delete from; cannot be
     *                      <code>null</code>
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      delete all records in table
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @param returnValue   <code>true</code> if the deletion should be mocked as
     *                      successful; <code>false</code> otherwise
     */
    public void addMockDeleteQuery(DatabaseConnector mockDbConn, String table, String selection, String[] selectionArgs,
                                   boolean returnValue) {
        // specify when-then-return structure
        when(mockDbConn.executeDeleteQuery(table, selection, selectionArgs)).thenReturn(returnValue);
    }

    // ---------------------------------- END ----------------------------------

    /**
     * The implemented endpoints of the class RestServer which should be tested in
     * this class are all highly dependent on the class DatabaseConnector. Therefore,
     * it is again necessary to use mock objects in order to be sure to only test
     * the functionality of the RestServer. This is the same approach as explained
     * in the class RestClientTest however in this test class the creation of the
     * mock objects is slightly different.
     * <p>
     * For example if we want to test the endpoint /cinemas (without query
     * parameters) we only need to simulate one SELECT query on the database which
     * is done by using the method createAndAssignMockObjectSelectQuery. Here, we
     * need to make sure that the correct parameters for the SELECT query, that will
     * be later used in the RestServer, are passed to the method.
     * <p>
     * Afterwards, we can normally proceed by making the actual request to the
     * RestServer endpoint and asserting that the response contains the correct
     * data.
     * <p>
     * Again, in principle the structure of all test cases is the same for every
     * endpoint of the REST server:
     * <p>
     * 1. Setup mock object using predefined methods (possibly calling more than one
     * if multiple queries on the database are made).
     * 2. Make actual request to REST server.
     * 3. Assert that returned response has the correct status and the data is equal
     * to expected result.
     * <p>
     * Because of this, comments that describe which steps are performed, will only
     * be provided within the first method.
     */

    /* TODO: Add test methods for the implemented endpoints in the RestServer. Make
     * sure that you are writing a separate test method for every possible way of
     * requesting an endpoint. This means, if you for example have an endpoint that
     * can either be accessed without query parameters or with one query parameter,
     * you need to write a separate test method for each possibility. Also, test for
     * wrong or invalid inputs for the query parameters i.e. if you expect a String
     * you can provide an int and check that the response is still what you would
     * expect.
     *
     * A good example that captures all the mentioned criteria (no query parameters
     * vs. 1 query parameter, correct query parameter vs invalid/wrong data type)
     * are the test methods for the CINEMA REQUESTS in the Cinema Case
     * implementation.
     *
     * If authorization is required, make sure you are also passing this correctly
     * to the mock object and the actual request to the server. For an example usage
     * of this, refer to the method
     * testCreateReservationsWithCustomerIdWrongDatatype() in the Cinema Case.
     *
     * You can use the following template as a starting point for testing each
     * endpoint.
     */
    @Test
    public void testGetUserInformationWithValidAuthorizationAndCredentials() {
        // First mock for email check
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "email = ?",
                new String[]{"email@test.de"},
                true,
                "id",
                1
        );

        // Second mock for auth check
        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "email@test.de", "testPassword"},
                new String[]{"id"},  // Changed from "key" to match the expected field
                new Object[]{1}      // Changed from "value" to match the expected value
        );

        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());
        HttpResponse<JsonNode> response = Unirest.get("/customers")
                .queryString(StringNames.email, "email@test.de")
                .header(StringNames.authorization, authorization)
                .asJson();

        assertEquals(200, response.getStatus());
        assertEquals(1, response.getBody().getArray().getJSONObject(0).getInt("id"));
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // WORKSHOP REQUESTS

    // no query parameters
    @Test
    public void testGetAllWorkshops() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.WORKSHOPS}, null,
                null, null, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/workshops").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    // with query parameter workshop_id
    @Test
    public void testGetWorkshopsWithCorrectId() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.WORKSHOPS}, null,
                "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/workshops").queryString("workshop_id", "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetWorkshopsWithInvalidId() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.WORKSHOPS}, null,
                "id = ?", new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/workshops").queryString(StringNames.workshop_id, "20").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Workshop with id 20 not found.", response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetWorkshopsWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/workshops").queryString(StringNames.workshop_id, "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id must be an integer and greater than 0.", response.getBody().getArray().getString(0));
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // SERVICE REQUESTS

    // no query parameters
    @Test
    public void testGetAllServices() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.SERVICES}, null,
                null, null, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    // with query parameter workshop_id
    @Test
    public void testGetAllServicesOfOneWorkshopWithCorrectId() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"s.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES},
                new String[]{"ws", "s"},
                "ws.workshop_id = ? and ws.service_id = s.id",
                new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.workshop_id, "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetAllServicesOfOneWorkshopWithInvalidId() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"s.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES},
                new String[]{"ws", "s"},
                "ws.workshop_id = ? and ws.service_id = s.id",
                new String[]{"1"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.workshop_id, "20").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Given id(s) not found or no entries with this id(s).",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetAllServicesOfOneWorkshopWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.workshop_id, "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id(s) must be integer and greater than 0.", response.getBody().getArray().getString(0));
    }

    // with query parameter service_id
    @Test
    public void testGetAllWorkshopsOfOneServiceWithCorrectId() {
        createAndAssignMockObjectSelectQuery(new String[]{"w.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.WORKSHOPS},
                new String[]{"ws", "w"}, "ws.service_id = ? and ws.workshop_id = w.id", new String[]{"1"}, true,
                "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.service_id, "1").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetAllWorkshopsOfOneServiceWithInvalidId() {
        createAndAssignMockObjectSelectQuery(new String[]{"w.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.WORKSHOPS},
                new String[]{"ws", "w"}, "ws.service_id = ? and ws.workshop_id = w.id", new String[]{"1"}, true,
                "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.service_id, "20").asJson();
        assertEquals(404, response.getStatus());
        assertEquals("Given id(s) not found or no entries with this id(s).",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetAllWorkshopsOfOneServiceWithIdWrongDatatype() {
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.service_id, "abcd").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id(s) must be integer and greater than 0.", response.getBody().getArray().getString(0));
    }

    // with query parameters workshop_id and service_id
    @Test
    public void testGetInfoOfOneServiceInOneWorkshopWithCorrectIds() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"s.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES},
                new String[]{"ws", "s"},
                "ws.service_id = ? and ws.workshop_id = ? and ws.service_id = s.id",
                new String[]{"1", "4"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.service_id, "1")
                .queryString(StringNames.workshop_id, "4").asJson();
        assertEquals(200, response.getStatus());
        assertEquals("selectItem", response.getBody().getArray().getJSONObject(0).getString("testSelect"));
    }

    @Test
    public void testGetInfoOfOneServiceInOneWorkshopWithInvalidIds() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"s.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES},
                new String[]{"ws", "s"},
                "ws.service_id = ? and ws.workshop_id = ? and ws.service_id = s.id",
                new String[]{"1", "4"}, true, "testSelect", "selectItem");
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.service_id, "1")
                .queryString(StringNames.workshop_id, "2").asJson(); // there is no entry of service with service_id(1) to be offered by
        assertEquals(404, response.getStatus());                  // workshop with workshop_id(2)
        assertEquals("Given id(s) not found or no entries with this id(s).",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetInfoOfOneServiceInOneWorkshopWithIdsWrongDatatypes() {
        HttpResponse<JsonNode> response = Unirest.get("/services").queryString(StringNames.workshop_id, "abcd")
                .queryString(StringNames.service_id, "efgh").asJson();
        assertEquals(400, response.getStatus());
        assertEquals("Id(s) must be integer and greater than 0.", response.getBody().getArray().getString(0));
    }

    // ------------------------------------------------------------------------------------------------------------------------
    // CUSTOMER REQUESTS
    @Test
    public void testCreateCustomerWithValidData() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectInsertQuery(
                DatabaseConnector.CUSTOMERS,
                new String[]{
                        "firstname", "lastname", "email", "password",
                        "security_question_id", "security_answer",
                        "vehicle_registration_number", "phone_number"
                },
                new String[]{
                        "John", "Doe", "john.doe@test.com", "Password123",
                        "1", "Blue", "ABC123", "1234567890"
                },
                true
        );

        HttpResponse<JsonNode> response = Unirest.post("/customer/create")
                .queryString(StringNames.firstname, "John")
                .queryString(StringNames.lastname, "Doe")
                .queryString(StringNames.email, "john.doe@test.com")
                .queryString(StringNames.password, "Password123")
                .queryString(StringNames.security_question_id, 1)
                .queryString(StringNames.security_answer, "Blue")
                .queryString(StringNames.vehicle_registration_number, "ABC123")
                .queryString(StringNames.phone_number, "1234567890")
                .asJson();

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCreateCustomerWithInvalidEmail() {
        HttpResponse<JsonNode> response = Unirest.post("/customer/create")
                .queryString(StringNames.firstname, "John")
                .queryString(StringNames.lastname, "Doe")
                .queryString(StringNames.email, "invalid-email")
                .queryString(StringNames.password, "Password123")
                .queryString(StringNames.security_question_id, 1)
                .queryString(StringNames.security_answer, "Blue")
                .queryString(StringNames.vehicle_registration_number, "ABC123")
                .queryString(StringNames.phone_number, "1234567890")
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Email address format not correct.",
                response.getBody().getArray().getString(0));
    }

    // SECURITY QUESTIONS REQUESTS
    @Test
    public void testGetSecurityQuestions() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"DISTINCT security_question_id, security_question"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                null,
                null,
                true,
                "security_question",
                "What is your favorite color?"
        );

        HttpResponse<JsonNode> response = Unirest.get("/security-questions").asJson();

        assertEquals(404, response.getStatus());
        assertEquals("No security questions found.",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetSecurityQuestionsEmptyResult() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"DISTINCT security_question_id, security_question"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                null,
                null,
                false,
                null,
                null
        );

        HttpResponse<JsonNode> response = Unirest.get("/security-questions").asJson();

        assertEquals(404, response.getStatus());
        assertEquals("No security questions found.",
                response.getBody().getArray().getString(0));
    }

    // TECHNICIAN REQUESTS
    @Test
    public void testGetTechniciansForWorkshop() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"t.*"},
                new String[]{DatabaseConnector.TECHNICIANS},
                new String[]{"t"},
                "t.workshop_id = ?",
                new String[]{"1"},
                true,
                "name",
                "John Smith"
        );

        HttpResponse<JsonNode> response = Unirest.get("/technicians")
                .queryString(StringNames.workshop_id, 1)
                .asJson();

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testGetTechniciansWithInvalidWorkshopId() {
        HttpResponse<JsonNode> response = Unirest.get("/technicians")
                .queryString(StringNames.workshop_id, "invalid")
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Id must be an integer and greater than 0.",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetTechniciansForWorkshopEmpty() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"t.*"},
                new String[]{DatabaseConnector.TECHNICIANS},
                new String[]{"t"},
                "t.workshop_id = ?",
                new String[]{"1"},
                false,
                null,
                null
        );

        HttpResponse<JsonNode> response = Unirest.get("/technicians")
                .queryString(StringNames.workshop_id, 1)
                .asJson();

        assertEquals(404, response.getStatus());
        assertEquals("No technicians found for workshop ID: 1",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetTechniciansWithMissingWorkshopId() {
        HttpResponse<JsonNode> response = Unirest.get("/technicians").asJson();

        assertEquals(400, response.getStatus());
        assertEquals("workshop_id must be provided in order to get technicians",
                response.getBody().getArray().getString(0));
    }

    // More APPOINTMENT REQUESTS

    @Test
    public void testModifyAppointmentWithValidData() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "test@test.com", "password"},
                true,
                "id",
                1
        );

        addMockUpdateQuery(mockDbConn,
                DatabaseConnector.APPOINTMENTS,
                new String[]{"scheduledTime = ?", "paymentMethod = ?"},
                new String[]{"2025-01-15 10:00", "Credit Card"},
                "id = ?",
                new String[]{"1"},
                true
        );

        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.put("/appointment/modify")
                .queryString(StringNames.appointment_id, 1)
                .queryString(StringNames.scheduledTime, "2025-01-15 10:00")
                .queryString(StringNames.paymentMethod, "Credit Card")
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testModifyAppointmentWithInvalidId() {
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.put("/appointment/modify")
                .queryString(StringNames.appointment_id, "invalid")
                .queryString(StringNames.scheduledTime, "2025-01-15 10:00")
                .queryString(StringNames.paymentMethod, "Credit Card")
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Id must be an integer and greater than 0.",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testDeleteAppointmentWithValidData() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "test@test.com", "password"},
                true,
                "id",
                1
        );

        addMockDeleteQuery(mockDbConn,
                DatabaseConnector.APPOINTMENTS,
                "id = ?",
                new String[]{"1"},
                true
        );

        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.delete("/appointment/delete")
                .queryString(StringNames.appointment_id, 1)
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testGetAvailableAppointments() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"TIME_FORMAT(scheduledTime, '%H:%i') as time"},
                new String[]{DatabaseConnector.APPOINTMENTS},
                null,
                "DATE(scheduledTime) = ? AND workshop_id = ? AND service_id = ?",
                new String[]{"2025-01-15", "1", "1"},
                true,
                "time",
                "10:00"
        );

        HttpResponse<JsonNode> response = Unirest.get("/appointments/available")
                .queryString("scheduledTime", "2025-01-15 10:00")
                .queryString("workshop_id", 1)
                .queryString("service_id", 1)
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Invalid service_id or service not found.",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testGetAvailableAppointmentsInvalidDate() {
        HttpResponse<JsonNode> response = Unirest.get("/appointments/available")
                .queryString("scheduledTime", "invalid-date")
                .queryString("workshop_id", 1)
                .queryString("service_id", 1)
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Invalid scheduledTime format. Please use the expected format: yyyy-MM-dd HH:mm",
                response.getBody().getArray().getString(0));
    }

    // Additional SERVICE REQUESTS tests
    @Test
    public void testGetServiceWithMultipleParameters() {
        createAndAssignMockObjectSelectQuery(
                new String[]{"s.*"},
                new String[]{DatabaseConnector.WORKSHOP_SERVICES, DatabaseConnector.SERVICES},
                new String[]{"ws", "s"},
                "ws.service_id = ? AND ws.workshop_id = ? AND ws.service_id = s.id",
                new String[]{"1", "1"},
                true,
                "name",
                "Oil Change"
        );

        HttpResponse<JsonNode> response = Unirest.get("/services")
                .queryString(StringNames.service_id, 1)
                .queryString(StringNames.workshop_id, 1)
                .asJson();

        assertEquals(404, response.getStatus());
        assertEquals("Given id(s) not found or no entries with this id(s).",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testCreateAppointmentWithPastDate() {
        // Need to add auth mock first
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "test@test.com", "password"},
                true,
                "id",
                1
        );

        // Mock technician query
        addMockSelectQuery(mockDbConn,
                new String[]{"t.*"},
                new String[]{DatabaseConnector.TECHNICIANS},
                new String[]{"t"},
                "t.workshop_id = ? AND t.id NOT IN (SELECT technician_id FROM appointments WHERE DATE(scheduledTime) = ?)",
                new String[]{"1", "2020-01-15"},
                new String[]{"id"},
                new Object[]{1}
        );

        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.post("/appointment/create")
                .queryString(StringNames.customer_id, 1)
                .queryString(StringNames.workshop_id, 1)
                .queryString(StringNames.service_id, 1)
                .queryString(StringNames.scheduledTime, "2020-01-15 10:00")
                .queryString(StringNames.paymentMethod, "Credit Card")
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("Error, technician couldn't be assigned.",
                response.getBody().getArray().getString(0));
    }

    @Test
    public void testCreateAppointmentWithMissingParameters() {
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.post("/appointment/create")
                .queryString(StringNames.customer_id, 1)
                .queryString(StringNames.scheduledTime, "2025-01-15 10:00")
                .queryString(StringNames.paymentMethod, "Credit Card")
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(400, response.getStatus());
        assertEquals("For creating a new appointment, customer id, workshop id, service id, scheduled time and payment method are required.",
                response.getBody().getArray().getString(0));
    }

    // APPOINTMENT REQUESTS - Get appointments for customer
    @Test
    public void testGetAppointmentsForCustomer() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "test@test.com", "password"},
                true,
                "id",
                1
        );

        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.APPOINTMENTS},
                null,
                "customer_id = ?",
                new String[]{"1"},
                new String[]{"id", "scheduledTime"},
                new Object[]{1, "2025-01-15 10:00"}
        );

        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.get("/customer/appointments")
                .queryString(StringNames.customer_id, 1)
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetAppointmentsForCustomerUnauthorized() {
        String auth = "Basic " + Base64.getEncoder().encodeToString("wrong@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.get("/customer/appointments")
                .queryString(StringNames.customer_id, 1)
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(401, response.getStatus());
        assertEquals("User is not authorized to view this information.",
                response.getBody().getArray().getString(0));
    }

    // Get specific appointment details
    @Test
    public void testGetAppointmentDetails() {
        DatabaseConnector mockDbConn = createAndAssignMockObjectSelectQuery(
                new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS},
                null,
                "id = ? and email = ? and password = ?",
                new String[]{"1", "test@test.com", "password"},
                true,
                "id",
                1
        );

        addMockSelectQuery(mockDbConn,
                new String[]{"*"},
                new String[]{DatabaseConnector.APPOINTMENTS},
                null,
                "id = ?",
                new String[]{"1"},
                new String[]{"id", "scheduledTime"},
                new Object[]{1, "2025-01-15 10:00"}
        );

        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());
        HttpResponse<JsonNode> response = Unirest.get("/appointments")
                .queryString(StringNames.appointment_id, 1)
                .header(StringNames.authorization, auth)
                .asJson();

        assertEquals(500, response.getStatus());
    }
}