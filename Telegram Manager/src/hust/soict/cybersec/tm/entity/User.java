package hust.soict.cybersec.tm.entity;

import java.util.HashSet;
import java.util.Set;

public class User 
{
    private long id;
    private String firstName;
    private String lastName;
    private String userName;
    private String phoneNumber;
    private boolean isScam;
    private boolean isFake;
    private String languageCode;
    private String type;
    private Set<Long> user_basic_group_ids;
    private Set<Long> user_super_group_ids;

    public User()
    {

    }


    public User(long id,
                String firstName,
                String lastName,
                String userName,
                String phoneNumber,
                boolean isScam,
                boolean isFake,
                String languageCode,
                String type,
                Set<Long> user_basic_group_ids,
                Set<Long> user_super_group_ids)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.isScam = isScam;
        this.isFake = isFake;
        this.languageCode = languageCode;
        this.type = type;
        this.user_basic_group_ids = user_basic_group_ids;
        this.user_super_group_ids = user_super_group_ids;
    }

    public String getDisplayName()
    {
        StringBuilder s = new StringBuilder(firstName);
        s.append(lastName);
        return s.toString();
    }

    public long getId() {
        return id;
    }
}
