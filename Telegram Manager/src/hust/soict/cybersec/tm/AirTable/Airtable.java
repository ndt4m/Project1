package hust.soict.cybersec.tm.airtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hust.soict.cybersec.tm.airtable.Table;
import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;

import java.util.ArrayList;
import java.util.List;

public class Airtable {
    private static final String userTableID = "tblJZJWSIFCNzjFyW";
    private static final String basicGroupTableID = "tblXzO7qCTPEWstFC";
    private static final String superGroupTableID = "tbl6VpMtlkGimF6VF";
    private static final String baseID = "appwZ9qRyIS0zKCQC";
    private static final String token = "pat3qYiT5gazmDM8c.e4c6ce1998c288b8ff2354a70e5e41c8957b47aef3edc08acec7dfd2eebcaa14";

    Table userTable;
    Table basicGroupTable;
    Table superGroupTable;

    public Airtable(){
        String response = Table.listTables(baseID, token);

        JsonArray listTable = JsonParser.parseString(response).getAsJsonObject().get("tables").getAsJsonArray();

        for (var table : listTable) {
            JsonObject tableJson = table.getAsJsonObject();
            String tableId = tableJson.get("id").getAsString();
            if (tableId.equals(userTableID)){
                userTable = new Table(tableJson, baseID, token);
            }
            if (tableId.equals(basicGroupTableID)){
                basicGroupTable = new Table(tableJson, baseID, token);
            }
            if (tableId.equals(superGroupTableID)){
                superGroupTable = new Table(tableJson, baseID, token);
            }
        }
    }

    private boolean pushUserTable(List<JsonObject> users){
        return userTable.pullAllRecord(users, baseID, token);
    }

    private boolean pushBasicGroupTable(List<JsonObject> basicGroups){
        List<JsonObject> listPush = new ArrayList<>();

        for (JsonObject jsonObject: basicGroups){
            JsonArray admins = jsonObject.get("AdminIDs").getAsJsonArray();

            JsonArray adminsPush = new JsonArray();

            for (JsonElement element : admins){
                Record record = userTable.getRecord(element.getAsString());
                if (record != null){
                    adminsPush.add(record.getId());
                }
            }

            jsonObject.add("Admin", adminsPush);


            JsonArray members = jsonObject.get("MemberIDs").getAsJsonArray();

            JsonArray membersPush = new JsonArray();

            for (JsonElement element : members){
                Record record = userTable.getRecord(element.getAsString());
                if (record != null){
                    membersPush.add(record.getId());
                }
            }

            jsonObject.add("User", membersPush);

            jsonObject.remove("AdminIDs");
            jsonObject.remove("MemberIDs");

            listPush.add(jsonObject);

        }
        return basicGroupTable.pullAllRecord(listPush, baseID, token);

    }


    private boolean pushSuperGroupTable(List<JsonObject> superGroups){
        List<JsonObject> listPush = new ArrayList<>();

        for (JsonObject jsonObject: superGroups){
            JsonArray admins = jsonObject.get("AdminIDs").getAsJsonArray();

            JsonArray adminsPush = new JsonArray();

            for (JsonElement element : admins){
                Record record = userTable.getRecord(element.getAsString());
                if (record != null){
                    adminsPush.add(record.getId());
                }
            }

            jsonObject.add("Admin", adminsPush);


            JsonArray members = jsonObject.get("MemberIDs").getAsJsonArray();

            JsonArray membersPush = new JsonArray();

            for (JsonElement element : members){
                Record record = userTable.getRecord(element.getAsString());
                if (record != null){
                    membersPush.add(record.getId());
                }
            }

            jsonObject.add("User", membersPush);
            jsonObject.remove("AdminIDs");
            jsonObject.remove("MemberIDs");

            listPush.add(jsonObject);

        }
        return superGroupTable.pullAllRecord(listPush, baseID, token);
    }


    public boolean push(List<User> users, List<BasicGroup> basicGroups, List<SuperGroup> superGroups){
        List<JsonObject> usersJson = new ArrayList<>();
        List<JsonObject> basicGroupsJson = new ArrayList<>();
        List<JsonObject> superGroupsJson = new ArrayList<>();

        for (User user : users){
            usersJson.add(user.toJson());
        }

        for (BasicGroup basicGroup : basicGroups){
            basicGroupsJson.add(basicGroup.toJson());
        }

        for (SuperGroup superGroup : superGroups){
            superGroupsJson.add(superGroup.toJson());
        }

        boolean result = true;
        result &= pushUserTable(usersJson);
        userTable.dropRecord(usersJson,baseID, token);
        userTable.syncRecord(baseID, token);
        result &= pushBasicGroupTable(basicGroupsJson);
        basicGroupTable.dropRecord(basicGroupsJson,baseID,token);
        basicGroupTable.syncRecord(baseID,token);
        result &= pushSuperGroupTable(superGroupsJson);
        superGroupTable.dropRecord(superGroupsJson,baseID,token);
        superGroupTable.syncRecord(baseID,token);



        return result;
    }

}
