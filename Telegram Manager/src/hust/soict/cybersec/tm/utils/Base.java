package hust.soict.cybersec.tm.utils;

import java.io.BufferedReader;
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

public class Base 
{
    public final static String RED = "\u001B[31m";
    public final static String GREEN = "\u001B[92m";
    public final static String BLUE = "\u001B[34m";
    public final static String MAGENTA = "\u001B[35m";
    public final static String YELLOW = "\u001B[33m";
    
    public static Client client = null;
    public static TdApi.AuthorizationState authorizationState = null;
    public static volatile boolean haveAuthorization = false;
    public static volatile boolean needQuit = false;
    public static volatile boolean canQuit = false;
    public static boolean haveReceivedRespond = false;
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

    
    public static volatile String currentPrompt = null;

    

    public static String promptString(String prompt) {
        System.out.print(prompt);
        currentPrompt = prompt;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        try {
            str = reader.readLine();
        } catch (IOException e) {
            
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
                                
                                getMainChatList();
                                break;
                            default:
                                
                        }
                    }
                });
                return;
            }
        }
    }

    public static void printColor(String color, String content)
    {
        System.out.println(color + content + "\u001B[0m");
    }

    public static String centerString(String text, int width, String delimiter) {
        StringBuilder sb = new StringBuilder();
        sb.append(delimiter.repeat(width));
        sb.append("".repeat(width) + "\n");
		if (text.length() > width) {
			return text.substring(0, width);
		} else {
			int padding = width - text.length();
			int leftPadding = padding / 2;
			int rightPadding = padding - leftPadding;
			sb.append(" ".repeat(leftPadding) + text + " ".repeat(rightPadding) + "\n");
		}
        sb.append(delimiter.repeat(width));
        sb.append("".repeat(width));
        return sb.toString();
	}
}
