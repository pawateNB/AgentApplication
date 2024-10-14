package org.example;

//import com.microsoft.sqlserver;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;


public class DbConnector {
    public int getCarrierId(String getCarrieName){
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("DB_URL");
        String username = dotenv.get("DB_USERNAME");
        String password = dotenv.get("DB_PASSWORD");
        try{
            Connection connection = DriverManager.getConnection(url, username, password);
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

    public static void main(String[] args) {
        DbConnector dbConnector = new DbConnector();
        dbConnector.getCarrierId("aetna");
    }
}
