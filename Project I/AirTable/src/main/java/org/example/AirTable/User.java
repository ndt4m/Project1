
package org.example.AirTable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class User {
    private String name;
    private int age;
    private String job;
    private String message;

    public User(String name, int age, String job, String message) {
        this.name = name;
        this.age = age;
        this.job = job;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getJob() {
        return job;
    }

    public String getmessage() {
        return message;
    }

    public static void main(String[] args) {
        // Initialize a User object
        User user = new User("Hoai Hoang", 19, "designer", "Hello World!");

        // Convert User attributes to JSON
        JsonObject fields = new JsonObject();
        fields.addProperty("Name", user.getName());
        fields.addProperty("Age", user.getAge());
        fields.addProperty("Job", user.getJob());
        fields.addProperty("message", user.getmessage());
        System.out.println(fields);


        // Set your Airtable parameters
        String tableId = "tblXlYIDu5eA3NCCJ";
        String baseId = "appwZ9qRyIS0zKCQC";
        String token = "pat3qYiT5gazmDM8c.e4c6ce1998c288b8ff2354a70e5e41c8957b47aef3edc08acec7dfd2eebcaa14";


        // Create a record
//        String response = Record.createRecord(fields, tableId, baseId, token);
//
//        if (response != null) {
//            System.out.println("Record created successfully!");
//            //System.out.println("Response: " + response);
//        } else {
//            System.out.println("Failed to create record.");
//        }


        // Delete a record
//        boolean deletionResult = Record.dropRecord("recF3xTsNckRhHYjw", tableId, baseId, token);
//        if (deletionResult) {
//                System.out.println("Record deleted successfully!");
//            } else {
//                System.out.println("Failed to delete record.");
//            }

        // List records
//        String response = Record.listRecords(tableId, baseId, token);
//
//        if (response != null) {
//            JsonElement jsonElement = JsonParser.parseString(response);
//            JsonArray records = jsonElement.getAsJsonObject().getAsJsonArray("records");
//
//            for (JsonElement record : records) {
//                JsonObject list_fields = record.getAsJsonObject().getAsJsonObject("fields");
//                String name = list_fields.get("Name").getAsString();
//                int age = list_fields.get("Age").getAsInt();
//                String job = list_fields.get("Job").getAsString();
//                String message = list_fields.get("message").getAsString();
//
//                User user_list = new User(name, age, job, message);
//                System.out.println(user_list.getName() + ", " + user_list.getAge() + ", " + user_list.getJob() + ", " + user_list.getmessage());
//            }
//        } else {
//            System.out.println("Failed to list records.");
//        }

        // Update Record
//        JsonObject updatedFields = new JsonObject();
//        updatedFields.addProperty("Name", "New Name");
//        updatedFields.addProperty("Age", 100);
//        updatedFields.addProperty("Job", "New Job");
//        updatedFields.addProperty("message", "Hello, updated message");
//
//        String recordId = "rechb35j34Dx55b8y";
//        String response = Record.updateRecord(updatedFields, recordId, tableId, baseId, token);
//
//        if (response != null) {
//            System.out.println("Record updated successfully!");
//            // Optionally, you can print the updated record details
//            System.out.println(response);
//        } else {
//            System.out.println("Failed to update record.");
//        }





    }
}



