package org.example;

import java.util.*;
import java.net.URI;
import java.net.http.*;

public class ApiConnector {
    public void connectToApiNew(List<Integer> carrierIds,List<String> firstNames,List<String> secondNames, List<String> emails){
        try {
            HttpClient client = HttpClient.newHttpClient();
            for (int i = 0; i < firstNames.size(); i++) {
                String firstName = firstNames.get(i);
                String secondName = secondNames.get(i);
                String email = emails.get(i);
                String json = "{"
                        + "\"insuranceCarrierId\": 16,"
                        + "\"accessType\": {"
                        + "\"isMember\": true,"
                        + "\"isMeals\": false"
                        + "},"
                        + "\"firstName\": \"" + firstName + "\","
                        + "\"lastName\": \"" + secondName + "\","
                        + "\"email\": \"" + email + "\","
                        + "\"actionType\": \"new\","
                        + "\"userName\": \"string\","
                        + "\"createUser\": \"Mnanduri\""
                        + "}";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://nbotc-train-use2-auth-app.azurewebsites.net/api/User/CreateAgentCredentials"))
                        .header("Content-Type", "application/json-patch+json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response:" + response.body());

                // Extracting Username, Password

                String responseBody = response.body();
                parseValue(responseBody, "userName");
                parseValue(responseBody, "password");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void parseValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf(",", startIndex);

        if (endIndex == -1) { // If it's the last property
            endIndex = json.indexOf("}", startIndex);
        }

        System.out.println(json.substring(startIndex, endIndex).replace("\"", "").trim());
    }

    public static void main(String[] args) {
        ApiConnector apiConnector = new ApiConnector();
        List<Integer> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        List<String> l3 = new ArrayList<>();
        List<String> l4 = new ArrayList<>();
        apiConnector.connectToApiNew(l1, l2, l3, l4);
    }
}
