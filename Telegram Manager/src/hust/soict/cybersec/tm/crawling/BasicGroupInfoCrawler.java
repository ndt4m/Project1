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
    

    private long id = 0l;
    private long chatId = 0l;
    private String groupName = "";
    private TdApi.ChatPermissions permissions = null;
    
    private int messageAutoDeleteTime = -1;
    private List<Long> adminIds = new ArrayList<>();
    private int memberCount = -1;
    private List<Long> memberIds = new ArrayList<>();
    private String description = "";
    private String inviteLink = "";
    
    private List<TdApi.Message> messages = new ArrayList<>();

    public BasicGroupInfoCrawler(Map<Long, TdApi.Chat> chats, Client client)
    {
        super(client, chats);
        
    }

    public void redefinedAttributes()
    {
        id = 0l;
        chatId = 0l;
        groupName = "";
        permissions = null;
        
        messageAutoDeleteTime = -1;
        adminIds = new ArrayList<>();
        memberCount = -1;
        memberIds = new ArrayList<>();
        description = "";
        inviteLink = "";
        
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
            chatId = chat.getKey();
            groupName = chat.getValue().title;
            permissions = chat.getValue().permissions;
            
            messageAutoDeleteTime = chat.getValue().messageAutoDeleteTime;
            

            blockingSend(new TdApi.GetChatAdministrators(chat.getKey()), updateBasicGroupHandler);
            if (adminIds.contains(currentUserId))
            {
                blockingSend(new TdApi.GetBasicGroupFullInfo(id), updateBasicGroupHandler);
            
                blockingSend(new TdApi.GetChatHistory(chat.getKey(), 0, 0, 50, false), updateBasicGroupHandler);
                int oldSize = messages.size();
                while (messages.size() <= 50)
                {
                    if (messages.size() == 0)
                    {
                        break;
                    }
                    blockingSend(new TdApi.GetChatHistory(chat.getKey(), messages.get(messages.size() - 1).id, 0, 50, false), updateBasicGroupHandler);
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
                
                this.addCollection(new BasicGroup(id,
                                                  chatId,
                                                  groupName, 
                                                  permissions, 
                                                
                                                  messageAutoDeleteTime, 
                                                  adminIds, 
                                                  memberCount, 
                                                  memberIds, 
                                                  description, 
                                                  inviteLink, 
                                                
                                                  msContent));
                                   
            }
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
                    
                    TdApi.ChatAdministrators chatAdministrators = (TdApi.ChatAdministrators) object;
                    for (TdApi.ChatAdministrator chatAdmin: chatAdministrators.administrators)
                    {
                        adminIds.add(chatAdmin.userId);
                    }
                    break;
                
                case TdApi.BasicGroupFullInfo.CONSTRUCTOR:
                    
                    TdApi.BasicGroupFullInfo basicGroupFullInfo = (TdApi.BasicGroupFullInfo) object;
                    
                    description = basicGroupFullInfo.description;
                    if (basicGroupFullInfo.inviteLink != null)
                    {  
                        inviteLink = basicGroupFullInfo.inviteLink.inviteLink;
                    }
                    else
                    {
                        inviteLink = "";
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
                    memberCount = basicGroupFullInfo.members.length;
                    break;
                case TdApi.Messages.CONSTRUCTOR:
                    for (TdApi.Message m: ((TdApi.Messages) object).messages)
                    {
                        messages.add(m);
                    }
                    break;
                case TdApi.Error.CONSTRUCTOR:
                    System.err.println("[-] Received an error when crawling super group: " + ((TdApi.Error) object).message);
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
