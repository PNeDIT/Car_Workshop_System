package model;

import utils.Utils;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS, EXCEPT FOR DATABASE CONSTANTS AND
 * TABLE NAMES! <br>
 * <br>
 * Handles the connection to the MariaDB database and performs SQL statements
 * (SELECT, INSERT, UPDATE, DELETE) on it.
 */
public class DatabaseConnector {

    // ------------------------ DATABASE CONSTANTS ------------------------
    public static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    public static final String DB_URL = "jdbc:mariadb://localhost/";
    // TODO: replace "root" with your username for the database
    public static final String USERNAME = "root";
    // TODO: replace "root" with the corresponding password to the username
    public static final String PASSWORD = "root";
    // --------------------------------------------------------------------

    // --------------------------- TABLE NAMES ---------------------------
    public static final String CUSTOMERS = "customers";
    public static final String WORKSHOPS = "workshops";
    public static final String PROMOTIONS = "promotions";
    public static final String TECHNICIANS = "technicians";
    public static final String SERVICES = "services";
    public static final String WORKSHOP_SERVICES = "workshop_services";
    public static final String REVIEWS = "reviews";
    public static final String APPOINTMENTS = "appointments";
    public static final String MAINTENANCE_REMINDERS = "maintenanceReminders";
    public static final String QR_CODES = "qrCodes";
    // -------------------------------------------------------------------
    private static final Logger logger = Logger.getLogger(Utils.class.getName());
    private Connection connection;

    public DatabaseConnector(String database) {
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // open connection
            connection = DriverManager.getConnection(DB_URL + database, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, "ClassNotFoundException in DatabaseConnector constructor");
        } catch (SQLInvalidAuthorizationSpecException e){
            logger.log(Level.WARNING, "Invalid credentials!");
            throw new RuntimeException("Invalid database credentials - did you update them in the DatabaseConnector?");
        } catch (SQLException e) {
            logger.log(Level.FINE, "SQL error when accessing %s", database);
            if (e.getMessage().contains("Unknown database")) {
                logger.log(Level.WARNING, "Database not found!");
                throw new RuntimeException("Database not found - did you initialize it with setupDatabaseAndTablesResSystem.sql?");
            }
        }
    }

    /**
     * Creates a SQL SELECT statement with the given parameters as follows:
     *
     * <br>
     * <br>
     * <b>SELECT</b> projection <br>
     * <b>FROM</b> tables as tableAlias <br>
     * <b>WHERE</b> selection = selectionArgs <br>
     * <br>
     * <p>
     * Here, in the <b>FROM</b> and <b>WHERE</b> part each of the arguments
     * corresponds to one item of the array. <br>
     * <br>
     * <p>
     * Next, executes the SQL statement and converts the result into a list of maps.
     * Each entry in the list corresponds to one table row. The keys of the map
     * correspond to column names and the values to the specific cell in the table.
     *
     * @param selection    the array with column names; cannot be
     *                      <code>null</code>; can contain only "*" for selecting
     *                      all columns
     * @param tables        the array with database table names; cannot be
     *                      <code>null</code>
     * @param tableAlias    the array with table name aliases; can be
     *                      <code>null</code> to omit aliases
     * @param condition     the array with conditions; can be <code>null</code> to
     *                      omit conditions
     * @param conditionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @return the list of maps representing the result
     */
    public List<Map<String, Object>> executeSelectQuery(String[] selection, String[] tables, String[] tableAlias,
                                                        String condition, String[] conditionArgs) {
        List<Map<String, Object>> map = null;
        try {
            PreparedStatement stmt = createSelectStatement(selection, tables, tableAlias, condition, conditionArgs);
            if (stmt != null) {
                ResultSet result = stmt.executeQuery();
                map = map(result);
            }
            close(stmt);
        } catch (SQLException e) {
            logger.log(Level.FINE, "SQL error when executing select statement");
        }
        return map;
    }

    /**
     * Creates a SQL SELECT statement like shown in
     * {@link #executeSelectQuery(String[], String[], String[], String, String[])}
     * with the same parameters.
     *
     * @param selection    the array with column names; cannot be
     *                      <code>null</code>; can contain only "*" for selecting
     *                      all columns
     * @param tables        the array with database table names; cannot be
     *                      <code>null</code>
     * @param tableAlias    the array with table name aliases; can be
     *                      <code>null</code> to omit aliases
     * @param condition     the array with conditions; can be <code>null</code> to
     *                      omit conditions
     * @param conditionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @return the created SQL statement as <code>PreparedStatement</code> object
     */
    private PreparedStatement createSelectStatement(String[] selection, String[] tables, String[] tableAlias,
                                                    String condition, String[] conditionArgs) {
        PreparedStatement stmt = null;
        if (selection != null && tables != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("SELECT ");
                sb.append(String.join(", ", selection));
                sb.append(" FROM ");

                // only if aliases for table given, concatenate them with table name
                if (tableAlias != null) {
                    String[] tmp = new String[tables.length];
                    for (int i = 0; i < tables.length; i++) {
                        tmp[i] = tables[i] + " as " + tableAlias[i];
                    }
                    sb.append(String.join(", ", tmp));
                } else {
                    sb.append(String.join(", ", tables));
                }

                // only if selection is available, append it
                if (condition != null) {
                    sb.append(" WHERE ");
                    sb.append(condition);
                }

                stmt = connection.prepareStatement(sb.toString());

                // only if parameters are given, substitute them
                if (conditionArgs != null) {
                    for (int i = 0; i < conditionArgs.length; i++) {
                        stmt.setString(i + 1, conditionArgs[i]);
                    }
                }

            } catch (SQLException e) {
                logger.log(Level.FINE, "SQL error when executing select statement");
                return null;
            }
        }
        return stmt;
    }

    /**
     * Creates a SQL UPDATE statement with the given parameters as follows:
     *
     * <br>
     * <br>
     * <b>UPDATE</b> table <br>
     * <b>SET</b> modification = modificationArgs <br>
     * <b>WHERE</b> selection = selectionsArgs <br>
     * <br>
     * <p>
     * Here, in the <b>SET</b> and <b>WHERE</b> part each of the arguments
     * corresponds to one item of the array. <br>
     * <br>
     * <p>
     * Next, executes the SQL statement and returns whether the update was
     * successful or not.
     *
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
     * @return <code>true</code> if the update was successful; <code>false</code>
     * otherwise
     */
    public boolean executeUpdateQuery(String table, String[] modification, String[] modificationArgs, String selection,
                                      String[] selectionArgs) {
        try {
            PreparedStatement stmt = createUpdateStatement(table, modification, modificationArgs, selection,
                    selectionArgs);
            if (stmt != null) {
                stmt.executeUpdate();
            } else {
                return false;
            }
            close(stmt);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * Creates a SQL UPDATE statement like shown in
     * {@link #executeUpdateQuery(String, String[], String[], String, String[])}
     * with the same parameters.
     *
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
     * @return the created SQL statement as <code>PreparedStatement</code> object
     */
    private PreparedStatement createUpdateStatement(String table, String[] modification, String[] modificationArgs,
                                                    String selection, String[] selectionArgs) {
        PreparedStatement stmt = null;
        if (table != null && modification != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE ");
                sb.append(table);
                sb.append(" SET ");

                // Join the modifications with commas instead of AND
                String[] cleanModifications = new String[modification.length];
                for (int i = 0; i < modification.length; i++) {
                    cleanModifications[i] = modification[i].replace(" and ", ", ");
                }
                sb.append(String.join(", ", cleanModifications));

                // only if selection is available, append it
                if (selection != null) {
                    sb.append(" WHERE ");
                    sb.append(selection);
                }

                String finalQuery = sb.toString();

                stmt = connection.prepareStatement(finalQuery);
                int index = 1;
                if (modificationArgs != null) {
                    for (String arg : modificationArgs) {
                        stmt.setString(index++, arg);
                    }
                }
                if (selection != null && selectionArgs != null) {
                    for (String arg : selectionArgs) {
                        stmt.setString(index++, arg);
                    }
                }

            } catch (SQLException e) {
                System.err.println("SQL error when creating update statement: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        return stmt;
    }

    /**
     * Creates a SQL INSERT statement with the given parameters as follows:
     *
     * <br>
     * <br>
     * <b>INSERT INTO</b> table (columns)<br>
     * <b>VALUES</b> (values)<br>
     * <br>
     * <p>
     * Executes the SQL statement and returns whether the update was successful or
     * not.
     *
     * @param table   the name of the table; cannot be <code>null</code>
     * @param columns the array containing the columns to insert values; cannot be
     *                <code>null</code>
     * @param values  the array with the values to insert; cannot be
     *                <code>null</code>; must have the same length as columns
     * @return <code>true</code> if the insertion was successful; <code>false</code>
     * otherwise
     */
    public boolean executeInsertQuery(String table, String[] columns, String[] values) {
        try {

            PreparedStatement stmt = createInsertStatement(table, columns, values);
            if (stmt != null) {
                stmt.executeUpdate();
            } else {
                System.err.println("Failed to create PreparedStatement - returned null");
                return false;
            }
            close(stmt);
        } catch (SQLException e) {
            System.err.println("SQL Exception in executeInsertQuery: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error in executeInsertQuery: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Creates a SQL INSERT statement like shown in
     * {@link #executeInsertQuery(String, String[], String[])} with the same
     * parameters.
     *
     * @param table   the name of the table; cannot be <code>null</code>
     * @param columns the array containing the columns to insert values; cannot be
     *                <code>null</code>
     * @param values  the array with the values to insert; cannot be
     *                <code>null</code>; must have the same length as columns
     * @return the created SQL statement as <code>PreparedStatement</code> object
     */
    private PreparedStatement createInsertStatement(String table, String[] columns, String[] values) {
        PreparedStatement stmt = null;
        try {

            if (table == null || columns == null || values == null || columns.length != values.length) {
                System.err.println("Invalid parameters for INSERT statement:");
                System.err.println("Table is null: " + (table == null));
                System.err.println("Columns is null: " + (columns == null));
                System.err.println("Values is null: " + (values == null));
                if (columns != null && values != null) {
                    System.err.println("Column length: " + columns.length);
                    System.err.println("Values length: " + values.length);
                }
                return null;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(table).append(" (");
            sb.append(String.join(", ", columns));
            sb.append(") VALUES (");

            String[] formattedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                formattedValues[i] = "\"" + values[i] + "\"";
            }
            sb.append(String.join(", ", formattedValues)).append(")");

            String finalQuery = sb.toString();

            stmt = connection.prepareStatement(finalQuery);
            return stmt;

        } catch (SQLException e) {
            System.err.println("SQL Error in createInsertStatement: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error in createInsertStatement: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a SQL DELETE statement with the given parameters as follows:
     *
     * <br>
     * <br>
     * <b>DELETE FROM</b> table<br>
     * <b>WHERE</b> selection = selectionArgs<br>
     * <br>
     * <p>
     * In the <b>WHERE</b> part selection and selectionArgs corresponds to one item
     * of the array. <br>
     * <br>
     * <p>
     * Executes the SQL statement and returns whether the update was successful or
     * not.
     *
     * @param table         the name of the table to delete from; cannot be
     *                      <code>null</code>
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      delete all records in table
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @return <code>true</code> if the deletion was successful; <code>false</code>
     * otherwise
     */
    public boolean executeDeleteQuery(String table, String selection, String[] selectionArgs) {
        try {
            PreparedStatement stmt = createDeleteStatement(table, selection, selectionArgs);
            if (stmt != null) {
                stmt.executeUpdate();
            } else {
                return false;
            }
            close(stmt);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    /**
     * Creates a SQL DELETE statement like shown in
     * {@link #executeDeleteQuery(String, String, String[])} with the same
     * parameters.
     *
     * @param table         the name of the table to delete from; cannot be
     *                      <code>null</code>
     * @param selection     the array with conditions; can be <code>null</code> to
     *                      delete all records in table
     * @param selectionArgs the array with the corresponding values for the
     *                      selection; can be <code>null</code> if no parameter
     *                      values are needed
     * @return the created SQL statement as <code>PreparedStatement</code> object
     */
    private PreparedStatement createDeleteStatement(String table, String selection, String[] selectionArgs) {
        PreparedStatement stmt = null;
        if (table != null) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("DELETE FROM ");
                sb.append(table);

                // only if selection is available, append it
                if (selection != null) {
                    sb.append(" WHERE ");
                    sb.append(selection);
                }

                stmt = connection.prepareStatement(sb.toString());
                // only if parameters are given, substitute them
                if (selectionArgs != null) {
                    for (int i = 0; i < selectionArgs.length; i++) {
                        stmt.setString(i + 1, selectionArgs[i]);
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.FINE, "SQL error when executing delete statement");
                return null;
            }
        }
        return stmt;
    }

    /**
     * Maps the <code>ResultSet</code> to a list of maps. Each list item corresponds
     * to one line in the result. The keys of the map are the column names and the
     * values of the map are the specific cell entries from the table.
     * <p>
     * Maps the result given from the database to a list of maps.
     *
     * @param result the ResultSet that is converted
     * @return the list of maps
     */
    public List<Map<String, Object>> map(ResultSet result) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            if (result != null) {
                ResultSetMetaData meta = result.getMetaData();
                int numColumns = meta.getColumnCount();
                while (result.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= numColumns; ++i) {
                        String name = meta.getColumnLabel(i);
                        Object value = result.getObject(i);
                        row.put(name, value);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.FINE, "SQL error when executing statement");
        } finally {
            close(result);
        }
        return results;
    }

    // define various close methods: for Connection, Statement and ResultSet

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.FINE, "SQL error when closing");
        }
    }

    private void close(Statement st) {
        try {
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            logger.log(Level.FINE, "SQL error when closing");
        }
    }

    private void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            logger.log(Level.FINE, "SQL error when closing");
        }
    }

}
