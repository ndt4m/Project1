package hust.soict.cybersec.tm.entity;

import java.util.Set;

import com.google.gson.JsonObject;

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
        s.append(" " + lastName);
        return s.toString();
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getType() {
        return type;
    }

    public String getUserName() {
        return userName;
    }
    public Set<Long> getUser_basic_group_ids() {
        return user_basic_group_ids;
    }

    public Set<Long> getUser_super_group_ids() {
        return user_super_group_ids;
    }

    public boolean getIsScam() {
        return isScam;
    }

    public boolean getIsFake() {
        return isFake;
    }

    public JsonObject toJson(){
        JsonObject fields = new JsonObject();
        fields.addProperty("Id", String.valueOf(getId()));
        fields.addProperty("FirstName", getFirstName());
        fields.addProperty("LastName", getLastName());
        fields.addProperty("UserName", getUserName());
        fields.addProperty("PhoneNumber", getPhoneNumber());
        fields.addProperty("IsScam", getIsScam());
        fields.addProperty("IsFake", getIsFake());
        fields.addProperty("LanguageCode", getLanguageCode());
        fields.addProperty("Type", getType());

        return fields;
    }
}
