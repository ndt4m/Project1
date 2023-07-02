package hust.soict.cybersec.tm.entity;

import java.util.ArrayList;
import java.util.List;

import org.drinkless.tdlib.TdApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BasicGroup {
    private long id;
    private long chatId;
    private String groupName;
    private TdApi.ChatPermissions permissions;
    // private boolean canBeDeletedOnlyForSelf;
    // private boolean canBeDeletedForAllUsers;
    // private boolean defaultDisableNotification;
    private int messageAutoDeleteTime;
    private List<Long> adminIds = new ArrayList<>();
    private int memberCount;
    private List<Long> memberIds = new ArrayList<>();
    private String description;
    private String inviteLink;
    // private List<TdApi.BotCommands> botCommands = new ArrayList<>();
    private List<TdApi.Message> messages = new ArrayList<>();
    

    public BasicGroup()
    {

    }

    public BasicGroup(long id,
                      long chatId,
                      String groupName, 
                      TdApi.ChatPermissions permissions, 
                    //   boolean canBeDeletedForAllUsers, 
                    //   boolean canBeDeletedOnlyForSelf,
                    //   boolean defaultDisableNotification,
                      int messageAutoDeleteTime,
                      List<Long> adminIds,
                      int memberCount,
                      List<Long> memberIds,
                      String description,
                      String inviteLink,
                    //   List<TdApi.BotCommands> botCommands,
                      List<TdApi.Message> messages)
    {
        this.id = id;
        this.chatId = chatId;
        this.groupName = groupName;
        this.permissions = permissions;
        // this.canBeDeletedForAllUsers = canBeDeletedForAllUsers;
        // this.canBeDeletedOnlyForSelf = canBeDeletedOnlyForSelf;
        // this.defaultDisableNotification = defaultDisableNotification;
        this.messageAutoDeleteTime = messageAutoDeleteTime;
        this.adminIds = adminIds;
        this.memberCount = memberCount;
        this.memberIds = memberIds;
        this.description = description;
        this.inviteLink = inviteLink;
        // this.botCommands = botCommands;
        this.messages = messages;
    }

    public long getId() {
        return id;
    }
    
    public long getChatId() {
        return chatId;
    }

    public List<Long> getMemberIds() {
        //System.out.println(this.memberIds + "o trong basic group class");
        return this.memberIds;
    }

    public TdApi.ChatPermissions getPermissions() {
        return permissions;
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

    // public void setCanBeDeletedOnlyForSelf(boolean canBeDeletedOnlyForSelf) {
    //     this.canBeDeletedOnlyForSelf = canBeDeletedOnlyForSelf;
    // }

    // public void setCanBeDeletedOnlyForAllUsers(boolean canBeDeletedForAllUsers) {
    //     this.canBeDeletedForAllUsers = canBeDeletedForAllUsers;
    // }

    // public void setDefaultDisableNotification(boolean defaultDisableNotification) {
    //     this.defaultDisableNotification = defaultDisableNotification;
    // }
    
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
    // public void setBotCommands(List<TdApi.BotCommands> botCommands) {
    //     this.botCommands = botCommands;
    // }
}
