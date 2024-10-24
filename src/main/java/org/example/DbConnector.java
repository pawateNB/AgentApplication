package org.example;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class DbConnector {
    private Connection connection;
    private Statement statement;
    private Dotenv dotenv = Dotenv.load();
    private String connectionUrl1 = dotenv.get("DB_CONNECTION_STRING1");
    private String connectionUrl2 = dotenv.get("DB_CONNECTION_STRING2");

    // Constructor to establish database connection
    public DbConnector(char db) {
        connectToDatabase(db);
    }

    // Method to establish connection based on the database type (N/E)
    private void connectToDatabase(char db) {
        try {
            if (db == 'N') {
                connection = DriverManager.getConnection(connectionUrl1);
            } else if (db == 'E') {
                connection = DriverManager.getConnection(connectionUrl2);
                System.out.println("Connection to EHCRM_SUP successful");
            }
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to retrieve the carrier ID based on carrier name
    public int getCarrierId(String carrierName) {
        String query = buildCarrierIdQuery(carrierName);
        return executeCarrierIdQuery(query);
    }

    // Helper method to build the carrier ID query
    private String buildCarrierIdQuery(String carrierName) {
        String formattedName = "%" + carrierName.replace(" ", "%") + "%";
        return "SELECT InsuranceCarrierID from Insurance.InsuranceCarriers WHERE InsuranceCarrierName LIKE '" + formattedName + "';";
    }

    // Helper method to execute the carrier ID query and return the ID
    private int executeCarrierIdQuery(String query) {
        try {
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return resultSet.getInt("InsuranceCarrierID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;  // Return 0 if no carrier ID is found
    }

    // Stub for getting the username, keeping this as a placeholder
    public String getUsername(int carrierId, String firstName, String secondName, String email) {
        return "No DB Connection";
    }

    // Main method for testing
    public static void main(String[] args) {
        DbConnector dbConnector = new DbConnector('E');
        System.out.println(dbConnector.getCarrierId("Elevance Health"));
    }
}
