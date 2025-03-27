package rest_client;

import com.google.gson.JsonObject;
import kong.unirest.*;
import model.User;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.StringNames;
import utils.Utils;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(MockitoExtension.class)
public class
RestClientTest {

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    private static final RestClient client = RestClient.getRestClient(true);
    @Mock(lenient = true)
    JsonNode value;
    @Mock(lenient = true)
    private GetRequest getRequest;
    @Mock(lenient = true)
    private HttpRequestWithBody requestWithBody;
    @Mock(lenient = true)
    private HttpResponse<JsonNode> httpResponse;
    private static MockedStatic<Unirest> mockedUnirest;

    @BeforeAll
    public static void initMock() {
        mockedUnirest = mockStatic(Unirest.class);
    }

    @AfterAll
    public static void deregisterMock() { mockedUnirest.close();}
    // ---------------------------------- END ----------------------------------

    /**
     * As you know from the lecture, unit testing aims at only testing one component
     * at a time. The methods of the RestClient class are highly dependent on the
     * implementation of the RestServer. This means, if we are testing one method of
     * RestClient, we are actually also testing the correct behavior of the
     * RestServer. In order to only test for the correct functionality of a
     * RestClient method, we can simulate the RestServer, so that we know what it
     * will always return as a response.
     * <p>
     * This simulation can be achieved by the usage of so-called mock objects. A
     * mock object simulates a specific method that is used in the method which we
     * want to test. The behavior of this mock object is set before it is actually
     * used, and therefore we can always be sure what the result of the execution of
     * this simulated method will be. This is just a very brief explanation of the
     * concept of mock objects. For further information you can refer for example to
     * https://www.vogella.com/tutorials/Mockito/article.html
     * <p>
     * In our case this means, we are creating a mock object, to simulate the
     * response of the server for a specific endpoint. For example if we want to
     * test the method restClient.requestCinemas() we only need to simulate the
     * endpoint /cinemas of the RestServer because this is the only endpoint that is
     * used (this is performed by using the when(...).thenReturn(...) structure as
     * shown in all test cases below).
     * <p>
     * After creating the mock object, we can then perform the actual test by
     * calling the method restClient.requestCinemas() and asserting afterwards that
     * the result returned by the mock object is equal to the result we expect.
     * <p>
     * In principle the structure of all test cases is the same for every REST
     * client method:
     * <p>
     * 1. Setup mock object using when and thenReturn. <br>
     * 2. Make actual request to REST client. <br>
     * 3. Assert that returned result is equal to expected result.
     * <p>
     * Because of this, comments that describe which steps are performed, will only
     * be provided within the first method.
     */

    /* TODO: Add test methods for the implemented methods of the class RestClient.
     * Make sure that you are writing a separate test method for every possible way
     * of requesting a method. This normally means, for one method you have to write
     * two test cases: the first case tests for a successful response of the
     * RestServer i.e. the server returns a status of 200/201 and the second case
     * tests for an unsuccessful response of the RestServer i.e. the server returns
     * a status of 400/401/404.
     *
     * A good example that shows the necessary test cases for one RestClient method
     * are the test methods in the section CINEMA REQUESTS of the Cinema Case
     * implementation.
     *
     * If authorization is required, make sure you are also passing this correctly
     * to the mock object and the actual request to the RestClient. For an example
     * usage of this, refer to the method testCreateNewReservationStatus201() in the
     * Cinema Case implementation.
     *
     * The creation of the mock object is different if you are testing a method that
     * uses the GET method than if you use the PUT, POST or DELETE method.
     * Therefore, there are two different templates which show you how to create the
     * mock object in each case. You can use the following templates as a starting
     * point for testing your RestClient methods.
     */

    @BeforeEach
    public void setup() {
        // Mock Unirest static methods
        mockedUnirest.when(() -> Unirest.post(anyString())).thenReturn(requestWithBody);
        mockedUnirest.when(() -> Unirest.put(anyString())).thenReturn(requestWithBody);
        mockedUnirest.when(() -> Unirest.get(anyString())).thenReturn(getRequest);
        mockedUnirest.when(() -> Unirest.delete(anyString())).thenReturn(requestWithBody);

        // Mock chained calls
        when(requestWithBody.queryString(anyString(), any())).thenReturn(requestWithBody);
        when(requestWithBody.header(anyString(), anyString())).thenReturn(requestWithBody);
        when(getRequest.queryString(anyString(), any())).thenReturn(getRequest);
        when(getRequest.header(anyString(), anyString())).thenReturn(getRequest);

        // Mock asJson() calls
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(getRequest.asJson()).thenReturn(httpResponse);
    }

    @Test
    public void testCaseTemplateWithGet() {
        // testGetUserInfoByMailStatus401 in CinemaCase
        String email = "noah.const@web.de";
        client.setUser(new User(email, "9ikwelf%"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("noah.const@web.de:9ikwelf%".getBytes());

        when(Unirest.get("/customers")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.email, email)).thenReturn(getRequest);
        when(getRequest.header(StringNames.authorization, auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(401);

        assertNull(client.getUserInfoByMail(email));
    }

    @Test
    public void testCreateNewUserStatus201() {
        // Tests successful user registration with status 201
        String firstname = "Max";
        String lastname = "Mustermann";
        String email = "max.mt@web.de";
        String password = "dieSonne";
        int security_question_id = 1;
        String security_answer = "Blue";
        String vehicle_registration_number = "ABC123";
        String phone_number = "1234567890";

        when(Unirest.post("/customer/create")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.firstname, firstname)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.lastname, lastname)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.email, email)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.password, password)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.security_question_id, security_question_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.security_answer, security_answer)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.vehicle_registration_number, vehicle_registration_number)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.phone_number, phone_number)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(201);

        assertTrue(client.createNewUser(firstname, lastname, email, password, security_question_id,
                security_answer, vehicle_registration_number, phone_number));
    }

    @Test
    public void testCreateNewUserStatus400() {
        // Tests failed user registration with status 400
        String firstname = "Max";
        String lastname = "Mustermann";
        String email = "max.mt@web.de";
        String password = "dieSonne";
        int security_question_id = 1;
        String security_answer = "Blue";
        String vehicle_registration_number = "ABC123";
        String phone_number = "1234567890";

        // Need to mock Unirest.post first
        when(Unirest.post("/customer/create")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.firstname, firstname)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.lastname, lastname)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.email, email)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.password, password)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.security_question_id, security_question_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.security_answer, security_answer)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.vehicle_registration_number, vehicle_registration_number)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.phone_number, phone_number)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertFalse(client.createNewUser(firstname, lastname, email, password, security_question_id,
                security_answer, vehicle_registration_number, phone_number));
    }

// tests for workshop requests----------------------------------------------------------------------------------------


    // test method requestAllWorkshops
    @Test
    public void testRequestAllWorkshops() {
        // Tests retrieving list of all workshops
        //set up mock object
        when(Unirest.get("/workshops")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString())
                .thenReturn("[{\"location\":\"101 Maple Boulevard\",\"name\":\"CarCare Center\",\"contact info\":\"support@carcare.com\"}]");

        //Make actual request to REST client
        List<JsonObject> result = client.requestAllWorkshops();

        //Assert that returned result is equal to expected result
        assertEquals(1, result.size());
        assertEquals("101 Maple Boulevard", result.get(0).get("location").getAsString());
        assertEquals("CarCare Center", result.get(0).get("name").getAsString());
        assertEquals("support@carcare.com", result.get(0).get("contact info").getAsString());
    }

    @Test
    public void testRequestAllWorkshopsStatus400() {
        when(Unirest.get("/workshops")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        List<JsonObject> result = client.requestAllWorkshops();
        assertTrue(result.isEmpty());
    }


    // test for the case that method requestWorkshopInformation was successful (returns status code 200)

    @Test
    public void testRequestWorkshopInformationStatus200() {
        // Tests retrieving specific workshop details successfully
        int workshop_id = 4;
        when(Unirest.get("/workshops")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString())
                .thenReturn("[{\"location\":\"101 Maple Boulevard\",\"name\":\"CarCare Center\",\"contact info\":\"support@carcare.com\"}]");
        when(httpResponse.getStatus()).thenReturn(200);

        List<JsonObject> result = client.requestWorkshopInformation(workshop_id);
        assertEquals("101 Maple Boulevard", result.get(0).get("location").getAsString());
        assertEquals("CarCare Center", result.get(0).get("name").getAsString());
        assertEquals("support@carcare.com", result.get(0).get("contact info").getAsString());
    }

    // test for the case that an invalid request has been made when using the method requestWorkshopInformation (return 400)
    @Test
    public void testRequestWorkshopInformationStatus400() {
        int workshop_id = 4;
        when(Unirest.get("/workshops")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.requestWorkshopInformation(workshop_id));
    }

//tests for service requests--------------------------------------------------------------------------------------------

// test for the case that method requestAllServicesOfWorkshop was successful (returns status code 200)


    @Test
public void testRequestAllServicesOfWorkshopStatus200() {
        // Tests retrieving all services for a specific workshop
    int workshop_id = 4;
    when(Unirest.get("/services")).thenReturn(getRequest);
    when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
    when(getRequest.asJson()).thenReturn(httpResponse);
    when(httpResponse.getBody()).thenReturn(value);
    when(value.toString()).thenReturn(
            "[{\"name\":\"Oil Change\",\"duration\":45,\"price\":49.99,\"description\":\"Change the engine oil and replace the filter.\",\"pickup_Available\":1,\"promotion_id\":2}]");
    when(httpResponse.getStatus()).thenReturn(200);

    List<JsonObject> result = client.requestAllServicesOfWorkshop(workshop_id);
    assertEquals(1, result.size());
    assertEquals("Oil Change", result.get(0).get("name").getAsString());
    assertEquals(45, result.get(0).get("duration").getAsInt());
    assertEquals(49.99, result.get(0).get("price").getAsDouble());
    assertEquals("Change the engine oil and replace the filter.", result.get(0).get("description").getAsString());
    assertEquals(1, result.get(0).get("pickup_Available").getAsInt());
    assertEquals(2, result.get(0).get("promotion_id").getAsInt());
}

// test for the case that method requestAllServicesOfWorkshop failed (returns status code 400)

    @Test
public void testRequestAllServicesOfWorkshopStatus400() {
    int workshop_id = 4;
    when(Unirest.get("/services")).thenReturn(getRequest);
    when(getRequest.queryString(StringNames.workshop_id,workshop_id)).thenReturn(getRequest);
    when(getRequest.asJson()).thenReturn(httpResponse);
    when(httpResponse.getStatus()).thenReturn(400);

    assertNull(client.requestAllServicesOfWorkshop(workshop_id));
}

// test for the case that method requestServiceInformationOfWorkshop was successful (returns status code 200)

    @Test
    public void testRequestServiceInformationOfWorkshopStatus200() {
        // Tests retrieving specific service details from a workshop
        int workshop_id = 4;
        int service_id = 1;

        when(Unirest.get("/services")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.service_id, service_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn(
                "[{\"name\":\"Oil Change\",\"duration\":45,\"price\":49.99,\"description\":\"Change the engine oil and replace the filter.\",\"pickup_Available\":1,\"promotion_id\":2}]"
        );

        List<JsonObject> result = client.requestServiceInformationOfWorkshop(workshop_id, service_id);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Oil Change", result.get(0).get("name").getAsString());
        assertEquals(45, result.get(0).get("duration").getAsInt());
        assertEquals(49.99, result.get(0).get("price").getAsDouble(), 0.01);
    }


    // test for the case that method requestServiceInformationOfWorkshop failed (returns status code 400)
    @Test
    public void testRequestServiceInformationOfWorkshopStatus400() {
        int workshop_id = 4;
        int service_id = 1;
        when(Unirest.get("/services")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.service_id, service_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.requestServiceInformationOfWorkshop( workshop_id, service_id));
    }

    // Test for the case that the method getAppointmentsForUser was successful (returns status code 200)
    @Test
    public void testGetAppointmentsForUserStatus200() throws UnirestException {
        // Tests retrieving user's appointments successfully
        client.setUser(new User("test@test.com", "password"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.get("/customer/appointments")).thenReturn(getRequest);
        when(getRequest.queryString(eq(StringNames.customer_id), anyInt())).thenReturn(getRequest);
        when(getRequest.header(eq(StringNames.authorization), anyString())).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"id\":1,\"scheduledTime\":\"2025-01-15 10:00\"}]");

        List<JsonObject> result = client.getAppointmentsForUser();
        assertNotNull(result);
    }

    // Test for the case that the method getAppointmentsForUser failed (returns status code 400)
    @Test
    public void testGetAppointmentsForUserStatus400() throws UnirestException {
        client.setUser(new User("test@test.com", "password"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.get("/customer/appointments")).thenReturn(getRequest);
        when(getRequest.queryString(eq(StringNames.customer_id), anyInt())).thenReturn(getRequest);
        when(getRequest.header(eq(StringNames.authorization), anyString())).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertNull(client.getAppointmentsForUser());
    }

    // Test for the case that the method testGetAppointmentDetailsStatus200 was successful (returns status code 200)

    @Test
    public void testGetAppointmentDetailsStatus200() throws UnirestException {
        int appointment_id = 10;
        client.setUser(new User("test@test.com", "password"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.get("/appointments")).thenReturn(getRequest);
        when(getRequest.queryString("appointment_id", appointment_id)).thenReturn(getRequest);
        when(getRequest.header("Authorization", auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"appointment_id\":10,\"date\":\"2025-02-15\",\"time\":\"03:00 PM\",\"service\":\"Car Inspection\",\"workshop_id\":7}]");

        List<JsonObject> result = client.getAppointmentDetails(appointment_id);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).get("appointment_id").getAsInt());
        assertEquals("2025-02-15", result.get(0).get("date").getAsString());
    }

    // Test for the case that the method testGetAppointmentDetailsStatus400 failed (returns status code 400)

    @Test
    public void testGetAppointmentDetailsStatus400() throws UnirestException {
        int appointment_id = 10;
        client.setUser(new User("test@test.com", "password"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.get("/appointments")).thenReturn(getRequest);
        when(getRequest.queryString("appointment_id", appointment_id)).thenReturn(getRequest);
        when(getRequest.header("Authorization", auth)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        List<JsonObject> result = client.getAppointmentDetails(appointment_id);
        assertNull(result);
    }

    @Test
    public void testRequestTechniciansForServiceStatus200() {
        // Tests retrieving technician information for a service
        int workshop_id = 1;

        when(Unirest.get("/technicians")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"id\":1,\"name\":\"John Smith\",\"certifications\":\"ASE Certified\",\"experience\":5}]");

        List<JsonObject> result = client.requestTechniciansForService(workshop_id);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Smith", result.get(0).get("name").getAsString());
        assertEquals("ASE Certified", result.get(0).get("certifications").getAsString());
        assertEquals(5, result.get(0).get("experience").getAsInt());
    }

    @Test
    public void testRequestTechniciansForServiceStatus400() {
        int workshop_id = 1;

        when(Unirest.get("/technicians")).thenReturn(getRequest);
        when(getRequest.queryString(StringNames.workshop_id, workshop_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        List<JsonObject> result = client.requestTechniciansForService(workshop_id);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetSecurityQuestionsStatus200() {
        // Tests retrieving security questions for user registration
        when(Unirest.get("/security-questions")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[{\"security_question_id\":1,\"security_question\":\"What is your favorite color?\"}]");

        List<JsonObject> result = client.getSecurityQuestions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).get("security_question_id").getAsInt());
        assertEquals("What is your favorite color?", result.get(0).get("security_question").getAsString());
    }

    @Test
    public void testGetSecurityQuestionsStatus400() {
        when(Unirest.get("/security-questions")).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        List<JsonObject> result = client.getSecurityQuestions();

        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreateNewAppointmentStatus201() throws ParseException {
        // Tests successful appointment creation
        int workshop_id = 1;
        int service_id = 1;
        Date scheduledTime = Utils.yearMonthDayTimeFormat.parse("2025-01-15 10:00");
        String paymentMethod = "Credit Card";

        client.setUser(new User("test@test.com", "password"));
        client.getUser().setId(1);
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.post("/appointment/create")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.customer_id, 1)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.workshop_id, workshop_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.service_id, service_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.scheduledTime, "2025-01-15 10:00")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.paymentMethod, paymentMethod)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(201);

        assertTrue(client.createNewAppointment(workshop_id, service_id, scheduledTime, paymentMethod));
    }

    @Test
    public void testCreateNewAppointmentStatus400() throws ParseException {
        int workshop_id = 1;
        int service_id = 1;
        Date scheduledTime = Utils.yearMonthDayTimeFormat.parse("2025-01-15 10:00");
        String paymentMethod = "Credit Card";

        client.setUser(new User("test@test.com", "password"));
        client.getUser().setId(1);

        // Need to mock the Unirest.post call first
        when(Unirest.post("/appointment/create")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.customer_id, client.getUser().getId())).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.workshop_id, workshop_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.service_id, service_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.scheduledTime, Utils.yearMonthDayTimeFormat.format(scheduledTime))).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.paymentMethod, paymentMethod)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, client.getUser().getAuthorization())).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertFalse(client.createNewAppointment(workshop_id, service_id, scheduledTime, paymentMethod));
    }

    @Test
    public void testModifyAppointmentStatus201() throws ParseException {
        // Tests successful appointment modification
        int appointment_id = 1;
        Date scheduledTime = Utils.yearMonthDayTimeFormat.parse("2025-01-15 10:00");
        String paymentMethod = "Credit Card";

        client.setUser(new User("test@test.com", "password"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        // Create a mock JsonNode for the response body
        JsonNode mockJsonNode = mock(JsonNode.class);
        when(mockJsonNode.toString()).thenReturn("{}");

        when(Unirest.put("/appointment/modify")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.appointment_id, appointment_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.scheduledTime, "2025-01-15 10:00")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.paymentMethod, paymentMethod)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(201);
        when(httpResponse.getBody()).thenReturn(mockJsonNode);  // Use mock JsonNode instead of null

        assertTrue(client.modifyAppointment(appointment_id, scheduledTime, paymentMethod));
    }

    @Test
    public void testModifyAppointmentStatus400() throws ParseException {
        int appointment_id = 1;
        Date scheduledTime = Utils.yearMonthDayTimeFormat.parse("2025-01-15 10:00");
        String paymentMethod = "Credit Card";

        client.setUser(new User("test@test.com", "password"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        // Create a mock JsonNode for the response body
        JsonNode mockJsonNode = mock(JsonNode.class);
        when(mockJsonNode.toString()).thenReturn("{}");

        when(Unirest.put("/appointment/modify")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.appointment_id, appointment_id)).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.scheduledTime, "2025-01-15 10:00")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.paymentMethod, paymentMethod)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody()).thenReturn(mockJsonNode);  // Use mock JsonNode instead of null

        assertFalse(client.modifyAppointment(appointment_id, scheduledTime, paymentMethod));
    }

    @Test
    public void testGetAvailableAppointmentsStatus200() throws ParseException, UnirestException {
        // Tests retrieving available appointment slots
        int workshop_id = 1;
        int service_id = 1;
        Date scheduledTime = Utils.yearMonthDayTimeFormat.parse("2025-01-15 10:00");

        when(Unirest.get("/appointments/available")).thenReturn(getRequest);
        when(getRequest.queryString("scheduledTime", "2025-01-15 10:00")).thenReturn(getRequest);
        when(getRequest.queryString("workshop_id", workshop_id)).thenReturn(getRequest);
        when(getRequest.queryString("service_id", service_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(200);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[\"10:00\",\"11:00\",\"12:00\"]");

        List<String> result = client.getAvailableAppointments(scheduledTime, workshop_id, service_id);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("10:00", result.get(0));
    }

    @Test
    public void testGetAvailableAppointmentsStatus400() throws ParseException, UnirestException {
        int workshop_id = 1;
        int service_id = 1;
        Date scheduledTime = Utils.yearMonthDayTimeFormat.parse("2025-01-15 10:00");

        when(Unirest.get("/appointments/available")).thenReturn(getRequest);
        when(getRequest.queryString("scheduledTime", Utils.yearMonthDayTimeFormat.format(scheduledTime))).thenReturn(getRequest);
        when(getRequest.queryString("workshop_id", workshop_id)).thenReturn(getRequest);
        when(getRequest.queryString("service_id", service_id)).thenReturn(getRequest);
        when(getRequest.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);
        when(httpResponse.getBody()).thenReturn(value);
        when(value.toString()).thenReturn("[]");

        List<String> result = client.getAvailableAppointments(scheduledTime, workshop_id, service_id);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteAppointmentStatus201() {
        // Tests successful appointment deletion
        int appointment_id = 1;
        client.setUser(new User("test@test.com", "password"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.delete("/appointment/delete")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.appointment_id, appointment_id)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(201);

        assertTrue(client.deleteAppointment(appointment_id));
    }

    @Test
    public void testDeleteAppointmentStatus400() {
        int appointment_id = 1;
        client.setUser(new User("test@test.com", "password"));
        String auth = "Basic " + Base64.getEncoder().encodeToString("test@test.com:password".getBytes());

        when(Unirest.delete("/appointment/delete")).thenReturn(requestWithBody);
        when(requestWithBody.queryString(StringNames.appointment_id, appointment_id)).thenReturn(requestWithBody);
        when(requestWithBody.header(StringNames.authorization, auth)).thenReturn(requestWithBody);
        when(requestWithBody.asJson()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(400);

        assertFalse(client.deleteAppointment(appointment_id));
    }

}