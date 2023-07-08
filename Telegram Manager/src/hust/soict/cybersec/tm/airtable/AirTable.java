package hust.soict.cybersec.tm.airtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AirTable {
    private static final String userTableID;
    private static final String basicGroupTableID;
    private static final String superGroupTableID;
    private static final String baseID;
    private static final String token;

    private static final Properties AIRTABLE = new Properties();

    static {
        try {
            AIRTABLE.load(new FileInputStream("airtable.properties"));
        } catch (IOException e) {
            
        }
        userTableID = AIRTABLE.getProperty("userTableID");
        basicGroupTableID = AIRTABLE.getProperty("basicGroupTableID");
        superGroupTableID = AIRTABLE.getProperty("superGroupTableID");
        baseID = AIRTABLE.getProperty("baseID");
        token = AIRTABLE.getProperty("token");
    }

    private Table atUserTable;
    private Table atBasicGroupTable;
    private Table atSuperGroupTable;

    public AirTable(){
        
        String response = Table.listTables(baseID, token);

        JsonArray listTable = JsonParser.parseString(response).getAsJsonObject().get("tables").getAsJsonArray();
        
        for (var table : listTable) {
            JsonObject tableJson = table.getAsJsonObject();
            String tableId = tableJson.get("id").getAsString();
            if (tableId.equals(userTableID)){
                atUserTable = new Table(tableJson, baseID, token);
            }
            if (tableId.equals(basicGroupTableID)){
                atBasicGroupTable = new Table(tableJson, baseID, token);
            }
            if (tableId.equals(superGroupTableID)){
                atSuperGroupTable = new Table(tableJson, baseID, token);
            }
        }
    }

    private boolean push2UserTable(List<JsonObject> usersJsonRecordFileds){
        List<JsonObject> listPush = new ArrayList<>();

        for (JsonObject jsonObject: usersJsonRecordFileds)
        {
            JsonArray memberOfBasicGroup = jsonObject.get("bsGroupsOfUser").getAsJsonArray();
            JsonArray memberOfBasicGroupPush = new JsonArray();
            for (JsonElement element: memberOfBasicGroup)
            {
                Record record = atBasicGroupTable.getRecord(element.getAsString());
                if (record != null)
                {
                    memberOfBasicGroupPush.add(record.getId());
                }
            }
            jsonObject.add("MemberOfBasicGroup", memberOfBasicGroupPush);

            JsonArray memberOfSuperGroup = jsonObject.get("spGroupsOfUser").getAsJsonArray();
            JsonArray memberOfSuperGroupPush = new JsonArray();
            for (JsonElement element: memberOfSuperGroup)
            {
                Record record = atSuperGroupTable.getRecord(element.getAsString());
                if (record != null)
                {
                    memberOfSuperGroupPush.add(record.getId());
                }
            }
            jsonObject.add("MemberOfSuperGroup", memberOfSuperGroupPush);

            JsonArray adminOfBasicGroup = jsonObject.get("adminOfBsGroup").getAsJsonArray();
            JsonArray adminOfBasicGroupPush = new JsonArray();
            for (JsonElement element: adminOfBasicGroup)
            {
                Record record = atBasicGroupTable.getRecord(element.getAsString());
                if (record != null)
                {
                    adminOfBasicGroupPush.add(record.getId());
                }
            }
            jsonObject.add("AdminOfBasicGroup", adminOfBasicGroupPush);

            JsonArray adminOfSuperGroup = jsonObject.get("adminOfSpGroup").getAsJsonArray();
            JsonArray adminOfSuperGroupPush = new JsonArray();
            for (JsonElement element: adminOfSuperGroup)
            {
                Record record = atSuperGroupTable.getRecord(element.getAsString());
                if (record != null)
                {
                    adminOfSuperGroupPush.add(record.getId());
                }
            }
            jsonObject.add("AdminOfSuperGroup", adminOfSuperGroupPush);

            jsonObject.remove("bsGroupsOfUser");
            jsonObject.remove("spGroupsOfUser");
            jsonObject.remove("adminOfBsGroup");
            jsonObject.remove("adminOfSpGroup");
            
            listPush.add(jsonObject);
        }
        return atUserTable.pushAllRecord(usersJsonRecordFileds, baseID, token);
    }

    private boolean push2BasicGroupTable(List<JsonObject> basicGroupsJsonRecordFields){
        List<JsonObject> listPush = new ArrayList<>();

        for (JsonObject jsonObject: basicGroupsJsonRecordFields){
            JsonArray admins = jsonObject.get("AdminIDs").getAsJsonArray();

            JsonArray adminsPush = new JsonArray();

            for (JsonElement element : admins){
                Record record = atUserTable.getRecord(element.getAsString());
                if (record != null){
                    adminsPush.add(record.getId());
                }
            }

            jsonObject.add("Admin", adminsPush);


            JsonArray members = jsonObject.get("MemberIDs").getAsJsonArray();

            JsonArray membersPush = new JsonArray();

            for (JsonElement element : members){
                Record record = atUserTable.getRecord(element.getAsString());
                if (record != null){
                    membersPush.add(record.getId());
                }
            }

            jsonObject.add("Member", membersPush);

            jsonObject.remove("AdminIDs");
            jsonObject.remove("MemberIDs");
            
            listPush.add(jsonObject);

        }
        return atBasicGroupTable.pushAllRecord(listPush, baseID, token);

    }


    private boolean push2SuperGroupTable(List<JsonObject> superGroupsJsonRecordFields){
        List<JsonObject> listPush = new ArrayList<>();

        for (JsonObject jsonObject: superGroupsJsonRecordFields){
            JsonArray admins = jsonObject.get("AdminIDs").getAsJsonArray();

            JsonArray adminsPush = new JsonArray();

            for (JsonElement element : admins){
                Record record = atUserTable.getRecord(element.getAsString());
                if (record != null){
                    adminsPush.add(record.getId());
                }
            }

            jsonObject.add("Admin", adminsPush);


            JsonArray members = jsonObject.get("MemberIDs").getAsJsonArray();

            JsonArray membersPush = new JsonArray();

            for (JsonElement element : members){
                Record record = atUserTable.getRecord(element.getAsString());
                if (record != null){
                    membersPush.add(record.getId());
                }
            }

            jsonObject.add("Member", membersPush);
            jsonObject.remove("AdminIDs");
            jsonObject.remove("MemberIDs");
            
            listPush.add(jsonObject);

        }
        return atSuperGroupTable.pushAllRecord(listPush, baseID, token);
    }


    public boolean push(List<User> users, List<BasicGroup> basicGroups, List<SuperGroup> superGroups){
        List<JsonObject> newUsersJsonRecordFiledsList = new ArrayList<>();
        List<JsonObject> newBasicGroupsJsonRecordFieldsList = new ArrayList<>();
        List<JsonObject> newSuperGroupsJsonRecordFieldsList = new ArrayList<>();

        for (User user : users){
            newUsersJsonRecordFiledsList.add(user.toJson());
        }
        for (BasicGroup basicGroup : basicGroups){
            newBasicGroupsJsonRecordFieldsList.add(basicGroup.toJson());
        }

        for (SuperGroup superGroup : superGroups){
            newSuperGroupsJsonRecordFieldsList.add(superGroup.toJson());
        }

        boolean result = true;
        result &= push2UserTable(newUsersJsonRecordFiledsList);
        atUserTable.dropRecord(newUsersJsonRecordFiledsList,baseID, token);
        
        result &= push2BasicGroupTable(newBasicGroupsJsonRecordFieldsList);
        atBasicGroupTable.dropRecord(newBasicGroupsJsonRecordFieldsList,baseID,token);
        
        result &= push2SuperGroupTable(newSuperGroupsJsonRecordFieldsList);
        atSuperGroupTable.dropRecord(newSuperGroupsJsonRecordFieldsList,baseID,token);
        
        
        return result;
    }

}

