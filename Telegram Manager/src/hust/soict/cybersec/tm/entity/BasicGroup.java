package hust.soict.cybersec.tm.entity;

import java.util.ArrayList;
import java.util.List;

import org.drinkless.tdlib.TdApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class BasicGroup {
    private long id;
    private long chatId;
    private String groupName;
    private TdApi.ChatPermissions permissions;
    
    private int messageAutoDeleteTime;
    private List<Long> adminIds = new ArrayList<>();
    private int memberCount;
    private List<Long> memberIds = new ArrayList<>();
    private String description;
    private String inviteLink;
    
    private List<TdApi.MessageContent> messages = new ArrayList<>();
    

    public BasicGroup()
    {

    }

    public BasicGroup(long id,
                      long chatId,
                      String groupName, 
                      TdApi.ChatPermissions permissions, 
                    
                      int messageAutoDeleteTime,
                      List<Long> adminIds,
                      int memberCount,
                      List<Long> memberIds,
                      String description,
                      String inviteLink,
                    
                      List<TdApi.MessageContent> messages)
    {
        this.id = id;
        this.chatId = chatId;
        this.groupName = groupName;
        this.permissions = permissions;
        
        this.messageAutoDeleteTime = messageAutoDeleteTime;
        this.adminIds = adminIds;
        this.memberCount = memberCount;
        this.memberIds = memberIds;
        this.description = description;
        this.inviteLink = inviteLink;
        
        this.messages = messages;
    }

    public long getId() {
        return id;
    }
    
    public long getChatId() {
        return chatId;
    }

    public List<Long> getMemberIds() {
        
        return this.memberIds;
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

    public List<Long> getAdminIds() {
        return adminIds;
    }

    public String getInviteLink() {
        return inviteLink;
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
    
    public void setId(long id) {
        this.id = id;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPermissions(TdApi.ChatPermissions permissions) {
        this.permissions = permissions;
    }
    
    public void setMessageAutoDeleteTime(int messageAutoDeleteTime) {
        this.messageAutoDeleteTime = messageAutoDeleteTime;
    }

    public void addAdminId(Long id)
    {
        this.adminIds.add(id);
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setMemberId(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    @Override
    public String toString()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        return gson.toJson(this);
    }
    
    public JsonObject toJson(){
        JsonObject fields = new JsonObject();
        Gson gson = new Gson();
        fields.addProperty("Id", String.valueOf(getId()));
        fields.addProperty("ChatID", getChatId());
        fields.addProperty("GroupName", getGroupName());
        fields.addProperty("Permission", gson.toJson(permissions));
        fields.addProperty("MessageAutoDeleteTime", getMessageAutoDeleteTime());
        fields.addProperty("MemberCount", getMemberCount());
        fields.addProperty("Description", getDescription());
        fields.addProperty("InviteLink", getInviteLink());
        fields.addProperty("Message", gson.toJson(messages));

        JsonArray AdminIDs = new JsonArray();
        for(Long ID: adminIds){
            AdminIDs.add(ID);
        }
        JsonArray MemberIDs = new JsonArray();
        for(Long ID: memberIds){
            MemberIDs.add(ID);
        }
        fields.add("AdminIDs", AdminIDs);
        fields.add("MemberIDs", MemberIDs);

        return fields;
    }
}
