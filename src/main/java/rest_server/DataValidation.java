package rest_server;

import model.DatabaseConnector;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DataValidation {

    private final DatabaseConnector dbConnector;

    public DataValidation(DatabaseConnector dbConnector) {
        this.dbConnector = dbConnector;
    }

    /*
     * TODO: Implement further data validation methods in case you need any
     * additional methods that are used in the class RestServer.
     *
     * Leave the rest of the methods and variables untouched as you might want to
     * use them.
     */

    /**
     * Checks if the input contains only numerical characters and the number is
     * greater than 0, meaning the input is a valid id.
     *
     * @param input the string containing the id
     * @return <code>true</code> if string is a valid id; <code>false</code>
     * otherwise
     */
    public boolean isValidId(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(input) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the given id is contained in the column <i>id</i> of the database
     * table, meaning the id is valid.
     *
     * @param id    the id to check
     * @param table the name of the database table
     * @return <code>true</code> if id exists; <code>false</code> otherwise
     */
    public boolean isValidId(int id, String table) {
        List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"}, new String[]{table},
                null, "id = ?", new String[]{String.valueOf(id)});

        return !result.isEmpty();
    }

    /**
     * Checks if the given payment method is valid by verifying if it matches one of
     * the allowed values: "Cash", "cash", "Credit Card", "Credit card", "credit card", "Debit Card", "Debit card", "debit card",
     * "PayPal", "paypal", "ApplePay" or "applepay"
     *
     * @param paymentMethod the payment method to check
     * @return <code>true</code> if the payment method is valid; <code>false</code> otherwise
     */
    public boolean isValidPaymentMethod(String paymentMethod) {
        // List of valid payment method values
        String[] validMethods = {"Cash", "cash", "Credit Card", "Credit card", "credit card", "Debit Card", "Debit card", "debit card",
                "PayPal", "paypal", "ApplePay", "applepay", "Not selected"};

        // Check if the paymentMethod is one of the valid values
        for (String method : validMethods) {
            if (method.equals(paymentMethod)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Authenticates the user who makes a request with the given authorization
     * string. Decodes this string at first and then makes a database request which
     * checks if a user with the provided first and last name has the corresponding
     * id.
     *
     * @param authString the string containing the users authentication
     * @param userId     the id the user should have
     * @return <code>true</code> if user is authenticated; <code>false</code>
     * otherwise
     */
    public boolean isUserAuthorized(String authString, String userId) {
        if (authString == null || authString.isEmpty()) {
            return false;
        }

        // Header is in the format "Basic 5tyc0uiDat4"
        // extract data before decoding it back to original string
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[1];

        // Decode the data back to original string
        byte[] decodedBytes = Base64.getDecoder().decode(authInfo);
        String decodedAuth = new String(decodedBytes);

        // extract username (email) and password
        String[] credentials = decodedAuth.split(":");
        String email = credentials[0];
        String password = credentials[1];

        // check if entry in table exists with given email, password and id
        List<Map<String, Object>> result = dbConnector.executeSelectQuery(new String[]{"*"},
                new String[]{DatabaseConnector.CUSTOMERS}, null,
                "id = ? and email = ? and password = ?",
                new String[]{userId, email, password});

        // check if user exists with this credentials and id
        return result.size() == 1;
    }

}
