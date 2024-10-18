package org.example;

//import com.microsoft.sqlserver;

import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;


public class DbConnector {
    Connection connection;
    Statement statement;
    Dotenv dotenv = Dotenv.load();
    String connectionUrl = dotenv.get("DB_CONNECTION_STRING");
    public DbConnector(){
        try{
            connection = DriverManager.getConnection(connectionUrl);
            statement = connection.createStatement();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public int getCarrierId(String getCarrierName) {
        String cName= "%" + getCarrierName.replace(" ", "%")+"%";
        String query = "SELECT InsuranceCarrierID from Insurance.InsuranceCarriers where InsuranceCarrierName like '"+cName+"';";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                int carrierId = resultSet.getInt("InsuranceCarrierID");
                return carrierId;
            }
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
        System.out.println(dbConnector.getCarrierId("Aetna"));
    }


}
