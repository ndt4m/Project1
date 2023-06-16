package hust.soict.cybersec.tm.crawling;

import org.drinkless.tdlib.TdApi;
import org.drinkless.tdlib.Client;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;



public class BasicGroupInfoCrawler extends Crawler
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
    private List<Long> adminId = new ArrayList<>();
    private int memberCount = -1;
    private List<Long> memberIds = new ArrayList<>();
    private String description = "Không rõ";
    private TdApi.ChatInviteLink inviteLink = null;
    private List<TdApi.BotCommands> botCommands = new ArrayList<>();

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
        adminId.clear();
        memberCount = -1;
        memberIds.clear();
        description = "Không rõ";
        inviteLink = null;
        botCommands.clear();
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
            //client.send(new TdApi.GetChatAdministrators(chat.getKey()), new UpdateBasicGroupHandler());
            //client.send(new TdApi.GetBasicGroupFullInfo(basicGroup.getId()), new DefaultHandler());
    
            //Thread.sleep(3000);
            System.out.println("id: " + id);
            System.out.println("groupName: " + groupName);
            System.out.println("permissions: " + permissions);
            System.out.println("canBeDeletedOnlyForSelf: " + canBeDeletedOnlyForSelf);
            System.out.println("canBeDeletedOnlyForAllUsers: " + canBeDeletedForAllUsers);
            System.out.println("defaultDisableNotification: " + defaultDisableNotification);
            System.out.println("messageAutoDeleteTime: " + messageAutoDeleteTime);
            System.out.println("memberCount: " + memberCount);
            System.out.println("adminId: " + adminId);
            System.out.println("memberIds: " + memberIds);
            System.out.println("description: " + description);
            System.out.println("InviteLink: " + inviteLink);
            System.out.println("botCommands: " + botCommands);
            redefinedAttributes();
        }
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
                        adminId.add(chatAdmin.userId);
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
                    }
                    break;
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
