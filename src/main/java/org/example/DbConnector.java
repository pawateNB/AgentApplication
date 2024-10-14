package org.example;

//import com.microsoft.sqlserver;

//import com.microsoft.sqlserver;

import java.sql.*;


public class DbConnector {
    public int getCarrierId(String getCarrieName){
        String connectionUrl = "jdbc:sqlserver://nbotcproduse2dbsrv;databaseName=NBAPP_SUP;user=MaskUser;password=Ma$kus36;";
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

    public static void main(String[] args) {
        DbConnector dbConnector = new DbConnector();
        dbConnector.getCarrierId("aetna");
    }
}
