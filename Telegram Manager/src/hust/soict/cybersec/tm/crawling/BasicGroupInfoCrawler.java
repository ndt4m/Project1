package hust.soict.cybersec.tm.crawling;

import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.entity.BasicGroup;

import org.drinkless.tdlib.Client;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;



public class BasicGroupInfoCrawler extends Crawler<BasicGroup>
{
    private Client.ResultHandler updateBasicGroupHandler = new UpdateBasicGroupHandler();
    private Map<Long, TdApi.BasicGroup> basicGroups;

    private long id = 0l;
    private String groupName = "Không rõ";
    private TdApi.ChatPermissions permissions = null;
    private int canBeDeletedOnlyForSelf = -1;
    private int canBeDeletedForAllUsers = -1;
    private int defaultDisableNotification = -1;
    private int messageAutoDeleteTime = -1;
    private List<Long> adminIds = new ArrayList<>();
    private int memberCount = -1;
    private List<Long> memberIds = new ArrayList<>();
    private String description = "Không rõ";
    private TdApi.ChatInviteLink inviteLink = null;
    private List<TdApi.BotCommands> botCommands = new ArrayList<>();
    private List<TdApi.Message> messages = new ArrayList<>();

    public BasicGroupInfoCrawler(Map<Long, TdApi.BasicGroup> basicGroups, Map<Long, TdApi.Chat> chats, Client client)
    {
        super(client, chats);
        this.basicGroups = basicGroups;
    }

    public void redefinedAttributes()
    {
        id = 0l;
        groupName = "Không rõ";
        permissions = null;
        canBeDeletedOnlyForSelf = -1;
        canBeDeletedForAllUsers = -1;
        defaultDisableNotification = -1;
        messageAutoDeleteTime = -1;
        adminIds = new ArrayList<>();
        memberCount = -1;
        memberIds = new ArrayList<>();
        description = "Không rõ";
        inviteLink = null;
        botCommands = new ArrayList<>();
        messages = new ArrayList<>();
    }

    
    public void crawlBasicGroupInfo() throws InterruptedException
    {
        for (Map.Entry<Long, TdApi.Chat> chat : chats.entrySet())
        {
            if (chat.getValue().type.getConstructor() != TdApi.ChatTypeBasicGroup.CONSTRUCTOR)
            {
                continue;
            }
            
            id = ((TdApi.ChatTypeBasicGroup) chat.getValue().type).basicGroupId;
            groupName = chat.getValue().title;
            permissions = chat.getValue().permissions;
            canBeDeletedOnlyForSelf = chat.getValue().canBeDeletedOnlyForSelf ? 1 : 0;
            canBeDeletedForAllUsers = chat.getValue().canBeDeletedForAllUsers ? 1 : 0;
            defaultDisableNotification = chat.getValue().defaultDisableNotification ? 1 : 0;
            messageAutoDeleteTime = chat.getValue().messageAutoDeleteTime;
            memberCount = basicGroups.get(id).memberCount;

            blockingSend(new TdApi.GetChatAdministrators(chat.getKey()), updateBasicGroupHandler);
            //System.out.println("-----------------------------");
            blockingSend(new TdApi.GetBasicGroupFullInfo(id), updateBasicGroupHandler);
            
            blockingSend(new TdApi.GetChatHistory(chat.getKey(), 0, 0, 100, false), updateBasicGroupHandler);
            int oldSize = messages.size();
            while (messages.size() <= 10000)
            {
                blockingSend(new TdApi.GetChatHistory(chat.getKey(), messages.get(messages.size() - 1).id, 0, 100, false), updateBasicGroupHandler);
                if (oldSize != messages.size())
                {
                    oldSize = messages.size();
                    continue;
                }
                break;
            }
            System.out.println(messages.size());
            // System.out.println(messages.get(0));
            //System.out.println("=====" + memberIds);
            this.addCollection(new BasicGroup(id, 
                                              groupName, 
                                              permissions, 
                                              (canBeDeletedForAllUsers == 1) ? true : false, 
                                              (canBeDeletedOnlyForSelf == 1) ? true : false, 
                                              (defaultDisableNotification == 1) ? true : false, 
                                              messageAutoDeleteTime, 
                                              adminIds, 
                                              memberCount, 
                                              memberIds, 
                                              description, 
                                              inviteLink, 
                                              botCommands, 
                                              messages));
                    //System.out.println(this.getCollection().get(this.getCollection().size() - 1).getMemberIds() + "===232332======");

            redefinedAttributes();
                    //System.out.println(this.getCollection().get(this.getCollection().size() - 1).getMemberIds() + "=========");

        }

        //System.out.println(this.getCollection().get(this.getCollection().size() - 1).getMemberIds() + "=========");
    }

    
    class UpdateBasicGroupHandler implements Client.ResultHandler
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
                
                case TdApi.BasicGroupFullInfo.CONSTRUCTOR:
                    //System.out.println("dfasfafasf");
                    TdApi.BasicGroupFullInfo basicGroupFullInfo = (TdApi.BasicGroupFullInfo) object;
                    //System.out.println(basicGroupFullInfo);
                    description = basicGroupFullInfo.description;
                    inviteLink = basicGroupFullInfo.inviteLink;
                    for (TdApi.BotCommands bc : basicGroupFullInfo.botCommands)
                    {
                        botCommands.add(bc);
                        //System.out.println(bc);
                    }
                    for (TdApi.ChatMember mem: basicGroupFullInfo.members)
                    {
                        if (mem.memberId instanceof TdApi.MessageSenderChat)
                        {
                            memberIds.add(((TdApi.MessageSenderChat) mem.memberId).chatId);
                        }
                        else
                        {
                            memberIds.add(((TdApi.MessageSenderUser) mem.memberId).userId);
                        }
                        //System.out.println(memberIds);
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
