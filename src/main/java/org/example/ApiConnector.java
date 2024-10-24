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
    List<String> returnedUsernames = new ArrayList<>();
    List<String> returnedPasswords = new ArrayList<>();

    // Main method for API connection
    public void connectToApi(List<Integer> carrierIds, List<String> portalTypes, List<String> actionTypes,
                             List<String> firstNames, List<String> secondNames, List<String> emails,
                             List<String> userNames, char val) {
        try {
            for (int i = 0; i < carrierIds.size(); i++) {
                String jsonRequest = prepareJsonRequest(carrierIds.get(i), portalTypes.get(i), actionTypes.get(i),
                        firstNames.get(i), secondNames.get(i), emails.get(i),
                        userNames.get(i), val);
                String responseBody = sendApiRequest(jsonRequest,val);
                processApiResponse(responseBody);
                Thread.sleep(3000); // Adding delay
            }
            updateExcelWithCredentials(returnedUsernames, returnedPasswords);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Prepare JSON request
    private String prepareJsonRequest(int carrierId, String portalType, String actionType, String firstName,
                                      String secondName, String email, String userName, char val) {
        if (!actionType.equals("new") && userName.equals("string")) {
            DbConnector dbConnector = new DbConnector(val);
            userName = dbConnector.getUsername(carrierId, firstName, secondName, email);
        }
        Boolean accessType = portalType.equals("MyBenefits Portal");
        return getJSON(carrierId, accessType, firstName, secondName, email, actionType, userName);
    }

    // Send API request and return response body
    private String sendApiRequest(String json,char val) throws IOException, InterruptedException {
        String uri;
        if(val == 'N'){
            uri = "https://nbotc-train-use2-auth-app.azurewebsites.net/api/User/CreateAgentCredentials";
        } else {
            uri = "https://nbantm-train-use2-auth-app.azurewebsites.net/api/User/CreateAgentCredentials";
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Content-Type", "application/json-patch+json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Process API response and store credentials
    private void processApiResponse(String responseBody) {
        String returnedUsername = parseValue(responseBody, "userName");
        String returnedPassword = parseValue(responseBody, "password");
        returnedUsernames.add(returnedUsername);
        returnedPasswords.add(returnedPassword);
    }

    // Generate JSON request payload
    private String getJSON(int id, Boolean accessType, String firstName, String lastName,
                           String email, String actionType, String userName) {
        return "{"
                + "\"insuranceCarrierId\":\"" + id + "\","
                + "\"accessType\": {"
                + "\"isMember\":\"" + accessType + "\","
                + "\"isMeals\":\"" + !accessType + "\""
                + "},"
                + "\"firstName\":\"" + firstName + "\","
                + "\"lastName\":\"" + lastName + "\","
                + "\"email\":\"" + email + "\","
                + "\"actionType\":\"" + actionType + "\","
                + "\"userName\":\"" + userName + "\","
                + "\"createUser\": \"Mnanduri\""
                + "}";
    }

    // Parse specific value from JSON response
    private String parseValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        return json.substring(startIndex, endIndex).replace("\"", "").trim();
    }

    // Update Excel file with returned credentials
    private void updateExcelWithCredentials(List<String> usernames, List<String> passwords) {
        String excelFilePath = "C:\\Users\\ParthAwate\\ExcelSheets\\CredCheck.xlsx";
        try (FileInputStream file = new FileInputStream(new File(excelFilePath))) {
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            int usernameColumn = findOrCreateColumn(sheet, "Username");
            int passwordColumn = findOrCreateColumn(sheet, "Password");

            writeCredentialsToSheet(sheet, usernames, passwords, usernameColumn, passwordColumn);

            file.close();
            writeFile(workbook, excelFilePath);
        } catch (IOException e) {
            System.err.println("Error updating Excel file: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Find or create a column in the Excel sheet
    private int findOrCreateColumn(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().equalsIgnoreCase(columnName)) {
                return cell.getColumnIndex();
            }
        }
        int newColumnIndex = headerRow.getLastCellNum();
        headerRow.createCell(newColumnIndex).setCellValue(columnName);
        return newColumnIndex;
    }

    // Write credentials to the Excel sheet
    private void writeCredentialsToSheet(Sheet sheet, List<String> usernames, List<String> passwords,
                                         int usernameColumn, int passwordColumn) {
        for (int i = 0; i < usernames.size(); i++) {
            Row row = sheet.getRow(i + 1); // Skip header row
            if (row == null) {
                row = sheet.createRow(i + 1);
            }
            row.createCell(usernameColumn).setCellValue(usernames.get(i));
            row.createCell(passwordColumn).setCellValue(passwords.get(i));
        }
    }

    // Write the updated workbook to the Excel file
    private void writeFile(Workbook workbook, String excelFilePath) throws IOException, InterruptedException {
        Thread.sleep(1000); // Delay for 1 second
        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            workbook.write(outputStream);
        }
        workbook.close();
        System.out.println("Excel file updated successfully!");
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

        apiConnector.connectToApi(carrierIds, portalTypes, actionTypes, firstNames, secondNames, emails, userNames, 'N');
    }
}
