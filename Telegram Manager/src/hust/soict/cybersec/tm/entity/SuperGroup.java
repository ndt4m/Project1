package hust.soict.cybersec.tm.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.drinkless.tdlib.TdApi;

public class SuperGroup 
{
    private long id;
    private long chatId;
    private String groupName;
    private TdApi.ChatPermissions permissions;
    // private boolean canBeDeletedOnlyForSelf;
    // private boolean canBeDeletedForAllUsers;
    // private boolean defaultDisableNotification;
    private int messageAutoDeleteTime;
    // private boolean isChannel;
    // private boolean isBroadCastGroup;
    // private boolean isFake;
    // private boolean isScam;
    private int memberCount;
    // private boolean canGetMembers;
    private boolean isAllHistoryAvailable;
    private Set<Long> adminIds = new HashSet<>();
    private Set<Long> memberIds = new HashSet<>();
    private String description;
    private String inviteLink;
    // private List<TdApi.BotCommands> botCommands = new ArrayList<>();
    private List<TdApi.Message> messages = new ArrayList<>();


    public SuperGroup()
    {

    }


    public SuperGroup(long id,
                      long chatId,
                      String groupName,
                      TdApi.ChatPermissions permissions,
                    //   boolean canBeDeletedOnlyForSelf,
                    //   boolean canBeDeletedForAllUsers,
                    //   boolean defaultDisableNotification,
                      int messageAutoDeleteTime,
                    //   boolean isChannel,
                    //   boolean isBroadCastGroup,
                    //   boolean isFake,
                    //   boolean isScam,
                      int memberCount,
                    //   boolean canGetMembers,
                      boolean isAllHistoryAvailable,
                      Set<Long> adminIds,
                      Set<Long> memberIds,
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
        // this.isChannel = isChannel;
        // this.isBroadCastGroup = isBroadCastGroup;
        // this.isFake = isFake;
        // this.isScam = isScam;
        this.memberCount = memberCount;
        // this.canGetMembers = canGetMembers;
        this.isAllHistoryAvailable = isAllHistoryAvailable;
        this.adminIds = adminIds;
        this.memberIds = memberIds;
        this.description = description;
        this.inviteLink = inviteLink;
        // this.botCommands = botCommands;
        this.messages = messages;
    }


    public long getId() {
        return id;
    }

    public Set<Long> getMemberIds() {
        return memberIds;
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
}
