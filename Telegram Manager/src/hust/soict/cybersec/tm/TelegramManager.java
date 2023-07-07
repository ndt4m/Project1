package hust.soict.cybersec.tm;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.Client.ResultHandler;

import org.drinkless.tdlib.TdApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import hust.soict.cybersec.tm.airtable.AirTable;
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
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public final class TelegramManager extends Base{
    

    private static Scanner sc = new Scanner(System.in);
            
    static {
        
        Client.setLogMessageHandler(0, new LogMessageHandler());

        
        Client.execute(new TdApi.SetLogVerbosityLevel(0));
        if (Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false))) instanceof TdApi.Error) {
            throw new IOError(new IOException("Write access to the current directory is required"));
        }

        
        client = Client.create(new UpdateHandler(), null, null);
        
    }

    private static List<BasicGroup> targetBasicGroups = new ArrayList<BasicGroup>();
    private static List<SuperGroup> targetSupergroups = new ArrayList<SuperGroup>();
    private static List<User> targetUsers = new ArrayList<User>();

    public static void openChat()
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
                            
                            break;
                        
                        default: 
                            printColor(RED, "Unhandle response: " + object.toString());
                        
                    }
                }
            });
            
            
        }
    }

    public static void updateData() 
    {   
        
        openChat();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            
            
        }
        FileWriter fwb = null;
        FileWriter fws = null;
        FileWriter fwu = null;
        try {
           
            SuperGroupInfoCrawler sgCrawler = new SuperGroupInfoCrawler(chats, client);
            sgCrawler.crawlSuperGroupInfo();
            targetSupergroups = sgCrawler.getCollection();
            BasicGroupInfoCrawler bgCrawler = new BasicGroupInfoCrawler(chats, client);
            bgCrawler.crawlBasicGroupInfo();
            targetBasicGroups = bgCrawler.getCollection();
            UserInfoCrawler uCrawler = new UserInfoCrawler(client, bgCrawler.getCollection(), sgCrawler.getCollection());
            uCrawler.crawlUserInfo();
            targetUsers = uCrawler.getCollection();
            
            fwb = new FileWriter("basicGroups.json");
            fws = new FileWriter("superGroups.json");  
            fwu = new FileWriter("users.json");

            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            gson.toJson(targetBasicGroups, fwb);
            gson.toJson(targetSupergroups, fws);
            gson.toJson(targetUsers, fwu);
            fwb.close();
            fws.close();
            fwu.close();
        } catch (InterruptedException | IOException e) {
            //logger.error("Error occurred: ", e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            try {
                fwb.close();
                fws.close();
                fwu.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Thread.currentThread().interrupt();
            }
        }
         
    }

    public static void showMainMenu()
    {
        printColor(MAGENTA, "TELEGRAM MANAGER: ");
        printColor(MAGENTA, "--------------------------------");
        printColor(MAGENTA, "1. Synchronize data to AirTable");
        printColor(MAGENTA, "2. Show user information");
        printColor(MAGENTA, "3. Show \"basic\" group information");
        printColor(MAGENTA, "4. Show \"super\" group information");
        printColor(MAGENTA, "5. Create \"basic\" group");
        printColor(MAGENTA, "6. Create \"super\" group");
        printColor(MAGENTA, "7. Add member");
        printColor(MAGENTA, "8. Kick member");
        printColor(MAGENTA, "9. Update data");
        printColor(MAGENTA, "10. Log out");
        printColor(MAGENTA, "0. Quit");
        printColor(MAGENTA, "--------------------------------");
        printColor(MAGENTA, "Please choose a number: 0-1-2-3-4-5-6-7-8-9-10");
    }

    public static void autoSynchronize()
    {
        printColor(GREEN, centerString("Create client", 180, "*"));
        authorizationLock.lock();
            try {
                while (!haveAuthorization) {
                    try {
                        gotAuthorization.await();
                    } catch (InterruptedException e) {
                       
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
                        
                    }
                }
            } finally {
                authorizationLock.unlock();
            }
            printColor(GREEN, centerString("Update data", 180, "*"));
            updateData();
            new AirTable().push(targetUsers,targetBasicGroups,targetSupergroups);
            printColor(GREEN, centerString("Syncronize is completed successfully!", 180, "*"));

    }
    
    public static void showUserGroups(String title, String type)
    {
        printColor(GREEN, centerString(title, 180, "#"));
        List<String> headersList = new ArrayList<String>();

        List<Integer> colAlignList = new ArrayList<>();
        List<Integer> colWidthsListEdited = new ArrayList<>();
        
        List<List<Long>> Id_List = new ArrayList<>();
        List<Integer> Id_Count_List = new ArrayList<>();
        for (User user : targetUsers)
        {
            headersList.add(user.getDisplayName());
            colAlignList.add(Block.DATA_CENTER);
            colWidthsListEdited.add(18);
            if (type.equals("B"))
            {
                Id_List.add(new ArrayList<>(user.getUser_basic_group_ids()));
                Id_Count_List.add(user.getUser_basic_group_ids().size());
            }
            else if (type.equals("S"))
            {
                Id_List.add(new ArrayList<>(user.getUser_super_group_ids()));
                Id_Count_List.add(user.getUser_super_group_ids().size());
            }
        }

        
        for (int k = 0; k < (int) Math.ceil((double) headersList.size() / 5); k++)
        {
            List<List<String>> rowList = new ArrayList<List<String>>();
            for (int i = 0; i < Collections.max(Id_Count_List.subList(k*5, Math.min(k*5+5, headersList.size()))); i++)
            {   
                List<String> row = new ArrayList<String>();
                for (int j = 0; j < headersList.subList(k*5, Math.min(k*5+5, headersList.size())).size(); j++)
                {   
                    if (type.equals("B"))
                    {
                        List<Long> user_basic_group_ids = Id_List.get(j);
                        if (user_basic_group_ids.size() > i)
                        {
                            
                            for (BasicGroup bs: targetBasicGroups)
                            {
                                if (bs.getId() == user_basic_group_ids.get(i))
                                {
                                    row.add(bs.getGroupName());
                                    
                                    break;
                                }
                            }
                            
                        }
                        else
                        {
                            row.add("");    
                        }
                    }
                    else if (type.equals("S"))
                    {
                        List<Long> user_super_group_ids = Id_List.get(j);
                        if (user_super_group_ids.size() > i)
                        {   
                            for (SuperGroup sg: targetSupergroups)
                            {
                                if (sg.getId() == user_super_group_ids.get(i))
                                {
                                    row.add(sg.getGroupName());
                                    break;
                                }
                            }
                            
                        }
                        else
                        {
                            row.add("");
                        }
                    }

                }
                rowList.add(row);
            }
            if (rowList.size() == 0)
            {
                List<String> row = new ArrayList<String>();
                for (int i = 0; i < headersList.subList(k*5, Math.min(k*5+5, headersList.size())).size(); i++)
                {
                    row.add(" ");
                }
                rowList.add(row);
            }
            printColor(BLUE, createTable(headersList.subList(k*5, Math.min(k*5+5, headersList.size())), rowList, colAlignList.subList(k*5, Math.min(k*5+5, headersList.size())), colWidthsListEdited.subList(k*5, Math.min(k*5+5, headersList.size()))));
            if (k != (int) Math.ceil((double) headersList.size() / 5) - 1)
            {
                printColor(GREEN, centerString(title + " CONTINUES", 180, " "));
            }
        }
    }

    public static void userMoreInfoMenu()
    {
        printColor(MAGENTA, "Options: ");
        printColor(MAGENTA, "----------------------------------------------------------");
        printColor(MAGENTA, "1. See a list of \"basic\" group that user belongs to");
        printColor(MAGENTA, "2. See a list of \"super\" group that user belongs to");
        printColor(MAGENTA, "0. Back");
        printColor(MAGENTA, "----------------------------------------------------------");
        printColor(MAGENTA, "Please choose a number: 0-1-2");
    }

    public static void chooseOptionUserMoreInfoMenu()
    {
        int choice = -1;
        boolean loop = true;
        while (loop)
        {   
            userMoreInfoMenu();
            try {
                printColor(MAGENTA, "Your choice is: ");
                choice = sc.nextInt();
            } catch (InputMismatchException e) {
                choice = -1;
                sc.nextLine();
            }

            switch (choice) {
                case 1: 
                    showUserGroups("USER BASIC GROUP LIST", "B");
                    break;
                case 2:
                    showUserGroups("USER SUPER GROUP LIST", "S");
                    break;
                case 0:
                    loop = false;
                    break;
                default:
                    printColor(RED, centerString("Invalid value. Please choose a number: 0-1-2", 180, "#"));
            }
        }
        
    }

    public static void showUsers()
    {
        printColor(GREEN, centerString("USER LIST", 180, "#"));
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
        List<Integer> colAlignList = new ArrayList<>();
        for (int i = 0; i < headersList.size(); i++) 
        {
            colAlignList.add(Block.DATA_CENTER);
        }
        List<Integer> colWidthsListEdited = Arrays.asList(20, 20, 20, 20, 15, 6, 6, 20);
       
    
        printColor(BLUE, createTable(headersList, rowsList, colAlignList, colWidthsListEdited));
        chooseOptionUserMoreInfoMenu();
        
    }

    public static void showBasicGroups()
    {
        printColor(GREEN, centerString("BASIC GROUP LIST", 180, "#"));
        updateData();
        List<String> headersList = Arrays.asList("Basic Group ID", "Chat ID", "Group Name", "Message Auto Delete Time", "Member Count","Description", "Invite Link");
        List<List<String>> rowList = new ArrayList<>();
        for (BasicGroup bs : targetBasicGroups)
        {
            List<String> row = new ArrayList<>();
            row.add(bs.getId()+"");
            row.add(bs.getChatId()+"");
            row.add(bs.getGroupName());
            row.add(bs.getMessageAutoDeleteTime()+"");
            row.add(bs.getMemberCount()+"");
            row.add(bs.getDescription());
            row.add(bs.getInviteLink());
            rowList.add(row);
        }

        List<Integer> colAlignList = new ArrayList<>();
        for (int i = 0; i < headersList.size(); i++)
        {
            colAlignList.add(Block.DATA_CENTER);
        }

        List<Integer> colWidthsListEdited = Arrays.asList(12, 12, 20, 24, 12, 40, 40);
        printColor(BLUE, createTable(headersList, rowList, colAlignList, colWidthsListEdited));
        chooseOptionGroupMoreInfoMenu("B");
    }

    public static void groupMoreInfoMenu()
    {
        printColor(MAGENTA, "Options: ");
        printColor(MAGENTA, "----------------------------------------------------------");
        printColor(MAGENTA, "1. See group permissions");
        printColor(MAGENTA, "2. See admin list");
        printColor(MAGENTA, "3. See member list");
        printColor(MAGENTA, "0. Back");
        printColor(MAGENTA, "----------------------------------------------------------");
        printColor(MAGENTA, "Please choose a number: 0-1-2-3");
    }

    public static void chooseOptionGroupMoreInfoMenu(String type)
    {
        int choice = -1;
        boolean loop = true;
        while (loop)
        {
            groupMoreInfoMenu();
            try {
                printColor(MAGENTA, "Your choice is: ");
                choice = sc.nextInt();
            } catch (InputMismatchException e) {
                choice = -1;
                sc.nextLine();
            }
            switch (choice) {
                case 1: 
                    showPermissions(type);
                    break;
                case 2:
                    showAdminOrMemList("ADMIN LIST", type, "ADMIN");
                    break;
                case 3:
                    showAdminOrMemList("MEMBER LIST", type, "MEM");
                    break;
                case 0:
                    loop = false;
                    break;
                default:
                    printColor(RED, centerString("Invalid value. Please choose a number: 0-1-2-3", 180, "#"));
            }
        }
        
    }

    public static void showPermissions(String type)
    {
        List<String> headersList = Arrays.asList("", "BasicMessages", "Audios", "Documents", "Photos", "Videos","VideoNotes", "VoiceNotes", "Polls", "OtherMessages", "AddWebPagePreviews", "ChangeInfo", "InviteUsers", "PinMessages", "ManageTopics");
        List<Integer> colAlignList = new ArrayList<>();
        List<List<String>> rowList = new ArrayList<List<String>>();
        for (int i = 0; i < headersList.size(); i++)
        {
            colAlignList.add(Block.DATA_CENTER);
        }
        
        String title = " Permissions";
        if (type.equals("B"))
        {
            title = "Basic Group" + title;
            for (BasicGroup bs: targetBasicGroups)
            {
                List<String> row = new ArrayList<>();
                row.add(bs.getGroupName());
                row.add(bs.getPermissions().canSendBasicMessages+"");
                row.add(bs.getPermissions().canSendAudios+"");
                row.add(bs.getPermissions().canSendDocuments+"");
                row.add(bs.getPermissions().canSendPhotos+"");
                row.add(bs.getPermissions().canSendVideos+"");
                row.add(bs.getPermissions().canSendVideoNotes+"");
                row.add(bs.getPermissions().canSendVoiceNotes+"");
                row.add(bs.getPermissions().canSendPolls+"");
                row.add(bs.getPermissions().canSendOtherMessages+"");
                row.add(bs.getPermissions().canAddWebPagePreviews+"");
                row.add(bs.getPermissions().canChangeInfo+"");
                row.add(bs.getPermissions().canInviteUsers+"");
                row.add(bs.getPermissions().canPinMessages+"");
                row.add(bs.getPermissions().canManageTopics+"");
                rowList.add(row);
            }
        }
        else if (type.equals("S"))
        {
            title = "Super Group" + title;
            for (SuperGroup sg: targetSupergroups)
            {
                List<String> row = new ArrayList<>();
                row.add(sg.getGroupName());
                row.add(sg.getPermissions().canSendBasicMessages+"");
                row.add(sg.getPermissions().canSendAudios+"");
                row.add(sg.getPermissions().canSendDocuments+"");
                row.add(sg.getPermissions().canSendPhotos+"");
                row.add(sg.getPermissions().canSendVideos+"");
                row.add(sg.getPermissions().canSendVideoNotes+"");
                row.add(sg.getPermissions().canSendVoiceNotes+"");
                row.add(sg.getPermissions().canSendPolls+"");
                row.add(sg.getPermissions().canSendOtherMessages+"");
                row.add(sg.getPermissions().canAddWebPagePreviews+"");
                row.add(sg.getPermissions().canChangeInfo+"");
                row.add(sg.getPermissions().canInviteUsers+"");
                row.add(sg.getPermissions().canPinMessages+"");
                row.add(sg.getPermissions().canManageTopics+"");
                rowList.add(row);
            }
        }
        printColor(GREEN, centerString(title, 180, "#"));
        List<Integer> colWidthsListEdited = Arrays.asList(15, 14, 7, 10, 7, 7, 11, 11, 6, 14, 19, 11, 12, 12, 13);
        printColor(BLUE, createTable(headersList, rowList, colAlignList, colWidthsListEdited));
    }

    public static void showAdminOrMemList(String title, String type, String mode)
    {
        printColor(GREEN, centerString(title, 180, "#"));
        
        List<String> headersList = new ArrayList<String>();
        
        List<Integer> colAlignList = new ArrayList<>();
        List<Integer> colWidthsListEdited = new ArrayList<>();
        List<List<Long>> Id_List = new ArrayList<>();
        List<Integer> Id_Count_List = new ArrayList<>();
        if (type.equals("B"))
        {
            for (BasicGroup bs: targetBasicGroups)
            {
                headersList.add(bs.getGroupName());
                colAlignList.add(Block.DATA_CENTER);
                colWidthsListEdited.add(18);
                if (mode.equals("ADMIN"))
                {
                    Id_List.add(new ArrayList<>(bs.getAdminIds()));
                    Id_Count_List.add(bs.getAdminIds().size());
                }
                else if (mode.equals("MEM"))
                {
                    Id_List.add(new ArrayList<>(bs.getMemberIds()));
                    Id_Count_List.add(bs.getMemberIds().size());
                }
            }
        }
        else if (type.equals("S"))
        {
            for (SuperGroup sg: targetSupergroups)
            {
                headersList.add(sg.getGroupName());
                colAlignList.add(Block.DATA_CENTER);
                colWidthsListEdited.add(18);
                if (mode.equals("ADMIN"))
                {
                    Id_List.add(new ArrayList<>(sg.getAdminIds()));
                    Id_Count_List.add(sg.getAdminIds().size());
                }
                else if (mode.equals("MEM"))
                {
                    Id_List.add(new ArrayList<>(sg.getMemberIds()));
                    Id_Count_List.add(sg.getMemberIds().size());
                }
            }
        }

        for (int k = 0; k < (int) Math.ceil((double) headersList.size() / 5); k++)
        {
            List<List<String>> rowList = new ArrayList<List<String>>();
            for (int i = 0; i < Collections.max(Id_Count_List.subList(k*5, Math.min(k*5+5, headersList.size()))); i++)
            {
                List<String> row = new ArrayList<String>();
                for (int j = 0; j < headersList.subList(k*5, Math.min(k*5+5, headersList.size())).size(); j++)
                {
                    List<Long> Ids = Id_List.get(j);

                    if (Ids.size() > i)
                    {   
                        boolean found = false;
                        for (User user: targetUsers)
                        {
                            if (user.getId() == Ids.get(i))
                            {
                                row.add(user.getDisplayName());
                                found = true;
                                break;
                            }
                            
                        }
                        if (!found)
                        {
                            row.add("bot");
                        }
                    }
                    else
                    {
                        row.add("");
                    }

                }
                rowList.add(row);
            }
             
            
            if (rowList.size() == 0)
            {
                List<String> row = new ArrayList<String>();
                for (int i = 0; i < headersList.subList(k*5, Math.min(k*5+5, headersList.size())).size(); i++)
                {
                    row.add(" ");
                }
                rowList.add(row);
            }
            printColor(BLUE, createTable(headersList.subList(k*5, Math.min(k*5+5, headersList.size())), rowList, colAlignList.subList(k*5, Math.min(k*5+5, headersList.size())), colWidthsListEdited.subList(k*5, Math.min(k*5+5, headersList.size()))));
            if (k != (int) Math.ceil((double) headersList.size() / 5) - 1)
            {
                printColor(GREEN, centerString(title + " CONTINUES", 180, " "));
            }
        }
    }

    public static String createTable(List<String> headersList, List<List<String>> rowsList, List<Integer> colAlignList, List<Integer> colWidthsListEdited)
    {
        
        Board board = new Board(190);
        Table table = new Table(board, 200, headersList, rowsList);
        table.getColWidthsList();
        table.setColAlignsList(colAlignList);
        table.setGridMode(Table.GRID_FULL).setColWidthsList(colWidthsListEdited);
        Block tableBlock = table.tableToBlocks();
        board.setInitialBlock(tableBlock);
        board.build();
        String tableString = board.getPreview();
        return tableString;
    }

    public static void showSuperGroups()
    {
        printColor(GREEN, centerString("SUPER GROUP LIST", 180, "#"));
        updateData();
        List<String> headersList = Arrays.asList("Super Group ID", "Chat ID", "Group Name", "Message Auto Delete Time", "Member Count", "History Available", "Description", "Invite Link");
        List<List<String>> rowList = new ArrayList<>();
        for (SuperGroup sg: targetSupergroups)
        {
            List<String> row = new ArrayList<>();
            row.add(sg.getId()+"");
            row.add(sg.getChatId()+"");
            row.add(sg.getGroupName());
            row.add(sg.getMessageAutoDeleteTime()+"");
            row.add(sg.getMemberCount()+"");
            row.add((sg.getIsAllHistoryAvailable()) ? "Yes" : "No");
            row.add(sg.getDescription());
            row.add(sg.getInviteLink());
            rowList.add(row);
        }

        List<Integer> colAlignList = new ArrayList<>();
        for (int i = 0; i < headersList.size(); i++)
        {
            colAlignList.add(Block.DATA_CENTER);
        }
        List<Integer> colWidthsListEdited = Arrays.asList(12, 12, 20, 24, 12, 17, 40, 40);
        printColor(BLUE, createTable(headersList, rowList, colAlignList, colWidthsListEdited));
        chooseOptionGroupMoreInfoMenu("S");
    }

    public static void chooseOptionMainMenu()
    {
        int choice = -1;
        showMainMenu();
        try {
            printColor(MAGENTA, "Your choice is: ");
            choice = sc.nextInt();
            sc.nextLine();
        } catch (InputMismatchException e) {
            choice = -1;
            sc.nextLine();
        }
        switch (choice) {
            case 1: 
                manualSynchronize();
                break;
            case 2:
                showUsers();
                break;
            case 3:
                showBasicGroups();
                break;
            case 4:
                showSuperGroups();
                break;
            case 5:
                createBasicGroup();
                break;
            case 6:
                createSuperGroup();
                break;
            case 7:
                addMember();
                break;
            case 8:
                kickMember();
                break;
            case 9:
                printColor(GREEN, centerString("UPDATING DATA", 180, "#"));
                updateData();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                   
                }
                updateData();
                printColor(GREEN, centerString("FINISHED!!!", 180, "#"));
                break;
            case 10:
                logOut();
                break;
            case 0:
                quit();
                break;
            default:
                printColor(RED, centerString("Invalid value. Please choose a number: 0-1-2-3-4-5-6-7-8-9-10", 180, "*"));
        }
    }

    public static void createBasicGroup()
    {
        printColor(MAGENTA, "Enter group name: ");
        
        String groupName = sc.nextLine();    
        client.send(new TdApi.CreateNewBasicGroupChat(null, groupName, 0), new Client.ResultHandler() {
            public void onResult(TdApi.Object object)
            {
                if (object.getConstructor() == TdApi.Error.CONSTRUCTOR)
                {
                    printColor(RED, centerString(((TdApi.Error) object).message, 180, "#"));
                    
                }
                else 
                {
                    printColor(GREEN, centerString("New \"basic\" group has been successfully created!", 180, "#"));
                }
            }
        });
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
           
        };
    }

    public static void createSuperGroup()
    {
        printColor(MAGENTA, "Enter group name: ");
        String groupName = sc.nextLine();
        client.send(new TdApi.CreateNewSupergroupChat(groupName, false, false, "", null, 0, false), new Client.ResultHandler() {
            public void onResult(TdApi.Object object)
            {
                if (object.getConstructor() == TdApi.Error.CONSTRUCTOR)
                {
                    printColor(RED, centerString(((TdApi.Error) object).message, 180, "#"));
                    
                }
                else 
                {
                    printColor(GREEN, centerString("New \"super\" group has been successfully created!", 180, "#"));
                }
            }
        });
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
           
        };
    }

    public static void addMember()
    {   
        printColor(MAGENTA, "Enter group chat ID: ");
        String chatId = sc.nextLine();
        printColor(MAGENTA, "Enter user ID: ");
        String userId = sc.nextLine();
        client.send(new TdApi.AddChatMember(toLong(chatId), toLong(userId), 0), new Client.ResultHandler() {
            public void onResult(TdApi.Object object)
            {
                if (object.getConstructor() == TdApi.Error.CONSTRUCTOR)
                {
                    printColor(RED, centerString(((TdApi.Error) object).message, 180, "#"));
                    
                }
                else 
                {
                    printColor(GREEN, centerString("Member has been successfully added!", 180, "#"));
                }
            }
        });
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
           
        };
    }

    public static void kickMember()
    {
        printColor(MAGENTA, "Enter group chat ID: ");
        String chatId = sc.nextLine();
        printColor(MAGENTA, "Enter user ID: ");
        String userId = sc.nextLine();
        client.send(new TdApi.BanChatMember(toLong(chatId), new TdApi.MessageSenderChat(toLong(userId)), 0, true), new Client.ResultHandler() {
            public void onResult(TdApi.Object object)
            {
                if (object.getConstructor() == TdApi.Error.CONSTRUCTOR)
                {
                    printColor(RED, centerString(((TdApi.Error) object).message, 180, "#"));
                    
                }
                else 
                {
                    printColor(GREEN, centerString("Member has been successfully kicked!", 180, "#"));
                }
            }
        });
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
           
        };
    }

    public static void logOut()
    {
        needQuit = true;
        haveAuthorization = false;
        client.send(new TdApi.LogOut(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) 
            {
                switch (object.getConstructor()) 
                {
                    case TdApi.Ok.CONSTRUCTOR:
                        printColor(GREEN, centerString("All local data will be destroyed", 180, "#"));
                        break;
                    default:
                        printColor(RED, "[-] Receive an error: " + object);
                }
            }
        });
    }

    public static void quit()
    {
        needQuit = true;
        haveAuthorization = false;
        client.send(new TdApi.Close(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) 
            {
                switch (object.getConstructor()) 
                {
                    case TdApi.Ok.CONSTRUCTOR:
                        printColor(GREEN, centerString("All databases will be flushed to disk and properly closed", 180, "#"));
                        break;
                    default:
                        printColor(RED, "[-] Receive an error: " + object);
                }
            }
        });
    }
    
    public static void manualSynchronize()
    {
        printColor(GREEN, centerString("START SYNC DATA TO AIRTABLE", 180, "#"));
        updateData();
        
        new AirTable().push(targetUsers,targetBasicGroups,targetSupergroups);

        printColor(GREEN, centerString("FINISH!!!!", 180, "#"));
    }

    public static void main(String[] args) throws InterruptedException {
        
        
        while (!needQuit) {
            
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
            openChat();
            printColor(YELLOW, centerString("                                                               _oo0oo_\r\n" + //
                    "                                                                                      o8888888o\r\n" + //
                    "                                                                                      88\" . \"88\r\n" + //
                    "                                                                                      (| -_- |)\r\n" + //
                    "                                                                                      0\\  =  /0\r\n" + //
                    "                                                                                    ___/`---'\\___\r\n" + //
                    "                                                                                  .' \\\\|     |// '.\r\n" + //
                    "                                                                                 / \\\\|||  :  |||// \\\r\n" + //
                    "                                                                                / _||||| -:- |||||- \\\r\n" + //
                    "                                                                               |   | \\\\\\  -  /// |   |\r\n" + //
                    "                                                                               | \\_|  ''\\---/''  |_/ |\r\n" + //
                    "                                                                               \\  .-\\__  '-'  ___/-. /\r\n" + //
                    "                                                                             ___'. .'  /--.--\\  `. .'___\r\n" + //
                    "                                                                          .\"\" '<  `.___\\_<|>_/___.' >' \"\".\r\n" + //
                    "                                                                         | | :  `- \\`.;`\\ _ /`;.`/ - ` : | |\r\n" + //
                    "                                                                         \\  \\ `_.   \\_ __\\ /__ _/   .-` /  /\r\n" + //
                    "                                                                     =====`-.____`.___ \\_____/___.-`___.-'=====\r\n" + //
                    "                                                                                       `=---='\r\n" + //
                    "                                                                \r\n" + //
                    "                                                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\r\n" + //
                    "                                                                            Ph\u1EADt ph\u00F9 h\u1ED9, kh\u00F4ng bao gi\u1EDD BUG\r\n" + //
                    "                                                                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~", 2266, ""));
		    printColor(GREEN, centerString("WELCOME TO TELEGRAM MANAGEMENT PROGRAM!", 180, "#"));


            while (haveAuthorization && haveFullMainChatList) {
                chooseOptionMainMenu();
                
            }
        }
        while (!canQuit) {
            Thread.sleep(1);
        }
    }

}
