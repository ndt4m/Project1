package hust.soict.cybersec.tm;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.Client.ResultHandler;
import org.drinkless.tdlib.TdApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hust.soict.cybersec.tm.crawling.BasicGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.SuperGroupInfoCrawler;
import hust.soict.cybersec.tm.crawling.UserInfoCrawler;
import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;
import hust.soict.cybersec.tm.utils.Base;
import hust.soict.cybersec.tm.utils.LogMessageHandler;
import hust.soict.cybersec.tm.utils.UpdateHandler;
import wagu.Block;
import wagu.Board;
import wagu.Table;

import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public final class TelegramManager extends Base{
    private static Scanner sc = new Scanner(System.in);

    static {
        // set log message handler to handle only fatal errors (0) and plain log messages (-1)
        Client.setLogMessageHandler(0, new LogMessageHandler());

        // disable TDLib log and redirect fatal errors and plain log messages to a file
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        // create client
        client = Client.create(new UpdateHandler(), null, null);
    }

    private static List<BasicGroup> targetBasicGroups = new ArrayList<BasicGroup>();
    private static List<SuperGroup> targetSupergroups = new ArrayList<SuperGroup>();
    private static List<User> targetUsers = new ArrayList<User>();

    public static void openchat()
    {

        for (Map.Entry<Long, TdApi.Chat> chat : chats.entrySet())
        {
            client.send(new TdApi.OpenChat(chat.getKey()), new ResultHandler() {
                @Override
                public void onResult(TdApi.Object object)
                {
                    switch (object.getConstructor()) 
                    {
                        case TdApi.Ok.CONSTRUCTOR: 
                            //System.out.println("ok");
                            break;
                        
                        default: 
                            System.out.println("Unhandle response: " + object.toString());
                        
                    }
                }
            });
            
            if (chat.getValue().type.getConstructor() == TdApi.ChatTypeBasicGroup.CONSTRUCTOR ||
                chat.getValue().type.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR)
            {
                TdApi.ChatPermissions permissions = chat.getValue().permissions;
                permissions.canInviteUsers = !(chat.getValue().permissions.canInviteUsers);
                client.send(new TdApi.SetChatPermissions(chat.getKey(), permissions), new ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object)
                    {
                        switch (object.getConstructor()) 
                        {
                            case TdApi.Ok.CONSTRUCTOR: 
                                //System.out.println("ok");
                                break;
                        }
                    }
                });
                permissions.canInviteUsers = chat.getValue().permissions.canInviteUsers;
                client.send(new TdApi.SetChatPermissions(chat.getKey(), permissions), new ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object)
                    {
                        switch (object.getConstructor()) 
                        {
                            case TdApi.Ok.CONSTRUCTOR: 
                                //System.out.println("ok");
                                break;
                        }
                    }
                });

                // for (int i = 0; i < 2; i++)
                // {
                //     client.send(new TdApi.GetBasicGroupFullInfo(((TdApi.ChatTypeBasicGroup) chat.getValue().type).basicGroupId), new UpdateHandler());
                // }
            }
        }
    }

    public static void updateData() 
    {   
        openchat();
        try {
            //System.out.println("Start Crawling");
            SuperGroupInfoCrawler sgCrawler = new SuperGroupInfoCrawler(chats, client);
            sgCrawler.crawlSuperGroupInfo();
            targetSupergroups = sgCrawler.getCollection();
            BasicGroupInfoCrawler bgCrawler = new BasicGroupInfoCrawler(chats, client);
            bgCrawler.crawlBasicGroupInfo();
            targetBasicGroups = bgCrawler.getCollection();
            UserInfoCrawler uCrawler = new UserInfoCrawler(client, bgCrawler.getCollection(), sgCrawler.getCollection());
            uCrawler.crawlUserInfo();
            targetUsers = uCrawler.getCollection();
            //System.out.println("finish Crawling");
            FileWriter fwb = new FileWriter("basicGroups.json");
            FileWriter fws = new FileWriter("superGroups.json");  
            FileWriter fwu = new FileWriter("users.json");

            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            gson.toJson(targetBasicGroups, fwb);
            gson.toJson(targetSupergroups, fws);
            gson.toJson(targetUsers, fwu);
            fwb.close();
            fws.close();
            fwu.close();
        } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    public static void showMenu()
    {
        String logo = "o\r\n" + //
                " \\_/\\o\r\n" + //
                "( Oo)                    \\|/\r\n" + //
                "(_=-)  .===O-  ~~Z~A~P~~ -O-\r\n" + //
                "/   \\_/U'                /|\\\r\n" + //
                "||  |_/\r\n" + //
                "\\\\  |\r\n" + //
                "{K ||\r\n" + //
                " | PP\r\n" + //
                " | ||\r\n" + //
                " (__\\\\";
        String title = "WELCOME TO TELEGRAM MANAGEMENT PROGRAM!";
		int width = 60;
		String line = "-".repeat(width);
		
		System.out.println(logo);
		System.out.println(line);
		//System.out.println(centerString(title, width));
		System.out.println(line);
        
        System.out.println("TELEGRAM MANAGER: ");
        System.out.println("--------------------------------");
        System.out.println("1. update");
        System.out.println("2. Update store");
        System.out.println("3. See current cart");
        System.out.println("0. Exit");
        System.out.println("--------------------------------");
        System.out.println("Please choose a number: 0-1-2-3");
        System.out.print("Your choise is: ");
    }

    public static void synchronize()
    {
        
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

            updateData();
            

    }
    
    public static void showUserGroups(String title, String type)
    {
        List<String> headersList = new ArrayList<String>();
        List<Integer> colAlignList = new ArrayList<>();
        List<Integer> colWidthsListEdited = new ArrayList<>();
        List<List<String>> rowsList = new ArrayList<List<String>>();
        List<List<Long>> user_group_ids_list = new ArrayList<>();
        int maxGroupSize = 0;
        for (User user : targetUsers)
        {
            headersList.add(user.getDisplayName());
            colAlignList.add(Block.DATA_CENTER);
            colWidthsListEdited.add(18);
            if (type.equals("B"))
            {
                user_group_ids_list.add(new ArrayList<>(user.getUser_basic_group_ids()));
                if (maxGroupSize < user.getUser_basic_group_ids().size())
                {
                    maxGroupSize = user.getUser_basic_group_ids().size();
                }
            }
            else if (type.equals("S"))
            {
                user_group_ids_list.add(new ArrayList<>(user.getUser_super_group_ids()));
                if (maxGroupSize < user.getUser_super_group_ids().size())
                {
                    maxGroupSize = user.getUser_super_group_ids().size();
                }
            }
        }

    
        for (int i = 0; i < maxGroupSize; i++)
        {   
            List<String> row = new ArrayList<String>();
            for (int j = 0; j < headersList.size(); j++)
            {   
                if (type.equals("B"))
                {
                    List<Long> user_basic_group_ids = user_group_ids_list.get(j);
                    if (user_basic_group_ids.size() > i)
                    {
                        for (BasicGroup bs: targetBasicGroups)
                        {
                            if (bs.getId() == user_basic_group_ids.get(i))
                            {
                                row.add(bs.getGroupName());
                            }
                        }
                        //row.add(user_basic_group_ids.get(i)+"");
                    }
                    else
                    {
                        row.add("");    
                    }
                }
                else if (type.equals("S"))
                {
                    List<Long> user_super_group_ids = user_group_ids_list.get(j);
                    if (user_super_group_ids.size() > i)
                    {   
                        for (SuperGroup sg: targetSupergroups)
                        {
                            if (sg.getId() == user_super_group_ids.get(i))
                            {
                                row.add(sg.getGroupName());
                            }
                        }
                        //row.add(user_super_group_ids.get(i) + "");
                    }
                    else
                    {
                        row.add("");
                    }
                }

            }
            rowsList.add(row);
        }

        Board board = new Board(160);
        Table table = new Table(board, 160, headersList, rowsList);
        table.getColWidthsList();
        table.setColAlignsList(colAlignList);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);
        Block tableBlock = table.tableToBlocks();
        board.setInitialBlock(tableBlock);
        board.build();
        String tableString = board.getPreview();
        int width = 120;
        String delimiter = "=";
        System.out.println(centerString(title, width, delimiter));
        System.out.println("".repeat(width));
        System.out.println(tableString);
    }

    public static void userMoreInfoMenu()
    {
        boolean loop = true;
        System.out.println("Options: ");
        System.out.println("----------------------------------------------------------");
        System.out.println("1. See a list of \"basic\" group that user belongs to");
        System.out.println("2. See a list of \"super\" group that user belongs to");
        System.out.println("3. Clear the terminal");
        System.out.println("0. Back");
        System.out.println("----------------------------------------------------------");
        System.out.println("Please choose a number: 0-1-2-3");

        while (loop)
        {
            System.out.print("Your choice is: ");
            int choice = sc.nextInt();
            switch (choice) {
                case 1: 
                    showUserGroups("USER BASIC GROUP LIST", "B");
                    break;
                case 2:
                    showUserGroups("USER SUPER GROUP LIST", "S");
                    break;
                case 3:
                    System.out.print("\033[H\033[2J");
                    break;
                case 0:
                    loop = false;
                    break;
                default:
                    System.out.println("Invalid value. Please choose a number: 0-1-2");
            }
        }
    }


    public static void showUsers()
    {
        String title = "USER LIST";
        int width = 120;
        String delimiter = "=";
        System.out.println(centerString(title, width, delimiter));
        updateData();
        List<String> headersList = Arrays.asList("User ID", "First Name", "Last Name", "UserName", "Phone Number", "IsScam", "IsFake", "User Type");
        List<List<String>> rowsList = new ArrayList<>();
        for (User user: targetUsers)
        {
            List<String> row = new ArrayList<>();
            row.add(user.getId()+"");
            row.add(user.getFirstName());
            row.add(user.getLastName());
            row.add(user.getUserName());
            row.add(user.getPhoneNumber());
            row.add((user.getIsScam()) ? "Yes" : "No");
            row.add((user.getIsFake()) ? "Yes" : "No");
            row.add(user.getType());
            rowsList.add(row);
        }
        Board board = new Board(160);
        Table table = new Table(board, 160, headersList, rowsList);
        table.getColWidthsList();
        List<Integer> colAlignList = Arrays.asList(
            Block.DATA_CENTER, 
            Block.DATA_CENTER, 
            Block.DATA_CENTER, 
            Block.DATA_CENTER,
            Block.DATA_CENTER,
            Block.DATA_CENTER,
            Block.DATA_CENTER, 
            Block.DATA_CENTER);
        table.setColAlignsList(colAlignList);
        List<Integer> colWidthsListEdited = Arrays.asList(20, 20, 20, 20, 15, 6, 6, 20);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);
        Block tableBlock = table.tableToBlocks();
        board.setInitialBlock(tableBlock);
        board.build();
        String tableString = board.getPreview();
        System.out.println(tableString);
        userMoreInfoMenu();
        
    }
    private static void getCommand() {
        String command = promptString(commandsLine);
        // System.out.println(command+"================================");
        String[] commands = command.split(" ");
        try {
            switch (commands[0]) {
                case "showUsers": {
                    showUsers();
                    break;
                }
                case "update": {
                    updateData();
                    Thread.sleep(9000);
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
                case "am": {
                    System.out.println("###################################################################");
                    System.out.println("Basic Group List:");
                    if (targetBasicGroups.size() == 0)
                    {
                        System.out.println("[+] There is no basic group in your chat list that you are admin");
                        System.out.println("[+] You can try to update the data again");
                        break;
                    } 
                    else 
                    {
                        for (BasicGroup bsg: targetBasicGroups)
                        {
                            System.out.println(bsg.getGroupName() + ": " + bsg.getChatId());
                        }
                    }

                    System.out.println("Super Groups List: ");
                    if (targetSupergroups.size() == 0)
                    {
                        System.out.println("[+] There is no super group in your chat list that you are admin");
                        System.out.println("[+] You can try to update the data again");
                        break;
                    }
                    else
                    {
                        for (SuperGroup sg: targetSupergroups)
                        {   
                            System.out.println(sg.getGroupName() + ": " + sg.getChatId());
                        }
                    }

                    System.out.println("User List: ");
                    if (targetUsers.size() == 0)
                    {
                        System.out.println("[+] There is no user in your chat list that you are admin");
                        System.out.println("[+] You can try to update the data again by commading \"updata\"");
                        break;
                    }
                    else
                    {
                        for (User u: targetUsers)
                        {
                           System.out.println(u.getDisplayName() + ": " + u.getId());
                        }
                    }
                    
                    // for (Map.Entry<Long, TdApi.Chat> chat : chats.entrySet())
                    // {
                    //     if (chat.getValue().type.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR)
                    //     {
                    //         System.out.println(chat.getValue().title + ": " + ((TdApi.ChatTypePrivate) chat.getValue().type).userId);
                    //     }
                    //     else if (chat.getValue().type.getConstructor() == TdApi.ChatTypeSecret.CONSTRUCTOR)
                    //     {
                    //         System.out.println(chat.getValue().title + ": " + ((TdApi.ChatTypeSecret) chat.getValue().type).userId);
                    //     }
                    // }
                    String chatId = promptString("Enter group Id: ");
                    String UserId = promptString("Enter user Id: ");
                    client.send(new TdApi.AddChatMember(toLong(chatId), toLong(UserId), 0), new UpdateHandler());
                    break;
                    
                }
                case "ku": {
                    client.send(new TdApi.BanChatMember(toLong(commands[1]), new TdApi.MessageSenderChat(toLong(commands[2])), 0, true), new UpdateHandler());
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
    
    public static void main(String[] args) throws InterruptedException {
        
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
            openchat();

            while (haveAuthorization && haveFullMainChatList) {
                getCommand();
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
        }
    }

}

/*‘’’package com.project.main;

import com.project.createchannels.CreateChannels;
import com.project.slackdatafetching.*;
import com.project.inviteusers.*;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

	private static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) throws Exception {
		//autoFetching();
		showlogo();
		showMenu();
		sc.close();
	}

	public static void showlogo() {
		String logo = "\n  _  _  _  _  _.---------------.\n"
				+ ".'\\__\\'\\__\\'\\__\\'\\__\\'\\__,`   .  __
_ \\\n"
				+ "|\\/ _\\/ __\\/ __\\/ __\\/ _:\\   |`.  \\  \\__ \\\n"
				+ " \\\\'\\__\\'\\__\\'\\__\\'\\__\\'\\_.__|\"\". \\  \\___ \\\n"
				+ "  \\\\/ _\\/ __\\/ __\\/ __\\/ _:                \\\n"
				+ "   \\\\'\\__\\'\\__\\'\\__\\ \\__\\'\\_;-----------------`\n"
				+ "    \\\\/   \\/   \\/   \\/   \\/ :               tk|\n"
				+ "     \\|______________________;________________|\n";

		String title = "WELCOME TO SLACK MANAGEMENT PROGRAM!";
		int width = 60;
		String line = "-".repeat(width);
		
		System.out.println(logo);
		System.out.println(line);
		System.out.println(centerString(title, width));
		System.out.println(line);
	}

	public static void showMenu() throws Exception {
		String menu = "\nPlease select an option:\n\n" + "1. Show Slack's channels\n"
				+ "2. Show Slack user's information\n" + "3. Create a channels\n" + "4. Invite user to channel\n" + "5. User management\n"
				+ "0. Exit\n\n" + "Enter your choice (1-5): ";
		System.out.print(menu);
		int option = sc.nextInt();
		
		switch (option) {
		case 0:
			System.out.println("Program ended");
			break;
		case 1:
			showChannels();
			break;
		case 2:
			showUsers();
			break;
		case 3:
			createChannel();
			break;
    		case 4:
			inviteUser();
			break;
		case 5:
			manageUsers();
			break;
		default:
			System.out.println("Invalid input!");
			showMenu();
			break;
		}
	}

	public static void autoFetching() {
		try {
			SlackDataFetching.airtableFetching();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void showUsers() throws Exception {
		SlackDataFetching.printUsers();
		System.out.println("Press Enter key to get back...");
		System.in.read();
		showMenu();
	}

	public static void showChannels() throws Exception {
		SlackDataFetching.printChannels();
		System.out.println("Press Enter key to get back...");
		System.in.read();
		showMenu();
	}

	public static void createChannel() throws Exception {
		// create channel bang slack API
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Note: Channel names have a 21-character limit and can include lowercase letters, non-Latin characters, numbers, and hyphens.\nEnter channel's name:\n");
		String channelName = reader.readLine();
		if (channelName.trim().isEmpty()) {
			System.out.println("Channel name cannot be empty.");
			System.out.println("Press Enter to get back...");
			System.in.read();
			showMenu();
		}		
    
		System.out.print("Enter '0' for private channel or '1' for public channel:\n");
		String channelType = reader.readLine();
		boolean isPrivate = false;

		while (!channelType.equals("0") && !channelType.equals("1")) {
		    System.out.println("Invalid input! Please enter '0' for private channel or '1' for public channel:");
		    channelType = reader.readLine();
		}

		isPrivate = channelType.equals("0");
		
		System.out.print("Enter channel' description (optional):\n");
		String description = reader.readLine();
		CreateChannels.createChannel(channelName, description, isPrivate);
    System.out.println("Press Enter key to get back...");
		System.in.read();
		showMenu();
	}

	public static void inviteUser() throws Exception {
		//for debug
		System.out.println("inviteUser");
		InviteUsers.inviteUser();
    System.out.println("Press Enter key to get back...");
		System.in.read();
		showMenu();
	}

	public static void manageUsers() {
		//for debug
		System.out.println("manageUsers");
		System.out.println("Press Enter key to get back...");
		System.in.read();
		showMenu();
	}

	public static String centerString(String text, int width) {
		if (text.length() > width) {
			return text.substring(0, width);
		} else {
			int padding = width - text.length();
			int leftPadding = padding / 2;
			int rightPadding = padding - leftPadding;
			return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
		}
	}

}
‘’’ */
