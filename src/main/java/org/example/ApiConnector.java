package org.example;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.net.URI;
import java.net.http.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ApiConnector {
    public HttpClient client = HttpClient.newHttpClient();
    private List<String> returnedUsernames = new ArrayList<>();
    private List<String> returnedPasswords = new ArrayList<>();

    // Parsing and storing values for username and password
    private String parseValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf(",", startIndex);

        if (endIndex == -1) { // If it's the last property
            endIndex = json.indexOf("}", startIndex);
        }
        String trim = json.substring(startIndex, endIndex).replace("\"", "").trim();
        System.out.println(key+":"+ trim);
        return trim;
    }

    // Generate JSON request payload
    private String getJSON(int id, Boolean accessType, String firstName, String lastName, String email, String actionType, String userName) {
        return "{"
                + "\"insuranceCarrierId\":\"" + id + "\","
                + "\"accessType\": {"
                + "\"isMember\":\"" + accessType + "\","
                + "\"isMeals\":\"" + !accessType + "\","
                + "},"
                + "\"firstName\":\"" + firstName + "\","
                + "\"lastName\":\"" + lastName + "\","
                + "\"email\":\"" + email + "\","
                + "\"actionType\":\"" + actionType + "\","
                + "\"userName\":\"" + userName + "\","
                + "\"createUser\": \"Mnanduri\""
                + "}";
    }

    // Connect to the API and update Excel with returned credentials
    public void connectToApi(List<Integer> carrierIds, List<String> portalTypes, List<String> actionTypes, List<String> firstNames, List<String> secondNames, List<String> emails, List<String> userNames) {
        try {
            for (int i = 0; i < carrierIds.size(); i++) {
                int carrierId = carrierIds.get(i);
                String firstName = firstNames.get(i);
                String secondName = secondNames.get(i);
                String email = emails.get(i);
                Boolean portal = portalTypes.get(i).equals("MyBenefits Portal");
                String actionType = actionTypes.get(i);
                String userName = userNames.get(i);

                // If actionType is not new and no username, retrieve from DB
                if (!actionType.equals("new") && userName.equals("string")) {
                    DbConnector dbConnector = new DbConnector();
                    userName = dbConnector.getUsername(carrierId, firstName, secondName, email);
                }

                String json = getJSON(carrierId, portal, firstName, secondName, email, actionType, userName);
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://nbotc-train-use2-auth-app.azurewebsites.net/api/User/CreateAgentCredentials"))
                        .header("Content-Type", "application/json-patch+json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String responseBody = response.body();

                // Parse returned username and password and store them
                String returnedUsername = parseValue(responseBody, "userName");
                String returnedPassword = parseValue(responseBody, "password");
                returnedUsernames.add(returnedUsername);
                returnedPasswords.add(returnedPassword);

                //Delay 3 seconds!
                Thread.sleep(3000);
            }

            // Update the Excel sheet with the returned usernames and passwords
            updateExcelWithCredentials(returnedUsernames, returnedPasswords);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to update Excel file with credentials
    private void updateExcelWithCredentials(List<String> usernames, List<String> passwords) {
        String excelFilePath = "C:\\Users\\ParthAwate\\ExcelSheets\\CredCheck.xlsx";

        try (FileInputStream file = new FileInputStream(new File(excelFilePath))) {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            int usernameColumn = -1;
            int passwordColumn = -1;

            // Find the username and password columns
            Row headerRow = sheet.getRow(0);
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().equalsIgnoreCase("Username")) {
                    usernameColumn = cell.getColumnIndex();
                }
                if (cell.getStringCellValue().equalsIgnoreCase("Password")) {
                    passwordColumn = cell.getColumnIndex();
                }
            }

            // If columns are not present, create them
            if (usernameColumn == -1) {
                usernameColumn = headerRow.getLastCellNum();
                headerRow.createCell(usernameColumn).setCellValue("Username");
            }
            if (passwordColumn == -1) {
                passwordColumn = headerRow.getLastCellNum();
                headerRow.createCell(passwordColumn).setCellValue("Password");
            }

            // Write usernames and passwords to corresponding rows
            for (int i = 0; i < usernames.size(); i++) {
                Row row = sheet.getRow(i + 1); // Skip header row
                if (row == null) {
                    row = sheet.createRow(i + 1);
                }
                Cell usernameCell = row.createCell(usernameColumn);
                usernameCell.setCellValue(usernames.get(i));

                Cell passwordCell = row.createCell(passwordColumn);
                passwordCell.setCellValue(passwords.get(i));
            }

            // Close the FileInputStream before writing to the file
            file.close();

            //Delay 1 second
            try{
                Thread.sleep(1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            // Write changes to the Excel file
            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }

            workbook.close();  // Close the workbook
            System.out.println("Excel file updated successfully!");

        } catch (IOException e) {
            System.err.println("Error updating Excel file: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        ApiConnector apiConnector = new ApiConnector();
        List<Integer> carrierIds = Arrays.asList(1, 2, 3);
        List<String> portalTypes = Arrays.asList("MyBenefits Portal", "Other Portal", "MyBenefits Portal");
        List<String> actionTypes = Arrays.asList("new", "reset", "delete");
        List<String> firstNames = Arrays.asList("John", "Jane", "Doe");
        List<String> secondNames = Arrays.asList("Doe", "Smith", "Doe");
        List<String> emails = Arrays.asList("john@example.com", "jane@example.com", "doe@example.com");
        List<String> userNames = Arrays.asList("jdoe", "jsmith", "ddoe");

        apiConnector.connectToApi(carrierIds, portalTypes, actionTypes, firstNames, secondNames, emails, userNames);
    }
}
