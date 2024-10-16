package org.example;

import java.util.*;
import java.net.URI;
import java.net.http.*;

public class ApiConnector {
    public HttpClient client= HttpClient.newHttpClient();
    private void parseValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey) + searchKey.length();
        int endIndex = json.indexOf(",", startIndex);

        if (endIndex == -1) { // If it's the last property
            endIndex = json.indexOf("}", startIndex);
        }

        System.out.println(json.substring(startIndex, endIndex).replace("\"", "").trim());
    }
    private String getJSON(int id,Boolean accessType,String firstName,String lastName,String email,String actionType,String userName){
        String json = "{"
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

        return json;

    }

    public void connectToApi(List<Integer> carrierIds,List<String> portalTypes,List<String> ActionType,List<String> firstNames,List<String> secondNames, List<String> emails,List<String> userNames){
        try {
            for (int i = 0; i < carrierIds.size(); i++) {
                int carrierId = carrierIds.get(i);
                String firstName = firstNames.get(i);
                String secondName = secondNames.get(i);
                String email = emails.get(i);
                Boolean portal = portalTypes.get(i).equals("MyBenefits Portal");
                String actionType = ActionType.get(i);
                String userName = userNames.get(i);
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
                parseValue(responseBody, "userName");
                parseValue(responseBody, "password");
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) {
        ApiConnector apiConnector = new ApiConnector();
        List<Integer> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        List<String> l3 = new ArrayList<>();
        List<String> l4 = new ArrayList<>();
        List<String>l5 = new ArrayList<>();
        List<String> l6 = new ArrayList<>();
        List<String> l7 = new ArrayList<>();
        apiConnector.connectToApi(l1, l2, l3, l4,l5,l6,l7);
    }
}
