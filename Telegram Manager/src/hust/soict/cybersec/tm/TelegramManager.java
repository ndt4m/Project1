package hust.soict.cybersec.tm;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.crawling.BasicGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.SuperGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.UserInfoCrawler;
import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class TelegramManager {
    protected static Client client = null;

    protected static TdApi.AuthorizationState authorizationState = null;
    protected static volatile boolean haveAuthorization = false;
    protected static volatile boolean needQuit = false;
    protected static volatile boolean canQuit = false;
    
    private static final Client.ResultHandler defaultHandler = new DefaultHandler();

    protected static final Lock authorizationLock = new ReentrantLock();
    protected static final Condition gotAuthorization = authorizationLock.newCondition();

    protected static final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Long, TdApi.BasicGroup>();
    protected static final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Long, TdApi.Supergroup>();

    private static List<BasicGroup> targetBasicGroups;
    private static List<SuperGroup> targetSupergroups;
    private static List<User> targetUsers;

    protected static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    protected static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    protected static boolean haveFullMainChatList = false;

    private static final String commandsLine = "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, gu <userId> - GetUser, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ";
    private static volatile String currentPrompt = null;

    protected static void print(String str) {
        if (currentPrompt != null) {
            System.out.println("");
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }

    protected static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentPrompt = null;
        return str;
    }

    private static long mapGroupNameToId(String groupName) 
    {
        for (SuperGroup superGroup: targetSupergroups)
        {
            if (superGroup.getGroupName().equals(groupName))
            {
                return superGroup.getChatId();
            }
        }

        for (BasicGroup basicGroup : targetBasicGroups)
        {
            if (basicGroup.getGroupName().equals(groupName))
            {
                return basicGroup.getChatId();
            }
        }
        return -1l;
    }

    private static long mapUserNameToId(String userName)
    {
        for (User user: targetUsers)
        {
            if (user.getDisplayName().equals(userName))
            {
                return user.getId();
            }
        }
        return -1l;
    }

    private static boolean isBasicGroup(String groupName)
    {
        for (BasicGroup basicGroup: targetBasicGroups)
        {
            if (basicGroup.getGroupName().equals(groupName))
            {
                return true;
            }
        }
        return false;
    }

    public static void updateData() throws InterruptedException
    {
        System.out.println("Start Crawling basicgroup");
        BasicGroupInfoCrawler bgCrawler = new BasicGroupInfoCrawler(basicGroups, chats, client);
        bgCrawler.crawlBasicGroupInfo();
        targetBasicGroups = bgCrawler.getCollection();
        
        //long[] userIds = {6173576926l, 6024238663l, 806954250l, 373610989l, 84210004l, 2134816269l};
        //client.send(new TdApi.CreateNewBasicGroupChat(null, "aa", 0), new UpdateHandler());
        //client.send(new TdApi.BanChatMember(-981850633l, new TdApi.MessageSenderChat(6173576926l), 0, true), new UpdateHandler());
        System.out.println("Start Crawling supergroup");
        SuperGroupInfoCrawler sgCrawler = new SuperGroupInfoCrawler(supergroups, chats, client);
        sgCrawler.crawlSuperGroupInfo(); 
        targetSupergroups = sgCrawler.getCollection();
        System.out.println("Start Crawling user");   
        UserInfoCrawler uCrawler = new UserInfoCrawler(client, bgCrawler.getCollection(), sgCrawler.getCollection());
        uCrawler.crawlUserInfo();
        targetUsers = uCrawler.getCollection();
        System.out.println("finish Crawling user");
    }

    private static void getCommand() {
        String command = promptString(commandsLine);
        // System.out.println(command+"================================");
        String[] commands = command.split(" ");
        try {
            switch (commands[0]) {
                case "update": {
                    updateData();
                    break;
                }
                case "createBasicGroup": {
                        if (commands.length == 3)
                        {
                            client.send(new TdApi.CreateNewBasicGroupChat(null, commands[1], Integer.parseInt(commands[2])), new UpdateHandler());
                        }
                        else
                        {
                            client.send(new TdApi.CreateNewBasicGroupChat(null, commands[1], 0), new UpdateHandler());

                        }
                        break;
                }
                case "createSuperGroup": {
                        if (commands.length == 2)
                        {
                            client.send(new TdApi.CreateNewSupergroupChat(commands[1], false, false, null, null, 0, false), new UpdateHandler());
                        }
                        else if (commands.length == 3)
                        {
                            client.send(new TdApi.CreateNewSupergroupChat(commands[1], false, false, null, null, Integer.parseInt(commands[2]), false), new UpdateHandler());
                        }
                        break;
                }
                case "addMembers": {
                    long chatId = mapGroupNameToId(commands[1]);
                    System.out.println(chatId);
                    String[] userNames = commands[2].split("-");
                    long[] userIds = new long[userNames.length];
                    for (int i = 0; i < userNames.length; i++) 
                    {
                        userIds[i] = mapUserNameToId(userNames[i]);
                        System.out.println(userIds[i]);
                    }

                    if (isBasicGroup(commands[1]))
                    {
                        for (int i = 0; i < userIds.length; i++)
                        {
                            client.send(new TdApi.AddChatMember(chatId, userIds[i], 0), new UpdateHandler());
                        }
                    }
                    else
                    {
                        client.send(new TdApi.AddChatMembers(chatId, userIds), new UpdateHandler());
                    }
                    break;
                }
                case "kickUser": {
                    long chatId = mapGroupNameToId(commands[1]);
                    long userId = mapUserNameToId(commands[2]);
                    client.send(new TdApi.BanChatMember(chatId, new TdApi.MessageSenderChat(userId), 0, true), new UpdateHandler());
                    break;
                }
                case "lo":
                    haveAuthorization = false;
                    client.send(new TdApi.LogOut(), defaultHandler);
                    break;
                case "q":
                    needQuit = true;
                    haveAuthorization = false;
                    client.send(new TdApi.Close(), defaultHandler);
                    break;
                default:
                    System.err.println("Unsupported command: " + command);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            print("Not enough arguments");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected static void getMainChatList() {
        synchronized (mainChatList) {
            if (!haveFullMainChatList) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), 50), new MainChatListHandler());
                return;
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        // set log message handler to handle only fatal errors (0) and plain log messages (-1)
        Client.setLogMessageHandler(0, new LogMessageHandler());

        // disable TDLib log and redirect fatal errors and plain log messages to a file
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        // create client
        client = Client.create(new UpdateHandler(), null, null);
        // main loop
        while (!needQuit) {
            // await authorization
            authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    gotAuthorization.await();
                }
            } finally {
                authorizationLock.unlock();
            }
            getMainChatList();
            authorizationLock.lock();
            try {
                while (!haveFullMainChatList) {
                    gotAuthorization.await();
                }
            } finally {
                authorizationLock.unlock();
            }

            updateData();
            while (haveAuthorization && haveFullMainChatList) {
                getCommand();
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
        }
    }

}
