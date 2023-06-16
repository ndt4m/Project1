package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class SuperGroupInfoCrawler extends Crawler
{
    private Client.ResultHandler updateSuperGroupHandler = new UpdateSuperGroupHandler();
    private Map<Long, TdApi.Supergroup> superGroups;
    
    private long id = 0l;
    private String groupName = "Không rõ";
    private TdApi.ChatPermissions permissions = null;
    private int canBeDeletedOnlyForSelf = -1;
    private int canBeDeletedForAllUsers = -1;
    private int defaultDisableNotification = -1;
    private int messageAutoDeleteTime = -1;
    private int isChannel = -1;
    private int isBroadCastGroup = -1;
    private int isFake = -1;
    private int isScam = -1;
    private int memberCount = -1;
    private int canGetMembers = -1;
    private int isAllHistoryAvailable = -1;
    private List<Long> adminId = new ArrayList<>();
    private List<Long> memberIds = new ArrayList<>();
    private String description = "Không rõ";
    private TdApi.ChatInviteLink inviteLink = null;
    private List<TdApi.BotCommands> botCommands = new ArrayList<>();

    public SuperGroupInfoCrawler()
    {

    }

    public SuperGroupInfoCrawler(Map<Long, TdApi.Supergroup> superGroups, Map<Long, TdApi.Chat> chats, Client client)
    {
        super(client, chats);
        this.superGroups = superGroups;
    }

    public void redefinedAttributes()
    {
        id = 0l;
        groupName = "Không rõ";
        permissions = null;
        canBeDeletedOnlyForSelf = -1;
        canBeDeletedForAllUsers = -1;
        defaultDisableNotification = -1;
        canGetMembers = -1;
        isAllHistoryAvailable = -1;
        messageAutoDeleteTime = -1;
        adminId = new ArrayList<>();
        memberCount = -1;
        memberIds = new ArrayList<>();
        description = "Không rõ";
        inviteLink = null;
        botCommands = new ArrayList<>();
        isChannel = -1;
        isBroadCastGroup = -1;
        isFake = -1;
        isScam = -1;
    }

    public void crawlSuperGroupInfo() throws InterruptedException
    {
        for (Map.Entry<Long, TdApi.Chat> chat : chats.entrySet())
        {
            if (chat.getValue().type.getConstructor() != TdApi.ChatTypeSupergroup.CONSTRUCTOR)
            {
                continue;
            }

            id = ((TdApi.ChatTypeSupergroup) chat.getValue().type).supergroupId;
            groupName = chat.getValue().title;
            permissions = chat.getValue().permissions;
            canBeDeletedOnlyForSelf = chat.getValue().canBeDeletedOnlyForSelf ? 1 : 0;
            canBeDeletedForAllUsers = chat.getValue().canBeDeletedForAllUsers ? 1 : 0;
            defaultDisableNotification = chat.getValue().defaultDisableNotification ? 1 : 0;
            messageAutoDeleteTime = chat.getValue().messageAutoDeleteTime;
            isChannel = superGroups.get(id).isChannel ? 1 : 0;
            isBroadCastGroup = superGroups.get(id).isBroadcastGroup ? 1 : 0;
            isScam = superGroups.get(id).isScam ? 1 : 0;
            isFake = superGroups.get(id).isFake ? 1 : 0;
            blockingSend(new TdApi.GetChatAdministrators(chat.getKey()), updateSuperGroupHandler);
            blockingSend(new TdApi.GetSupergroupFullInfo(id), updateSuperGroupHandler);
            if (canGetMembers == 1)
            {
                blockingSend(new TdApi.GetSupergroupMembers(id, null, 0, 200), updateSuperGroupHandler);
            }
            if (!adminId.isEmpty())
            {
                System.out.println("superGroup Id: " + groupName + "======" + adminId);
                System.out.println(memberIds);
            }
            redefinedAttributes();
        }
    }

    class UpdateSuperGroupHandler implements Client.ResultHandler
    {
        @Override
        public void onResult(TdApi.Object object)
        {
            switch (object.getConstructor())
            {
                case TdApi.ChatAdministrators.CONSTRUCTOR:
                    //System.out.println("ddddddddddddddd");
                    TdApi.ChatAdministrators chatAdministrators = (TdApi.ChatAdministrators) object;
                    for (TdApi.ChatAdministrator chatAdmin: chatAdministrators.administrators)
                    {
                        adminId.add(chatAdmin.userId);
                    }
                    break;
                case TdApi.SupergroupFullInfo.CONSTRUCTOR:
                    TdApi.SupergroupFullInfo supergroupFullInfo = (TdApi.SupergroupFullInfo) object;
                    memberCount = supergroupFullInfo.memberCount;
                    description = supergroupFullInfo.description;
                    canGetMembers = supergroupFullInfo.canGetMembers ? 1 : 0;
                    isAllHistoryAvailable = supergroupFullInfo.isAllHistoryAvailable ? 1 : 0;
                    inviteLink = supergroupFullInfo.inviteLink;
                    for (TdApi.BotCommands bc : supergroupFullInfo.botCommands)
                    {
                        botCommands.add(bc);
                    }
                    break;
                case TdApi.ChatMembers.CONSTRUCTOR:
                    TdApi.ChatMembers chatMembers = (TdApi.ChatMembers) object;
                    for (TdApi.ChatMember mem: chatMembers.members)
                    {
                        if (mem.memberId instanceof TdApi.MessageSenderChat)
                        {
                            memberIds.add(((TdApi.MessageSenderChat) mem.memberId).chatId);
                        }
                        else
                        {
                            memberIds.add(((TdApi.MessageSenderUser) mem.memberId).userId);
                        }
                    }
                    break;
                default:
                    //System.out.println(object.toString());
            }
            haveReceivedRespond = true;
            authorizationLock.lock();
            try {
                gotAuthorization.signal();
            } finally {
                authorizationLock.unlock();
            }
        }
    }
}
