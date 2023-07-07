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
    
    
    private long id = 0l;
    private long chatId = 0l;
    private String groupName = "";
    private TdApi.ChatPermissions permissions = null;
    
    private int messageAutoDeleteTime = -1;
    
    private int memberCount = -1;
    
    private int isAllHistoryAvailable = -1;
    private Set<Long> adminIds = new HashSet<>();
    private Set<Long> memberIds = new HashSet<>();
    private String description = "";
    private String inviteLink = "";
    
    private List<TdApi.Message> messages = new ArrayList<>();

    public SuperGroupInfoCrawler()
    {

    }

    public SuperGroupInfoCrawler(Map<Long, TdApi.Chat> chats, Client client)
    {
        super(client, chats);
        
    }

    public void redefinedAttributes()
    {
        id = 0l;
        chatId = 0l;
        groupName = "";
        permissions = null;
        
        isAllHistoryAvailable = -1;
        messageAutoDeleteTime = -1;
        adminIds = new HashSet<>();
        memberCount = -1;
        memberIds = new HashSet<>();
        description = "";
        inviteLink = "";
        
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
            chatId = chat.getKey();
            groupName = chat.getValue().title;
            permissions = chat.getValue().permissions;
            
            messageAutoDeleteTime = chat.getValue().messageAutoDeleteTime;
            
            blockingSend(new TdApi.GetChatAdministrators(chat.getKey()), updateSuperGroupHandler);
            if (adminIds.contains(currentUserId))
            {   
                blockingSend(new TdApi.GetSupergroupFullInfo(id), updateSuperGroupHandler);
                blockingSend(new TdApi.GetSupergroupMembers(id, null, 0, 200), updateSuperGroupHandler);
                
                for (int i = 0; i < Math.min((int) Math.ceil(memberCount / ((double) 200)) + 1, (int) 10000/200); i++)
                {   
                    
                    blockingSend(new TdApi.GetSupergroupMembers(id, null, 200 * i, 200), updateSuperGroupHandler);
                    
                }
                
                blockingSend(new TdApi.GetChatHistory(chat.getKey(), 0, 0, 50, false), updateSuperGroupHandler);
                int oldSize = messages.size();
                while (messages.size() <= 50)
                {
                    
                    blockingSend(new TdApi.GetChatHistory(chat.getKey(), messages.get(messages.size() - 1).id, 0, 50, false), updateSuperGroupHandler);
                    if (oldSize != messages.size())
                    {
                        oldSize = messages.size();
                        continue;
                    }
                    break;
                }
                List<TdApi.MessageContent> msContent = new ArrayList<TdApi.MessageContent>();
                for (TdApi.Message ms: messages)
                {
                    msContent.add(ms.content);
                }
                this.addCollection(new SuperGroup(id, 
                                              chatId,
                                              groupName, 
                                              permissions, 
                                            
                                              messageAutoDeleteTime, 
                                            
                                              memberCount, 
                                            
                                              (isAllHistoryAvailable == 1) ? true : false, 
                                              adminIds, 
                                              memberIds, 
                                              description, 
                                              inviteLink, 
                                            
                                              msContent));
                
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
                    
                    TdApi.ChatAdministrators chatAdministrators = (TdApi.ChatAdministrators) object;
                    for (TdApi.ChatAdministrator chatAdmin: chatAdministrators.administrators)
                    {
                        adminIds.add(chatAdmin.userId);
                    }
                    break;
                case TdApi.SupergroupFullInfo.CONSTRUCTOR:
                    TdApi.SupergroupFullInfo supergroupFullInfo = (TdApi.SupergroupFullInfo) object;
                    
                    description = supergroupFullInfo.description;
                    
                    isAllHistoryAvailable = supergroupFullInfo.isAllHistoryAvailable ? 1 : 0;
                    if (supergroupFullInfo.inviteLink != null) 
                    {
                        inviteLink = supergroupFullInfo.inviteLink.inviteLink;
                    }
                    else
                    {
                        inviteLink = "";
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
                    memberCount = memberIds.size();
                    break;
                case TdApi.Messages.CONSTRUCTOR:
                    for (TdApi.Message m: ((TdApi.Messages) object).messages)
                    {
                        messages.add(m);
                    }
                    break;
                case TdApi.Error.CONSTRUCTOR:
                    TdApi.Error error= (TdApi.Error) object;
                    if (!error.message.equals("Administrator list is inaccessible"))
                    {
                        System.err.println("[-] Received an error when crawling super group: " + ((TdApi.Error) object).message);

                    }
                    break;
                default:
                    
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
