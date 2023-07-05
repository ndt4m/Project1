//package hust.soict.cybersec.tm.airtable.pushmethod;
//
//import hust.soict.cybersec.tm.airtable.Record;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import hust.soict.cybersec.tm.entity.User;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class PushUserMethod {
//    public void pushMethod(List<User> List_of_user){
//        if(List_of_user != null){
//            System.out.println("UserList exists");
//            for (User user : List_of_user){
//                System.out.println(user.getDisplayName());
//
//                //Convert User attributes to JSON
//                JsonObject fields = new JsonObject();
//                fields.addProperty("ID", user.getId());
//                fields.addProperty("FirstName", user.getFirstName());
//                fields.addProperty("LastName", user.getLastName());
//                fields.addProperty("UserName", user.getUserName());
//                fields.addProperty("PhoneNumber", user.getPhoneNumber());
//                fields.addProperty("IsScam", user.getIsScam());
//                fields.addProperty("IsFake", user.getIsFake());
//                fields.addProperty("LanguageCode", user.getLanguageCode());
//                fields.addProperty("Type", user.getType());
//                // Convert user_basic_group_ids to comma-separated string
//                String basicGroupIds = user.getUser_basic_group_ids()
//                        .stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(","));
//                fields.addProperty("InBasicGroup", basicGroupIds);
//
//                // Convert user_super_group_ids to comma-separated string
//                String superGroupIds = user.getUser_super_group_ids()
//                        .stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(","));
//                fields.addProperty("InSuperGroup", superGroupIds);
//
//                String messages = user.get()
//                        .stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(","));
//                fields.addProperty("InSuperGroup", superGroupIds);
//
//
//                System.out.println(fields);
//
//                //Set table AirTable parameters
//                String tableId = "tblJZJWSIFCNzjFyW";
//                String baseId = "appwZ9qRyIS0zKCQC";
//                String token = "pat3qYiT5gazmDM8c.e4c6ce1998c288b8ff2354a70e5e41c8957b47aef3edc08acec7dfd2eebcaa14";
//
//                //Create Records
//                String response = Record.createRecord(fields, tableId, baseId, token);
//
//                if (response != null) {
//                    System.out.println("Record created successfully!");
//                    //System.out.println("Response: " + response);
//                } else {
//                    System.out.println("Failed to create record.");
//                }
//            }
//        }
//    }
//}