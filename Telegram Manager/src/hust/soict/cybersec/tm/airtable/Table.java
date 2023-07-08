package hust.soict.cybersec.tm.airtable;




import com.google.gson.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;




import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Table{
    private int numChanges;
    private final String id;
    private final String name;
    private final List<Field> fieldList = new ArrayList<>();
    private final List<Record> records = new ArrayList<>();

    
    public Table(JsonObject table, String baseId, String token) {
        this.id = table.get("id").getAsString();
        this.name = table.get("name").getAsString();
        table.get("fields").getAsJsonArray().forEach(field -> this.fieldList.add(new Field(field.getAsJsonObject())));

        
        syncRecord(baseId, token);
        
    }

    protected void syncRecord(String baseId, String token) {
        records.clear();
        String rec0rds = Record.listRecords(id, baseId, token);
        if (rec0rds == null) {
            System.out.println("Error: Could not get records for table: " + name);
        } else {
            JsonObject recordsJson = new Gson().fromJson(rec0rds, JsonObject.class);
            JsonArray listRecords = recordsJson.get("records").getAsJsonArray();
            listRecords.forEach(rec -> this.records.add(new Record(rec.getAsJsonObject())));
        }
    }

    
    protected String getName() {
        return this.name;
    }
    protected String getId() {
        return this.id;
    }
    protected int getNumChanges() {
        return numChanges;
    }
    protected int getNumRecords() {
        return records.size();
    }

    
    protected Field getField(String name) {
        for (Field field : this.fieldList) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }
    
    protected List<Record> getRecords() {
        return records;
    }

    protected boolean addField(JsonObject field, String baseId, String token) {
        String fieldCreate = Field.createField(field, id, baseId, token);
        if (fieldCreate == null) {
            System.out.println("Error: Could not create field: " + field.get("name").getAsString() + " in table: " + name);
            return false;
        }
        JsonObject fieldJson = JsonParser.parseString(fieldCreate).getAsJsonObject();
        this.fieldList.add(new Field(fieldJson));
        
        return true;
    }

    
    private boolean updateRecord(JsonObject recordFields, Record rec0rd, String baseId, String token) {
        String updatedRecord = Record.updateRecord(recordFields, rec0rd.getId(), id, baseId, token);
        if (updatedRecord == null) {
            System.out.println("Error: Could not update record: " + rec0rd.getIdFieldVal() + " in table: " + name);
            return false;
        }
        JsonObject recordJson = JsonParser.parseString(updatedRecord).getAsJsonObject();
        
        records.remove(rec0rd);
        
        records.add(new Record(recordJson));
        
        return true;
    }
    private boolean addRecord(JsonObject recordFields, String baseId, String token) {
        String createdRecord = Record.createRecord(recordFields, id, baseId, token);
        if (createdRecord == null) {
            System.out.println("Error: Could not create record: " + recordFields.get("Id").getAsString() + " in table: " + name + "has id: " + id + " baseId: " + baseId);
            return false;
        }
        JsonObject recordJson = new Gson().fromJson(createdRecord, JsonObject.class);
        JsonArray listRecords = recordJson.get("records").getAsJsonArray();

        
        listRecords.forEach(rec -> this.records.add(new Record(rec.getAsJsonObject())));

        
        return true;
    }
    protected Record getRecord(String idFieldVal) {
        for (Record rec : this.records) {
            if (rec.getIdFieldVal().equals(idFieldVal)) {
                return rec;
            }
        }
        return null;
    }
    
    public boolean pushRecord(JsonObject newRecordFields, String baseId, String token) {
        Record oldRecord = getRecord(newRecordFields.get("Id").getAsString());
        if (oldRecord == null) {
            if (addRecord(newRecordFields, baseId, token)){
                numChanges++;
                return true;
            }
            
            return false;
        }
    
        if (oldRecord.equals(newRecordFields)) {
            return true;
        }
        
        if (updateRecord(newRecordFields, oldRecord, baseId, token)) {
            numChanges++;
            return true;
        }
        
        return false;
    }

    protected boolean pushAllRecord(List<JsonObject> newEntityRecordFieldsList, String baseId, String token) {
        numChanges = 0;
        for (JsonObject newRecordFields : newEntityRecordFieldsList) {
            if (!pushRecord(newRecordFields, baseId, token)) {
                
                return false;
            }
        }
        
        return true;
    }

    protected void dropRecord(List<JsonObject> newEntityRecordFieldsList, String baseId, String token) {
        List<Record> dropList = new ArrayList<>();
        for (Record rec : this.records) {
            
            boolean isExist = false;
            for (JsonObject fields : newEntityRecordFieldsList) {
                if (rec.getIdFieldVal().equals(fields.get("Id").getAsString())) {
                    isExist = true;
                    break;
                }
            }
            
            if (!isExist) {
                if (Record.dropRecord(rec.getId(), id, baseId, token)) {
                    
                    dropList.add(rec);
                } else {
                    System.out.println("Error: Could not delete record: " + rec.getIdFieldVal() + " in table: " + name);
                }
            }
        }
        this.records.removeAll(dropList);
    }

    
    protected static String listTables(String baseId, String token) {
        String url = "https://api.airtable.com/v0/meta/bases/" + baseId + "/tables";
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(url);
            
            get.setHeader("Authorization", "Bearer " + token);
            ClassicHttpResponse response = client.execute(get);
            if (response.getCode() != 200) {
                System.out.println("Error: Could not list tables");
                return null;
            }
            
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            System.out.println("Error: Could not list tables due to exception: " + e.getMessage());
            return null;
        }
    }
    protected static String createTable(String name, JsonArray fields, String baseId, String token){
        String url = "https://api.airtable.com/v0/meta/bases/" + baseId + "/tables";
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + token);
            post.setHeader("Content-Type", "application/json");

            JsonObject body = new JsonObject();
            body.addProperty("name", name);
            body.add("fields", fields);

            post.setEntity(new StringEntity(body.toString()));

            ClassicHttpResponse response = client.execute(post);
            if (response.getCode() != 200) {
                System.out.println("Error: Could not create table: " + name);
                return null;
            }
            
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            System.out.println("Error: Could not create table: " + name + " with message: " + e.getMessage());
            return null;
        }
    }

}

