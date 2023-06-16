package hust.soict.cybersec.tm.entity;

import java.util.ArrayList;
import java.util.List;

import org.drinkless.tdlib.TdApi;

public class BasicGroup {
    private long id;
    private String groupName;
    private TdApi.ChatPermissions permissions;
    private boolean canBeDeletedOnlyForSelf;
    private boolean canBeDeletedOnlyForAllUsers;
    private boolean defaultDisableNotification;
    private int messageAutoDeleteTime;
    private List<Long> adminId = new ArrayList<>();
    private int memberCount;
    private List<Long> memberId = new ArrayList<>();
    private String description;
    private String inviteLink;
    private List<TdApi.BotCommands> botCommands = new ArrayList<>();
    
    public long getId() {
        return id;
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

    public void setCanBeDeletedOnlyForSelf(boolean canBeDeletedOnlyForSelf) {
        this.canBeDeletedOnlyForSelf = canBeDeletedOnlyForSelf;
    }

    public void setCanBeDeletedOnlyForAllUsers(boolean canBeDeletedOnlyForAllUsers) {
        this.canBeDeletedOnlyForAllUsers = canBeDeletedOnlyForAllUsers;
    }

    public void setDefaultDisableNotification(boolean defaultDisableNotification) {
        this.defaultDisableNotification = defaultDisableNotification;
    }
    
    public void setMessageAutoDeleteTime(int messageAutoDeleteTime) {
        this.messageAutoDeleteTime = messageAutoDeleteTime;
    }

    public void addAdminId(Long id)
    {
        this.adminId.add(id);
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public void setMemberId(List<Long> memberId) {
        this.memberId = memberId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInviteLink(String inviteLink) {
        this.inviteLink = inviteLink;
    }

    public void setBotCommands(List<TdApi.BotCommands> botCommands) {
        this.botCommands = botCommands;
    }
}
