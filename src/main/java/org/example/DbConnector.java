package org.example;

//import com.microsoft.sqlserver;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;


public class DbConnector {
    public int getCarrierId(String getCarrieName){
        Dotenv dotenv = Dotenv.load();
//
        String connectionUrl = dotenv.get("DB_CONNECTION_STRING");

        try{
            Connection connection = DriverManager.getConnection(connectionUrl);
            if(connection != null){
                System.out.println("Connected to database");
            }else {
                System.out.println("Failed to connect to database");
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public String getUsername(int carrierId, String firstName, String secondName, String email) {
        return "No DB Connection";
    }
    public static void main(String[] args) {
        DbConnector dbConnector = new DbConnector();
        dbConnector.getCarrierId("aetna");
    }


}
