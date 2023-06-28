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
import java.util.ArrayList;
import java.util.List;

public class Record_GPT {
    ArrayList<String> recordID_list = new ArrayList<>();
    protected static String createRecord_GPT(JsonObject fields, String tableId, String baseId, String token) {
        String url = "https://api.airtable.com/v0/" + baseId + "/" + tableId;

        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            JsonObject body = new JsonObject();
            body.add("fields", fields);

            JsonArray records = new JsonArray();
            records.add(body);

            JsonObject fullBody = new JsonObject();
            fullBody.add("records", records);

            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + token);
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
    protected static boolean dropRecord_GPT(String recordId, String tableId, String baseId, String Token){
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
