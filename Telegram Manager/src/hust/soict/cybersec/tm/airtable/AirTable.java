package hust.soict.cybersec.tm.airtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;

import java.util.ArrayList;
import java.util.List;

public class AirTable {
    private static final String userTableID = "tbl4y5hY1zz38uq6M";
    private static final String basicGroupTableID = "tbltki8kkSaveHNOZ";
    private static final String superGroupTableID = "tblY2v9iAMjuwwQPV";
    private static final String baseID = "app3NFSG2GJiNJEZt";
    private static final String token = "patzon7RmPnoPUI8D.b2b67bd6454b5ba71b6d3f59d23dc393b88cade9a69022643287e42cc3600a29";

    Table atUserTable;
    Table atBasicGroupTable;
    Table atSuperGroupTable;

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

