package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;
import hust.soict.cybersec.tm.entity.User;

public class UserInfoCrawler extends Crawler<User>
{
    private Client.ResultHandler updateUserHandler = new UpdateUserHandler();
    private List<BasicGroup> basicGroups = new ArrayList<>();
    private List<SuperGroup> superGroups = new ArrayList<>();

    private long id = 0l;
    private String firstName = "Không rõ";
    private String lastName = "Không rõ";
    private String userName = "Không rõ";
    private String phoneNumber = "Không rõ";
    private int isScam = -1;
    private int isFake = -1;
    private String languageCode = "Không rõ";
    private String type = "Không rõ";
    private Set<Long> user_basic_group_ids = new HashSet<>();
    private Set<Long> user_super_group_ids = new HashSet<>();
    
    public UserInfoCrawler()
    {

    }

    public UserInfoCrawler(Client client, List<BasicGroup> basicGroups, List<SuperGroup> superGroups)
    {
        super(client);
        this.basicGroups = basicGroups;
        this.superGroups = superGroups;
    }

    public void redefinedAttributes()
    {
        id = 0l;
        firstName = "Không rõ";
        lastName = "Không rõ";
        userName = "Không rõ";
        phoneNumber = "Không rõ";
        isScam = -1;
        isFake = -1;
        languageCode = "Không rõ";
        type = "Không rõ";
        user_basic_group_ids = new HashSet<>();
        user_super_group_ids = new HashSet<>();
    }

    public void findUserBasicGroupIds(long userId)
    {
        for (BasicGroup basicGroup: basicGroups)
        {
            if (basicGroup.getMemberIds().contains(userId))
            {
                user_basic_group_ids.add(basicGroup.getId());
            }
        }
    }

    public void findUserSuperGroupIds(long userId)
    {
        for (SuperGroup superGroup: superGroups)
        {
            if (superGroup.getMemberIds().contains(userId))
            {
                user_super_group_ids.add(superGroup.getId());
            }
        }
    }


    public void crawlUserInfo() throws InterruptedException
    {
        Set<Long> userIds = new HashSet<>();
        for (BasicGroup basicGroup: basicGroups)
        {
            for (long userId : basicGroup.getMemberIds())
            {   
                userIds.add(userId);
            }
        }


        for (SuperGroup superGroup: superGroups)
        {
            for (Long userId: superGroup.getMemberIds())
            {
                userIds.add(userId);
            }
        }

        for (long userId: userIds)
        {
            id = userId;
            findUserBasicGroupIds(id);
            findUserSuperGroupIds(id);
            blockingSend(new TdApi.GetUser(id), updateUserHandler);
            // System.out.println("id: " + id);
            // System.out.println("firstName: " + firstName);
            // System.out.println("lastName: " + lastName);
            // System.out.println("userName: " + userName);
            // System.out.println("phoneNumber: " + phoneNumber);
            // System.out.println("isScam: " + isScam);
            // System.out.println("isFake: " + isFake);
            // System.out.println("languageCode: " + languageCode);
            // System.out.println("type: " + type);
            // System.out.println("user_basic_group_ids: " + user_basic_group_ids);
            // System.out.println("user_super_group_ids: " + user_super_group_ids);
            // System.out.println("====================================================================");
            this.addCollection(new User(id, 
                                        firstName, 
                                        lastName, 
                                        userName, 
                                        phoneNumber, 
                                        (isScam == 1) ? true : false, 
                                        (isFake == 1) ? true : false, 
                                        languageCode, 
                                        type, 
                                        user_basic_group_ids, 
                                        user_super_group_ids));
            redefinedAttributes();
        }
    }


    class UpdateUserHandler implements Client.ResultHandler
    {
        @Override
        public void onResult(TdApi.Object object)
        {
            switch (object.getConstructor())
            {
                case TdApi.User.CONSTRUCTOR:
                    TdApi.User user = (TdApi.User) object;
                    firstName = user.firstName;
                    lastName = user.lastName;
                    if (user.usernames != null)
                    {
                        userName = user.usernames.activeUsernames[0];
                    }
                    else
                    {
                        userName = "";
                    }
                    phoneNumber = user.phoneNumber;
                    isScam = user.isScam ? 1 : 0;
                    isFake = user.isFake ? 1 : 0;
                    languageCode = user.languageCode;
                    if (user.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR)
                    {
                        type = "bot user";
                    }
                    else if (user.type.getConstructor() == TdApi.UserTypeRegular.CONSTRUCTOR)
                    {
                        type = "regular user";
                    }
                    break;

                default:
                    //System.out.println(object.toString());
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
