package org.example.Test1; // Scucessful creating record

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;
import org.example.Entity.User;

public class AirTableExample2 {
    public static void main(String[] args) {
        String baseId = "appwZ9qRyIS0zKCQC";
        String tableName = "User";
        String apiKey = "keyVDjRmqqsUJD0xC";

        User user1 = new User("John", 25);
        Gson gson = new Gson();
        String myJson = gson.toJson(user1);
        //System.out.println(myJson);

        try {
            // Endpoint URL
            String url = String.format("https://api.airtable.com/v0/%s/%s", baseId, tableName);

            // Create a new record JSON payload
            //String jsonPayload = "{\"fields\": {\"Name\": \"Hoang Anh\", \"Age\": 25}}";

            // Create connection
            URL apiURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();

            // Set request method
            connection.setRequestMethod("POST");

            // Set headers
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // Enable output and send payload
            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(myJson);
            outputStream.flush();
            outputStream.close();

            // Get response
            int responseCode = connection.getResponseCode();

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Process response
            if (responseCode == 200) {
                System.out.println("Record created successfully");
                System.out.println("Response: " + response.toString());
            } else {
                System.out.println("Request unsuccessful. Status code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

