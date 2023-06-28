package org.example.AirTable;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPatch;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.List;


public class Record {
    protected static String listRecords(String tableId, String baseId, String token) {
        //curl "https://api.airtable.com/v0/{baseId}/{tableIdOrName}" \
        //-H "Authorization: Bearer YOUR_TOKEN"
        String url = "https://api.airtable.com/v0/" + baseId + "/" + tableId;

        try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpGet get = new HttpGet(url);
            get.setHeader("Authorization", "Bearer " + token);
            get.setHeader("Content-Type", "application/json");

            ClassicHttpResponse response = client.execute(get);
            return EntityUtils.toString(response.getEntity());
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    protected static String createRecord(JsonObject fields, String tableId, String baseId, String Token){
        String url = "https://api.airtable.com/v0/" + baseId + "/" + tableId;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {

            JsonObject body = new JsonObject();
            body.add("fields", fields);

            JsonArray records = new JsonArray();
            records.add(body);

            JsonObject fullBody = new JsonObject();
            fullBody.add("records", records);

            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + Token);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(fullBody.toString()));

            ClassicHttpResponse response = client.execute(post);

            if (response.getCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                JsonArray createdRecords = jsonResponse.getAsJsonArray("records");

                if (createdRecords.size() > 0) {
                    JsonObject createdRecord = createdRecords.get(0).getAsJsonObject();
                    String recordId = createdRecord.get("id").getAsString();
                    System.out.println("Record created. Record ID: " + recordId);
                    return recordId;
                } else {
                    System.out.println("Failed to retrieve record ID from response.");
                    return null;
                }
            } else {
                System.out.println("Error creating record: " + response.getCode());
                System.out.println("fullBody: " + fullBody);
                return null;
            }
        } catch (IOException | ParseException e) {
            System.out.println("Error creating record: " + e.getMessage());
            return null;
        }
    }
    protected static String updateRecord(JsonObject fields, String recordId, String tableId, String baseId, String Token){
        String url = "https://api.airtable.com/v0/" + baseId + "/" + tableId + "/" + recordId;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPatch patch = new HttpPatch(url);
            patch.setHeader("Authorization", "Bearer " + Token);
            patch.setHeader("Content-Type", "application/json");

            JsonObject fullBody = new JsonObject();
            fullBody.add("fields", fields);
            patch.setEntity(new StringEntity(fullBody.toString()));

            ClassicHttpResponse response = client.execute(patch);

            if (response.getCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                return null;
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    protected static boolean dropRecord(String recordId, String tableId, String baseId, String Token){
        // curl -X DELETE "https://api.airtable.com/v0/{baseId}/{tableIdOrName}/{recordId}" \
        //-H "Authorization: Bearer YOUR_TOKEN"
        String url = "https://api.airtable.com/v0/" + baseId + "/" + tableId + "/" + recordId;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpDelete delete = new HttpDelete(url);
            delete.setHeader("Authorization", "Bearer " + Token);

            ClassicHttpResponse response = client.execute(delete);

            if (response.getCode() == 200) {
                System.out.println("Record " + recordId + " deleted");
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
