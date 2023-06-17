package hust.soict.cybersec.tm;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.crawling.BasicGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.SuperGroupInfoCrawler;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
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

    protected static final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<Long, TdApi.User>();
    protected static final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Long, TdApi.BasicGroup>();
    protected static final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Long, TdApi.Supergroup>();
    protected static final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    protected static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    protected static final NavigableSet<OrderedChat> mainChatList = new TreeSet<OrderedChat>();
    protected static boolean haveFullMainChatList = false;

    protected static final ConcurrentMap<Long, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Long, TdApi.UserFullInfo>();
    protected static final ConcurrentMap<Long, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>();
    protected static final ConcurrentMap<Long, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>();

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

    private static long getChatId(String arg) {
        long chatId = 0;
        try {
            chatId = Long.parseLong(arg);
        } catch (NumberFormatException ignored) {
        }
        return chatId;
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

    private static void getCommand() {
        String command = promptString(commandsLine);
        // System.out.println(command+"================================");
        String[] commands = command.split(" ", 2);
        try {
            switch (commands[0]) {
                case "gu": {
                    System.out.println(users.get(getChatId(commands[1])));
                    break;
                }
                case "gbg": {
                    client.send(new TdApi.GetBasicGroup(getChatId(commands[1])), defaultHandler);
                    break;
                }
                case "gsg": {
                    client.send(new TdApi.GetSupergroup(getChatId(commands[1])), defaultHandler);
                    break;
                }
                case "gsgfi": {
                    client.send(new TdApi.GetSupergroupFullInfo(getChatId(commands[1])), defaultHandler);
                    break;
                }
                case "gh": {
                    client.send(new TdApi.GetChatHistory(getChatId(commands[1]), 1120927744, 0, 99, false), defaultHandler);
                    break;
                }
                case "gadmin":
                {
                    client.send(new TdApi.GetChatAdministrators(getChatId(commands[1])), defaultHandler);
                    break;
                }
                case "gil": {
                    String inviteLink = "adafsdfasfd";
                    client.send(new TdApi.GetChatInviteLink(getChatId(commands[1]), inviteLink), defaultHandler);
                    System.out.println(inviteLink);
                }
                case "gc":
                    client.send(new TdApi.GetChat(getChatId(commands[1])), defaultHandler);
                    break;
                case "me":
                    client.send(new TdApi.GetMe(), defaultHandler);
                    break;
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
        }
    }

    protected static void getMainChatList() {
        synchronized (mainChatList) {
            //System.out.println(mainChatList + "=====================");
            if (!haveFullMainChatList) {
                //System.out.println("ầdafsfasfaf");
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
        // test Client.execute
        //defaultHandler.onResult(Client.execute(new TdApi.GetTextEntities("@telegram /test_command https://telegram.org telegram.me @gif @test")));
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
            // System.out.println("chats: " + chats.size());
            // System.out.println("basic group: " + basicGroups.size());
            // System.out.println("super group: " + supergroups.size());
            // System.out.println("basic group full info: " + basicGroupsFullInfo.size());
            // System.out.println("super group full info: " + supergroupsFullInfo.size());
            // System.out.println("user: " + users.size());
            // for (Map.Entry<Long, TdApi.Chat> entry: chats.entrySet())
            // {
            //     if (entry.getValue().type.getConstructor() == TdApi.ChatTypeBasicGroup.CONSTRUCTOR)
            //     {
            //         System.out.println("basic group: " + entry.getValue().title + " ----- " + entry.getValue().id);
            //     }
            //     else if (entry.getValue().type.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR)
            //     {
            //         System.out.println("super group: " + entry.getValue().title + " ----- " + entry.getValue().id);
            //     }

            // }
            BasicGroupInfoCrawler bgCrawler = new BasicGroupInfoCrawler(basicGroups, chats, client);
            bgCrawler.crawlBasicGroupInfo();
            // SuperGroupInfoCrawler sgCrawler = new SuperGroupInfoCrawler(supergroups, chats, client);
            // sgCrawler.crawlSuperGroupInfo();
            while (haveAuthorization && haveFullMainChatList) {
                getCommand();
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
            //System.out.println("fasfsdfadsfdsfdffasdfdfafsfaffafdsaf");
        }
    }

}
