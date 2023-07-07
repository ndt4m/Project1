package hust.soict.cybersec.tm;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.crawling.BasicGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.SuperGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.UserInfoCrawler;
import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;
import hust.soict.cybersec.tm.utils.Base;
import hust.soict.cybersec.tm.utils.LogMessageHandler;
import hust.soict.cybersec.tm.utils.UpdateHandler;

import java.io.IOError;
import java.io.IOException;

import java.util.List;
import java.util.Map;


public final class TelegramManager extends Base{
    

    private static List<BasicGroup> targetBasicGroups;
    private static List<SuperGroup> targetSupergroups;
    private static List<User> targetUsers;

    public static List<BasicGroup> getTargetBasicGroups() {
        return targetBasicGroups;
    }

    public static List<SuperGroup> getTargetSupergroups() {
        return targetSupergroups;
    }

    public static List<User> getTargetUsers() {
        return targetUsers;
    }

    public static void updateData() throws InterruptedException
    {
        System.out.println("Start Crawling supergroup");
        SuperGroupInfoCrawler sgCrawler = new SuperGroupInfoCrawler(chats, client);
        sgCrawler.crawlSuperGroupInfo(); 
        targetSupergroups = sgCrawler.getCollection();
        System.out.println("Start Crawling basicgroup");
        BasicGroupInfoCrawler bgCrawler = new BasicGroupInfoCrawler(chats, client);
        bgCrawler.crawlBasicGroupInfo();
        targetBasicGroups = bgCrawler.getCollection();
        
        //long[] userIds = {6173576926l, 6024238663l, 806954250l, 373610989l, 84210004l, 2134816269l};
        //client.send(new TdApi.CreateNewBasicGroupChat(null, "aa", 0), new UpdateHandler());
        //client.send(new TdApi.BanChatMember(-981850633l, new TdApi.MessageSenderChat(6173576926l), 0, true), new UpdateHandler());
        System.out.println("Start Crawling user");   
        UserInfoCrawler uCrawler = new UserInfoCrawler(client, bgCrawler.getCollection(), sgCrawler.getCollection());
        uCrawler.crawlUserInfo();
        targetUsers = uCrawler.getCollection();
        System.out.println("finish Crawling user");
    }

    public static void synchronize()
    {
        Client.setLogMessageHandler(0, new LogMessageHandler());

        // disable TDLib log and redirect fatal errors and plain log messages to a file
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        client = Client.create(new UpdateHandler(), null, null);
        authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    try {
                        gotAuthorization.await();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } finally {
                authorizationLock.unlock();
            }
            getMainChatList();
            authorizationLock.lock();
            try {
                while (!haveFullMainChatList) {
                    try {
                        gotAuthorization.await();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } finally {
                authorizationLock.unlock();
            }

        try {
            updateData();
            PushUserMethod pushUser = new PushUserMethod();
            pushUser.pushMethod(targetUsers);

        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static void getCommand() {
        String command = promptString(commandsLine);
        // System.out.println(command+"================================");
        String[] commands = command.split(" ");
        try {
            switch (commands[0]) {
                // case "gu": {
                //     client.send(new TdApi.GetUser(5846793443l), defaultHandler);
                //     break;
                // }\
                case "sync":{
                    synchronize();
                    break;
                }
                case "update": {
                    updateData();
                    PushUserMethod pushUser = new PushUserMethod();
                    pushUser.pushMethod(targetUsers);
                    PushBasicGroupMethod pushBasicGroup = new PushBasicGroupMethod();
                    pushBasicGroup.pushMethod(targetBasicGroups);
                    PushSuperGroupMethod pushSuperGroup = new PushSuperGroupMethod();
                    pushSuperGroup.pushMethod(targetSupergroups);
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
                case "am": {
                    System.out.println("###################################################################");
                    System.out.println("Basic Group List:");
                    for (BasicGroup bsg: targetBasicGroups)
                    {
                        System.out.println(bsg.getGroupName() + ": " + bsg.getChatId());
                    }
                    System.out.println("Super Groups List: ");
                    for (SuperGroup sg: targetSupergroups)
                    {   
                        System.out.println(sg.getGroupName() + ": " + sg.getChatId());
                    }
                    System.out.println("User List: ");
                    for (User u: targetUsers)
                    {
                        System.out.println(u.getDisplayName() + ": " + u.getId());
                    }
                    
                    for (Map.Entry<Long, TdApi.Chat> chat : chats.entrySet())
                    {
                        if (chat.getValue().type.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR)
                        {
                            System.out.println(chat.getValue().title + ": " + ((TdApi.ChatTypePrivate) chat.getValue().type).userId);
                        }
                        else if (chat.getValue().type.getConstructor() == TdApi.ChatTypeSecret.CONSTRUCTOR)
                        {
                            System.out.println(chat.getValue().title + ": " + ((TdApi.ChatTypeSecret) chat.getValue().type).userId);
                        }
                    }
                    String chatId = promptString("Enter group Id: ");
                    String UserId = promptString("Enter user Id: ");
                    client.send(new TdApi.AddChatMember(Long.parseLong(chatId), Long.parseLong(UserId), 0), new UpdateHandler());
                    break;
                    
                }
                case "ku": {
                    client.send(new TdApi.BanChatMember(Long.parseLong(commands[1]), new TdApi.MessageSenderChat(Long.parseLong(commands[2])), 0, true), new UpdateHandler());
                    break;
                }
                case "lo":
                    needQuit = true;
                    haveAuthorization = false;
                    client.send(new TdApi.LogOut(), new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) 
                        {
                            switch (object.getConstructor()) 
                            {
                                case TdApi.Ok.CONSTRUCTOR:
                                    System.out.println("All local data will be destroyed");
                                    break;
                                default:
                                    System.err.println("[-] Receive an error: " + newLine + object);
                            }
                        }
                    });
                    break;
                case "q":
                    needQuit = true;
                    haveAuthorization = false;
                    client.send(new TdApi.Close(), new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) 
                        {
                            switch (object.getConstructor()) 
                            {
                                case TdApi.Ok.CONSTRUCTOR:
                                    System.out.println("All databases will be flushed to disk and properly closed.");
                                    break;
                                default:
                                    System.err.println("[-] Receive an error: " + newLine + object);
                            }
                        }
                    });
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
            //updateData();
            while (haveAuthorization && haveFullMainChatList) {
                getCommand();
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
        }
    }

}
// ‘’’package com.project.main;

// import com.project.createchannels.CreateChannels;
// import com.project.slackdatafetching.*;
// import com.project.inviteusers.*;

// import java.io.IOException;
// import java.io.BufferedReader;
// import java.io.InputStreamReader;
// import java.util.*;

// public class Main {

// 	private static Scanner sc = new Scanner(System.in);

// 	public static void main(String[] args) throws Exception {
// 		//autoFetching();
// 		showlogo();
// 		showMenu();
// 		sc.close();
// 	}

// 	public static void showlogo() {
// 		String logo = "\n  _  _  _  _  _.---------------.\n"
// 				+ ".'\\__\\'\\__\\'\\__\\'\\__\\'\\__,`   .  __
// _ \\\n"
// 				+ "|\\/ _\\/ __\\/ __\\/ __\\/ _:\\   |`.  \\  \\__ \\\n"
// 				+ " \\\\'\\__\\'\\__\\'\\__\\'\\__\\'\\_.__|\"\". \\  \\___ \\\n"
// 				+ "  \\\\/ _\\/ __\\/ __\\/ __\\/ _:                \\\n"
// 				+ "   \\\\'\\__\\'\\__\\'\\__\\ \\__\\'\\_;-----------------`\n"
// 				+ "    \\\\/   \\/   \\/   \\/   \\/ :               tk|\n"
// 				+ "     \\|______________________;________________|\n";

// 		String title = "WELCOME TO SLACK MANAGEMENT PROGRAM!";
// 		int width = 60;
// 		String line = "-".repeat(width);
		
// 		System.out.println(logo);
// 		System.out.println(line);
// 		System.out.println(centerString(title, width));
// 		System.out.println(line);
// 	}

// 	public static void showMenu() throws Exception {
// 		String menu = "\nPlease select an option:\n\n" + "1. Show Slack's channels\n"
// 				+ "2. Show Slack user's information\n" + "3. Create a channels\n" + "4. Invite user to channel\n" + "5. User management\n"
// 				+ "0. Exit\n\n" + "Enter your choice (1-5): ";
// 		System.out.print(menu);
// 		int option = sc.nextInt();
		
// 		switch (option) {
// 		case 0:
// 			System.out.println("Program ended");
// 			break;
// 		case 1:
// 			showChannels();
// 			break;
// 		case 2:
// 			showUsers();
// 			break;
// 		case 3:
// 			createChannel();
// 			break;
//     		case 4:
// 			inviteUser();
// 			break;
// 		case 5:
// 			manageUsers();
// 			break;
// 		default:
// 			System.out.println("Invalid input!");
// 			showMenu();
// 			break;
// 		}
// 	}

// 	public static void autoFetching() {
// 		try {
// 			SlackDataFetching.airtableFetching();
// 		} catch (IOException e) {
// 			// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
// 	}

// 	public static void showUsers() throws Exception {
// 		SlackDataFetching.printUsers();
// 		System.out.println("Press Enter key to get back...");
// 		System.in.read();
// 		showMenu();
// 	}

// 	public static void showChannels() throws Exception {
// 		SlackDataFetching.printChannels();
// 		System.out.println("Press Enter key to get back...");
// 		System.in.read();
// 		showMenu();
// 	}

// 	public static void createChannel() throws Exception {
// 		// create channel bang slack API
// 		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
// 		System.out.print("Note: Channel names have a 21-character limit and can include lowercase letters, non-Latin characters, numbers, and hyphens.\nEnter channel's name:\n");
// 		String channelName = reader.readLine();
// 		if (channelName.trim().isEmpty()) {
// 			System.out.println("Channel name cannot be empty.");
// 			System.out.println("Press Enter to get back...");
// 			System.in.read();
// 			showMenu();
// 		}		
    
// 		System.out.print("Enter '0' for private channel or '1' for public channel:\n");
// 		String channelType = reader.readLine();
// 		boolean isPrivate = false;

// 		while (!channelType.equals("0") && !channelType.equals("1")) {
// 		    System.out.println("Invalid input! Please enter '0' for private channel or '1' for public channel:");
// 		    channelType = reader.readLine();
// 		}

// 		isPrivate = channelType.equals("0");
		
// 		System.out.print("Enter channel' description (optional):\n");
// 		String description = reader.readLine();
// 		CreateChannels.createChannel(channelName, description, isPrivate);
//     System.out.println("Press Enter key to get back...");
// 		System.in.read();
// 		showMenu();
// 	}

// 	public static void inviteUser() throws Exception {
// 		//for debug
// 		System.out.println("inviteUser");
// 		InviteUsers.inviteUser();
//     System.out.println("Press Enter key to get back...");
// 		System.in.read();
// 		showMenu();
// 	}

// 	public static void manageUsers() {
// 		//for debug
// 		System.out.println("manageUsers");
// 		System.out.println("Press Enter key to get back...");
// 		System.in.read();
// 		showMenu();
// 	}

// 	public static String centerString(String text, int width) {
// 		if (text.length() > width) {
// 			return text.substring(0, width);
// 		} else {
// 			int padding = width - text.length();
// 			int leftPadding = padding / 2;
// 			int rightPadding = padding - leftPadding;
// 			return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
// 		}
// 	}

// }
// ‘’’