package hust.soict.cybersec.tm.utils;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.TelegramManager;

public class Base 
{
    public static Client client = null;
    public static TdApi.AuthorizationState authorizationState = null;
    public static volatile boolean haveAuthorization = false;
    public static volatile boolean needQuit = false;
    public static volatile boolean canQuit = false;

    public static final Lock authorizationLock = new ReentrantLock();
    public static final Condition gotAuthorization = authorizationLock.newCondition();

    public static final ConcurrentMap<Long, TdApi.User> users = new ConcurrentHashMap<Long, TdApi.User>();
    public static final ConcurrentMap<Long, TdApi.BasicGroup> basicGroups = new ConcurrentHashMap<Long, TdApi.BasicGroup>();
    public static final ConcurrentMap<Long, TdApi.Supergroup> supergroups = new ConcurrentHashMap<Long, TdApi.Supergroup>();
    public static final ConcurrentMap<Integer, TdApi.SecretChat> secretChats = new ConcurrentHashMap<Integer, TdApi.SecretChat>();

    public static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    public static final NavigableSet<TdApi.Chat> mainChatList = new TreeSet<TdApi.Chat>();
    public static boolean haveFullMainChatList = false;

    public static final ConcurrentMap<Long, TdApi.UserFullInfo> usersFullInfo = new ConcurrentHashMap<Long, TdApi.UserFullInfo>();
    public static final ConcurrentMap<Long, TdApi.BasicGroupFullInfo> basicGroupsFullInfo = new ConcurrentHashMap<Long, TdApi.BasicGroupFullInfo>();
    public static final ConcurrentMap<Long, TdApi.SupergroupFullInfo> supergroupsFullInfo = new ConcurrentHashMap<Long, TdApi.SupergroupFullInfo>();

    public static final String newLine = System.getProperty("line.separator");
    public static final String commandsLine = "Enter command (gcs - GetChats, gc <chatId> - GetChat, me - GetMe, gu <userId> - GetUser, sm <chatId> <message> - SendMessage, lo - LogOut, q - Quit): ";
    public static volatile String currentPrompt = null;

    public static void print(String str) {
        if (currentPrompt != null) {
            System.out.println("");
        }
        System.out.println(str);
        if (currentPrompt != null) {
            System.out.print(currentPrompt);
        }
    }
    
    public static long toLong(String arg) {
        long result = 0;
        try {
            result = Long.parseLong(arg);
        } catch (NumberFormatException ignored) {
        }
        return result;
    }

    public static String promptString(String prompt) {
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
    public static long toLong(String arg) {
        long result = 0;
        try {
            result = Long.parseLong(arg);
        } catch (NumberFormatException ignored) {
        }
        return result;
    }
    
    public static void getMainChatList() {
        synchronized (mainChatList) {
            if (!haveFullMainChatList) {
                // send LoadChats request if there are some unknown chats and have not enough known chats
                client.send(new TdApi.LoadChats(new TdApi.ChatListMain(), 50), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        switch (object.getConstructor()) {
                            case TdApi.Error.CONSTRUCTOR:
                                if (((TdApi.Error) object).code == 404) {
                                    synchronized (mainChatList) {
                                        haveFullMainChatList = true;
                                        authorizationLock.lock();
                                        try {
                                            gotAuthorization.signal();
                                        } finally {
                                            authorizationLock.unlock();
                                        }
                                    }
                                } else {
                                    System.err.println("Receive an error for LoadChats:\n" + object);
                                }
                                break;
                            case TdApi.Ok.CONSTRUCTOR:
                                // chats had already been received through updates, let's retry request
                                getMainChatList();
                                break;
                            default:
                                //System.err.println("Receive wrong response from TDLib:\n" + object);
                        }
                    }
                });
                return;
            }
        }
    }

}
