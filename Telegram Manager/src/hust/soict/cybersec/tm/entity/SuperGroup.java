package hust.soict.cybersec.tm.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drinkless.tdlib.TdApi;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SuperGroup 
{
    private long id;
    private long chatId;
    private String groupName;
    private TdApi.ChatPermissions permissions;
    
    private int messageAutoDeleteTime;
    
    private int memberCount;
    
    private boolean isAllHistoryAvailable;
    private Set<Long> adminIds = new HashSet<>();
    private Set<Long> memberIds = new HashSet<>();
    private String description;
    private String inviteLink;
    
    private List<TdApi.MessageContent> messages = new ArrayList<>();


    public SuperGroup()
    {

    }


    public SuperGroup(long id,
                      long chatId,
                      String groupName,
                      TdApi.ChatPermissions permissions,
                    
                      int messageAutoDeleteTime,
                    
                      int memberCount,
                    
                      boolean isAllHistoryAvailable,
                      Set<Long> adminIds,
                      Set<Long> memberIds,
                      String description,
                      String inviteLink,
                    
                      List<TdApi.MessageContent> messages)
    {
        this.id = id;
        this.chatId = chatId;
        this.groupName = groupName;
        this.permissions = permissions;
        
        this.messageAutoDeleteTime = messageAutoDeleteTime;
       
        this.memberCount = memberCount;
        
        this.isAllHistoryAvailable = isAllHistoryAvailable;
        this.adminIds = adminIds;
        this.memberIds = memberIds;
        this.description = description;
        this.inviteLink = inviteLink;
        
        this.messages = messages;
    }


    public long getId() {
        return id;
    }

    public Set<Long> getMemberIds() {
        return memberIds;
    }

    public Set<Long> getAdminIds() {
        return adminIds;
    }
    
    public boolean getIsAllHistoryAvailable() {
        return isAllHistoryAvailable;
    }
    public int getMessageAutoDeleteTime() {
        return messageAutoDeleteTime;
    }
    public String getDescription() {
        return description;
    }
    public int getMemberCount() {
        return memberCount;
    }
    public TdApi.ChatPermissions getPermissions() {
        return permissions;
    }
    public List<TdApi.MessageContent> getMessages() {
        return messages;
    }
    public String getGroupName() {
        return groupName;
    }

    public long getChatId() {
        return chatId;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public JsonObject toJson(){
        Gson gson = new Gson();
        JsonObject fields = new JsonObject();
        fields.addProperty("Id", String.valueOf(getId()));
        fields.addProperty("ChatID", getChatId());
        fields.addProperty("GroupName", getGroupName());
        fields.addProperty("Permission", gson.toJson(permissions));
        fields.addProperty("MessageAutoDeleteTime", getMessageAutoDeleteTime());
        fields.addProperty("MemberCount", getMemberCount());
        fields.addProperty("IsAllHistoryAvailable", getIsAllHistoryAvailable());
        fields.addProperty("Description", getDescription());
        fields.addProperty("InviteLink", getInviteLink());
        fields.addProperty("Message", gson.toJson(messages) );
        JsonArray admidIDs = new JsonArray();
        for(Long ID : adminIds){
            admidIDs.add(ID);
        }
        JsonArray memberIDs = new JsonArray();
        for(Long ID : memberIds){
            memberIDs.add(ID);
        }
        fields.add("AdminIDs", admidIDs);
        fields.add("MemberIDs", memberIDs);

        return fields;
    }
}
