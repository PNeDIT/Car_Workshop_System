package model;

import java.util.Base64;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS!
 * Models the currently logged in user with the corresponding attributes (simple
 * POJO (Plain Old Java Object)).
 */
public class User {

    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String authorization;
    private int loyaltyTokens;

    /**
     * Used on log in/sign up, so that authorization is already created and
     * information from REST server can be requested.
     *
     * @param email    the email of the user
     * @param password the corresponding password to the email
     */
    public User(String firstname, String lastname, String email, String password) {
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.password = password;
        this.loyaltyTokens = 0;

        // generate authorization string
        String auth = email + ":" + password;
        String authEnc = Base64.getEncoder().encodeToString(auth.getBytes());
        this.authorization = "Basic " + authEnc;
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.loyaltyTokens = 0;

        // generate authorization string
        String auth = email + ":" + password;
        String authEnc = Base64.getEncoder().encodeToString(auth.getBytes());
        this.authorization = "Basic " + authEnc;
    }

    // GETTER and SETTERS

    /**
     * Assigns the three attributes - id, first name and last name - with the given
     * values.
     *
     * @param id        the id used in the database table
     * @param firstname the first name of the user
     * @param lastname  the last name of the user
     */
    public void setIdAndName(int id, String firstname, String lastname) {
        this.id = id;
        this.firstName = firstname;
        this.lastName = lastname;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLoyaltyTokens() {
        return loyaltyTokens;
    }

    public void setLoyaltyTokens(int loyaltyTokens) {
        this.loyaltyTokens = loyaltyTokens;
    }

    /**
     * Updates the loyalty tokens based on the user's appointment history
     * @param appointmentCount the number of appointments the user has made
     */
    public void updateLoyaltyTokensFromHistory(int appointmentCount) {
        this.loyaltyTokens = appointmentCount;
    }

    @Override
    public String toString() {
        return "Session [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email + "]";
    }

}
