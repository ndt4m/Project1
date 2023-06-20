package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.entity.BasicGroup;
import hust.soict.cybersec.tm.entity.SuperGroup;

public class UserInfoCrawler extends Crawler
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
        user_basic_group_ids.clear();
        user_super_group_ids.clear();
    }


    public void crawlUserInfo() throws InterruptedException
    {
        
        for ()

        for (BasicGroup basicGroup: basicGroups)
        {
            for (long userId : basicGroup.getMemberIds())
            {   
                id = userId;
                
                user_basic_group_ids.add(basicGroup.getId());
                blockingSend(new TdApi.GetUser(id), updateUserHandler);
            }
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
                    userName = user.usernames.activeUsernames[0];
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
