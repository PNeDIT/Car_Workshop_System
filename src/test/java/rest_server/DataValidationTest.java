package rest_server;

import model.DatabaseConnector;

import java.util.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataValidationTest {

    // ------------- DO NOT CHANGE THE FOLLOWING PART OF THE CODE -------------
    private static DataValidation dataVal;

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
     */
    public void createAndAssignMockObjectSelectQuery(String[] projection, String[] tables, String[] tableAlias,
                                                     String selection, String[] selectionArgs, boolean addListItem,
                                                     String key, Object value) {
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

        // assign mock database connection
        dataVal = new DataValidation(mockDbConn);

    }
    // ---------------------------------- END ----------------------------------

    /**
     * The usage of mock objects in this test class is the same as in
     * <code>{@link RestServerTest}</code>. For more information on the usage of
     * mock objects refer to this class.
     */

    /* TODO: Implement further test cases for testing for the correct behavior of
     * your newly implemented methods from the class {@link DataValidation}. Make
     * sure you implement test cases for all possible inputs for the current method.
     *
     * Leave the rest of the test methods untouched as you might want to use them as
     * a reference for implementing your own test cases.
     */

    @Test
    public void testIsValidId() {
        assertFalse(dataVal.isValidId(null));
        assertFalse(dataVal.isValidId(""));
        assertFalse(dataVal.isValidId("0"));
        assertFalse(dataVal.isValidId("-5"));

        assertTrue(dataVal.isValidId("1"));
        assertTrue(dataVal.isValidId("10"));
        assertTrue(dataVal.isValidId("1000"));
    }

    @Test
    public void testIsValidIdFromDatabase() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ?", new String[]{"1"}, true, "key", "value");

        assertTrue(dataVal.isValidId(1, DatabaseConnector.CUSTOMERS));
        assertFalse(dataVal.isValidId(10, DatabaseConnector.CUSTOMERS));

        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ?", new String[]{"1"}, false, null, null);

        assertFalse(dataVal.isValidId(1, DatabaseConnector.CUSTOMERS));
        assertFalse(dataVal.isValidId(10, DatabaseConnector.CUSTOMERS));

    }

    @Test
    public void testIsUserAuthorized() {
        createAndAssignMockObjectSelectQuery(new String[]{"*"}, new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?", new String[]{"1", "email@test.de", "testPassword"},
                true,"key", "value");
        String authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:testPassword".getBytes());

        // correct input
        assertTrue(dataVal.isUserAuthorized(authorization, "1"));

        // wrong id
        assertFalse(dataVal.isUserAuthorized(authorization, "2"));

        // wrong authorization
        authorization = "Basic " + Base64.getEncoder().encodeToString("email@test.de:wrongPassword".getBytes());
        assertFalse(dataVal.isUserAuthorized(authorization, "2"));
        assertFalse(dataVal.isUserAuthorized(authorization, "1"));
        assertFalse(dataVal.isUserAuthorized("", "1"));
        assertFalse(dataVal.isUserAuthorized(null, "1"));
    }

    @Test
    public void testIsValidPaymentMethod() {
        // Test valid payment methods (various cases)
        String[] validMethods = {
                "Cash", "cash",
                "Credit Card", "Credit card", "credit card",
                "Debit Card", "Debit card", "debit card",
                "PayPal", "paypal",
                "ApplePay", "applepay",
                "Not selected"
        };
        for (String method : validMethods) {
            assertTrue(dataVal.isValidPaymentMethod(method),
                    "Should accept valid payment method: " + method);
        }

        // Test invalid payment methods
        String[] invalidMethods = {
                null,
                "",
                "CASH",
                "creditcard",
                "debitcard",
                "pay pal",
                "apple pay",
                "Bitcoin",
                "Google Pay"
        };
        for (String method : invalidMethods) {
            assertFalse(dataVal.isValidPaymentMethod(method),
                    "Should reject invalid payment method: " + method);
        }
    }

}