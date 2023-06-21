package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.entity.SuperGroup;

public class SuperGroupInfoCrawler extends Crawler<SuperGroup>
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
    private Set<Long> adminIds = new HashSet<>();
    private Set<Long> memberIds = new HashSet<>();
    private String description = "Không rõ";
    private TdApi.ChatInviteLink inviteLink = null;
    private List<TdApi.BotCommands> botCommands = new ArrayList<>();
    private List<TdApi.Message> messages = new ArrayList<>();

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
        adminIds = new HashSet<>();
        memberCount = -1;
        memberIds = new HashSet<>();
        description = "Không rõ";
        inviteLink = null;
        botCommands = new ArrayList<>();
        isChannel = -1;
        isBroadCastGroup = -1;
        isFake = -1;
        isScam = -1;
        messages = new ArrayList<>();
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
            if (canGetMembers == 1 && (isChannel == 0 || adminIds.contains(2134816269l)))
            {   
                blockingSend(new TdApi.GetSupergroupMembers(id, null, 0, 200), updateSuperGroupHandler);
                //System.out.println(memberCount);
                for (int i = 0; i < Math.min((int) Math.ceil(memberCount / 200) + 1, (int) 10000/200); i++)
                {   
                    System.out.print(i + ". ");
                    blockingSend(new TdApi.GetSupergroupMembers(id, null, 200 * i, 200), updateSuperGroupHandler);
                    //Thread.sleep(100);
                    System.out.println("memSize: " + memberIds.size());
                }
                
                blockingSend(new TdApi.GetChatHistory(chat.getKey(), 0, 0, 100, false), updateSuperGroupHandler);
                int oldSize = messages.size();
                while (messages.size() <= 10000)
                {
                    System.out.println("size: " + messages.size());
                    blockingSend(new TdApi.GetChatHistory(chat.getKey(), messages.get(messages.size() - 1).id, 0, 100, false), updateSuperGroupHandler);
                    if (oldSize != messages.size())
                    {
                        oldSize = messages.size();
                        continue;
                    }
                    break;
                }
                this.addCollection(new SuperGroup(id, 
                                              groupName, 
                                              permissions, 
                                              (canBeDeletedOnlyForSelf == 1) ? true : false, 
                                              (canBeDeletedForAllUsers == 1) ? true : false, 
                                              (defaultDisableNotification == 1) ? true : false, 
                                              messageAutoDeleteTime, 
                                              (isChannel == 1) ? true : false, 
                                              (isBroadCastGroup == 1) ? true : false, 
                                              (isFake == 1) ? true : false, 
                                              (isScam == 1) ? true : false, 
                                              memberCount, 
                                              (canGetMembers == 1) ? true : false, 
                                              (isAllHistoryAvailable == 1) ? true : false, 
                                              adminIds, 
                                              memberIds, 
                                              description, 
                                              inviteLink, 
                                              botCommands, 
                                              messages));
                //System.out.println(messages.size());
            }
            // if (!adminIds.isEmpty())
            // {
            //     System.out.println("superGroup Id: " + groupName + "======" + adminIds);
            //     System.out.println(memberIds);
            // }
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
                        adminIds.add(chatAdmin.userId);
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
                    //System.out.println("i'm here");
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
                case TdApi.Messages.CONSTRUCTOR:
                    for (TdApi.Message m: ((TdApi.Messages) object).messages)
                    {
                        messages.add(m);
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
