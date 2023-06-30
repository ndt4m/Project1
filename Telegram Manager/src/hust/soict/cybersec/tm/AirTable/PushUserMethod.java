package hust.soict.cybersec.tm.AirTable;

import com.google.gson.JsonObject;
import hust.soict.cybersec.tm.entity.User;

import java.util.List;

public class PushUserMethod {
    public void pushMethod(List<User> List_of_user){
        if(List_of_user != null){
            System.out.println("UserList exists");
            for (User user : List_of_user){
                System.out.println(user.getDisplayName());

                //Convert User attributes to JSON
                JsonObject fields = new JsonObject();
                fields.addProperty("ID", user.getId());
                fields.addProperty("FirstName", user.getFirstName());
                fields.addProperty("LastName", user.getLastName());
                fields.addProperty("UserName", user.getUserName());
                fields.addProperty("PhoneNumber", user.getPhoneNumber());
                fields.addProperty("IsScam", user.isScam());
                fields.addProperty("IsFake", user.isFake());
                System.out.println(fields);

                //Set table AirTable parameters
                String tableId = "tblJZJWSIFCNzjFyW";
                String baseId = "appwZ9qRyIS0zKCQC";
                String token = "pat3qYiT5gazmDM8c.e4c6ce1998c288b8ff2354a70e5e41c8957b47aef3edc08acec7dfd2eebcaa14";

                //Create Records
                String response = Record.createRecord(fields, tableId, baseId, token);

                if (response != null) {
                    System.out.println("Record created successfully!");
                    //System.out.println("Response: " + response);
                } else {
                    System.out.println("Failed to create record.");
                }
            }
        }
    }
}
