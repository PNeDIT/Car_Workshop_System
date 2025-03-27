package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    @Test
    public void testGetUniqueItems() {
        List<JsonObject> jsonList = new ArrayList<>();

        // check empty json list
        List<String> result = Utils.getUniqueItems(jsonList, "firstname");
        assertEquals(new ArrayList<String>(), result);

        // add to persons to json list
        String person1 = "{\"firstname\": \"Max\", \"lastname\": \"Mustermann\", \"age\": 42}";
        String person2 = "{\"firstname\": \"Michael\", \"lastname\": \"Müller\", \"age\": 21}";
        jsonList.add(JsonParser.parseString(person1).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person1).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person2).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person2).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person2).getAsJsonObject());

        // check firstnames
        result = Utils.getUniqueItems(jsonList, "firstname");
        String[] firstnames = {"Max", "Michael"};
        assertEquals(new ArrayList<>(Arrays.asList(firstnames)), result);
        // check lastnames
        String[] lastnames = {"Mustermann", "Müller"};
        result = Utils.getUniqueItems(jsonList, "lastname");
        assertEquals(new ArrayList<>(Arrays.asList(lastnames)), result);
    }

    @Test
    public void testGetUniqueItemsWithValue() {
        List<JsonObject> jsonList = new ArrayList<>();

        // check empty json list
        List<JsonObject> result = Utils.getItemsWithValue(jsonList, "firstname", "Max");
        assertTrue(result.isEmpty());

        // add to persons to json list
        String person1 = "{\"firstname\": \"Max\", \"lastname\": \"Mustermann\", \"age\": 42}";
        String person2 = "{\"firstname\": \"Max\", \"lastname\": \"Maier\", \"age\": 32}";
        String person3 = "{\"firstname\": \"Michael\", \"lastname\": \"Müller\", \"age\": 21}";
        jsonList.add(JsonParser.parseString(person1).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person2).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person3).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person3).getAsJsonObject());

        String[] lastnames = {"Mustermann", "Maier"};
        String[] ages = {"42", "32"};
        for (int i = 0; i < result.size(); i++) {
            assertEquals("Max", result.get(i).get("firstname").getAsString());
            assertEquals(lastnames[i], result.get(i).get("lastname").getAsString());
            assertEquals(ages[i], result.get(i).get("age").getAsString());
        }
    }

    @Test
    public void testGetItems() {
        List<JsonObject> jsonList = new ArrayList<>();

        // check empty json list
        List<String> result = Utils.getUniqueItems(jsonList, "firstname");
        assertEquals(new ArrayList<String>(), result);

        // add to persons to json list
        String person1 = "{\"firstname\": \"Max\", \"lastname\": \"Mustermann\", \"age\": 42}";
        String person2 = "{\"firstname\": \"Michael\", \"lastname\": \"Müller\", \"age\": 21}";
        jsonList.add(JsonParser.parseString(person1).getAsJsonObject());
        jsonList.add(JsonParser.parseString(person2).getAsJsonObject());

        // check firstnames
        result = Utils.getUniqueItems(jsonList, "firstname");
        String[] firstnames = {"Max", "Michael"};
        assertEquals(new ArrayList<>(Arrays.asList(firstnames)), result);
        // check lastnames
        String[] lastnames = {"Mustermann", "Müller"};
        result = Utils.getUniqueItems(jsonList, "lastname");
        assertEquals(new ArrayList<>(Arrays.asList(lastnames)), result);
    }

    @Test
    public void testConvertTime12To24() {
        String time12 = "08:42:07 pm";
        String converted = Utils.convertTime12To24(time12);
        assertEquals("20:42", converted);

        time12 = "08:42:07 am";
        converted = Utils.convertTime12To24(time12);
        assertEquals("08:42", converted);

        time12 = "08:42:07";
        converted = Utils.convertTime12To24(time12);
        assertNull(converted);

        time12 = "20:42:07";
        converted = Utils.convertTime12To24(time12);
        assertNull(converted);

        time12 = "";
        converted = Utils.convertTime12To24(time12);
        assertNull(converted);

        time12 = null;
        converted = Utils.convertTime12To24(time12);
        assertNull(converted);
    }

    @Test
    public void testParseStringDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = Utils.parseStringToDate("22.05.2020", dateFormat);
        assertEquals("Fri May 22 00:00:00 CEST 2020", date.toString());

        date = Utils.parseStringToDate(null, dateFormat);
        assertNull(date);

        date = Utils.parseStringToDate("22.05.2020", null);
        assertNull(date);

        date = Utils.parseStringToDate(null, null);
        assertNull(date);

        date = Utils.parseStringToDate("22 Mar 2020", dateFormat);
        assertNull(date);

        dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        date = Utils.parseStringToDate("22.05.2020", dateFormat);
        assertNull(date);
    }

    @Test
    public void testParseListOfStringsToDates() throws ParseException {
        List<String> dates = new ArrayList<>();
        dates.add("22.05.2020");
        dates.add("06.07.2020");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        List<Date> expectedDates = new ArrayList<>();
        expectedDates.add(dateFormat.parse("22.05.2020"));
        expectedDates.add(dateFormat.parse("06.07.2020"));

        List<Date> parsedDates = Utils.parseListOfStringsToDates(dates, dateFormat);
        assertEquals(expectedDates, parsedDates);

        parsedDates = Utils.parseListOfStringsToDates(null, dateFormat);
        assertEquals(new ArrayList<Date>(), parsedDates);

        parsedDates = Utils.parseListOfStringsToDates(dates, null);
        assertEquals(new ArrayList<Date>(), parsedDates);

        parsedDates = Utils.parseListOfStringsToDates(null, null);
        assertEquals(new ArrayList<Date>(), parsedDates);

        dates = new ArrayList<>();
        dates.add("22 Mar 2020");
        dates.add("06 Jun 2020");
        parsedDates = Utils.parseListOfStringsToDates(dates, dateFormat);
        assertEquals(new ArrayList<Date>(), parsedDates);

        dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        parsedDates = Utils.parseListOfStringsToDates(dates, dateFormat);
        assertEquals(new ArrayList<Date>(), parsedDates);

    }

    @Test
    public void testIsPastDate() {
        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate todaysDate = LocalDate.now();

        Date[] pastDates = {Date.from(todaysDate.minusDays(5).atStartOfDay(defaultZoneId).toInstant()),
                Date.from(todaysDate.minusDays(10).atStartOfDay(defaultZoneId).toInstant())};
        for (Date date : pastDates) {
            assertTrue(Utils.isPastDate(date));
        }

        Date[] futureDates = {Date.from(todaysDate.plusDays(5).atStartOfDay(defaultZoneId).toInstant()),
                Date.from(todaysDate.plusDays(10).atStartOfDay(defaultZoneId).toInstant())};
        for (Date date : futureDates) {
            assertFalse(Utils.isPastDate(date));
        }
    }

    @Test
    public void testIsValidEmailAddress() {
        String[] correctEmails = {"firstname.lastname@gmail.com", "a.b@web.de", "a@web.de"};
        for (String email : correctEmails) {
            assertTrue(Utils.isValidEmailAddress(email));
        }

        String[] incorrectEmails = {null, "", "firstname.lastname@gmail", "1234"};
        for (String email : incorrectEmails) {
            assertFalse(Utils.isValidEmailAddress(email));
        }
    }

    @Test
    public void testIsAlpha() {
        String[] alphaStrings = {"Max Mustermann", "Hans-Peter Müller", "Brian O'Connor", "Amélie Poulain"};
        for (String s : alphaStrings) {
            assertTrue(Utils.isAlpha(s));
        }

        String[] nonAlphaStrings = {null, "", "Max1", "Max_Mustermann", "Max&Musterman/Muster", "<Max Mustermann>"};
        for (String s : nonAlphaStrings) {
            assertFalse(Utils.isAlpha(s));
        }
    }

    @Test
    public void testIsValidPassword() {
        String[] validPasswords = {"a1s2d3f4", "asdfasdf", "asdf%&/(", "q!2§t-_#"};
        for (String password : validPasswords) {
            assertTrue(Utils.isValidPassword(password));
        }

        String[] invalidPasswords = {null, "", "abc", "q!2§t-_"};
        for (String password : invalidPasswords) {
            assertFalse(Utils.isValidPassword(password));
        }
    }

    @Test
    public void testParseYearMonthDayTime() {
        // Test valid date string
        Date date = Utils.parseYearMonthDayTime("2024-03-15 14:30");
        assertNotNull(date);
        assertEquals("2024-03-15 14:30", Utils.formatYearMonthDayTime(date));

        // Test invalid format
        date = Utils.parseYearMonthDayTime("2024/03/15 14:30");
        assertNull(date);

        // Test null input
        date = Utils.parseYearMonthDayTime(null);
        assertNull(date);
    }

    @Test
    public void testFormatYearMonthDayTime() {
        // Test valid date
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 15, 14, 30);
        Date date = cal.getTime();
        String formatted = Utils.formatYearMonthDayTime(date);
        assertEquals("2024-03-15 14:30", formatted);

        // Test null date
        String result = Utils.formatYearMonthDayTime(null);
        assertNull(result);
    }

    @Test
    public void testIsValidVehicleRegistrationNumber() {
        // Test valid registration numbers
        String[] validNumbers = {
                "AB123CD",
                "A-123-CD",
                "AB 1234",
                "M-PM123"
        };
        for (String number : validNumbers) {
            assertTrue(Utils.isValidVehicleRegistrationNumber(number),
                    "Should accept valid registration: " + number);
        }

        // Test invalid registration numbers
        String[] invalidNumbers = {
                "",
                "123ABC",
                "ABC-123-DEF-456",
                "A1",
                "ABCD1234567"
        };
        for (String number : invalidNumbers) {
            assertFalse(Utils.isValidVehicleRegistrationNumber(number),
                    "Should reject invalid registration: " + number);
        }
    }

    @Test
    public void testIsValidPhoneNumber() {
        // Test valid phone numbers
        String[] validNumbers = {
                "1234567890",
                "9876543210",
                "0123456789"
        };
        for (String number : validNumbers) {
            assertTrue(Utils.isValidPhoneNumber(number),
                    "Should accept valid phone number: " + number);
        }

        // Test invalid phone numbers
        String[] invalidNumbers = {
                "",
                "123",
                "12345",
                "123456789",
                "12345678901",
                "abcdefghij",
                "123-456-7890"
        };
        for (String number : invalidNumbers) {
            assertFalse(Utils.isValidPhoneNumber(number),
                    "Should reject invalid phone number: " + number);
        }
    }

}