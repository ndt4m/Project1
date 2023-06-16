package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.crawling.SuperGroupInfoCrawler.UpdateSuperGroupHandler;
import hust.soict.cybersec.tm.crawling.UserInfoCrawler.UpdateUserHandler;

public class UserInfoCrawler extends Crawler
{
    private Client.ResultHandler updateUserHandler = new UpdateUserHandler();

    private long id = 0l;
    private String firstName = "Không rõ";
    private String lastName = "Không rõ";
    private String userName = "Không rõ";
    private String phoneNumber = "Không rõ";
    private int isScam = -1;
    private int isFake = -1;
    private String languageCode = "Không rõ";
    private String type = "Không rõ";
    private List<Long> user_basic_group_ids = new ArrayList<>();
    private List<Long> user_super_group_ids = new ArrayList<>();

    public UserInfoCrawler()
    {

    }

    public UserInfoCrawler(Client client)
    {
        super(client);

    }

    public void redefinedAttributes()
    {
        id = 0l;
        permissions = null;
        canBeDeletedOnlyForSelf = -1;
        canBeDeletedForAllUsers = -1;
        defaultDisableNotification = -1;
        messageAutoDeleteTime = -1;
        firstName = "Không rõ";
        lastName = "Không rõ";
        userName = "Không rõ";
        phoneNumber = "Không rõ";
        isScam = -1;
        isFake = -1;
        languageCode = "Không rõ";
        type = "Không rõ";
    }


    public void crawlUserInfo()
    {
        
    }


    class UpdateUserHandler implements Client.ResultHandler
    {
        @Override
        public void onResult(TdApi.Object object)
        {
            switch (object.getConstructor())
            {

            }
        }
    }
}
