package hust.soict.cybersec.tm.airtable;

import com.google.gson.JsonObject;
import hust.soict.cybersec.tm.entity.BasicGroup;

import java.util.List;

public class PushBasicGroupMethod {
    public void pushMethod(List<BasicGroup> List_of_Basic_Group){
        if(List_of_Basic_Group != null){
            System.out.println("BasicGroup exists");
            for(BasicGroup basicGroup : List_of_Basic_Group){

                //Convert BasicGroup attributes to JSON
                JsonObject fields = new JsonObject();
                fields.addProperty("ID", basicGroup.getId());
                fields.addProperty("ChatID", basicGroup.getChatId());
                fields.addProperty("GroupName", basicGroup.getGroupName());
                fields.addProperty("MessageAutoDeleteTime", basicGroup.getMessageAutoDeleteTime());
                fields.addProperty("MemberCount", basicGroup.getMemberCount());

                //Set table AirTable parameters
                String tableId = "tblXzO7qCTPEWstFC";
                String baseId = "appwZ9qRyIS0zKCQC";
                String token = "pat3qYiT5gazmDM8c.e4c6ce1998c288b8ff2354a70e5e41c8957b47aef3edc08acec7dfd2eebcaa14";

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
