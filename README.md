# Table of contents

1. [Introduction](#introduction)
2. [Installation](#install)
3. [Usage](#usage)
4. [Contact information](#contact)

# Introduction <a name="introduction"></a>
### Task describing 
Our job is to create a program that controll the Telegram and Airtable service. The program contain the following feature:
    
    1. Automatically synching
    2. Manually synching
    3. Create a new group
    4. Add user to a group
    5. Kick user from a group

# Installation <a name="install"></a>
#### Required dependencies
    1. gson-2.10.1.jar
    2. guava-31.0.1-jre.jar
    3. httpclient5-5.2.1.jar
    4. httpcore5-5.2.2.jar
    5. httpcore5-h2-5.2.2.jar
    6. log4j-1.2.17.jar
    7. slf4j-api-2.0.7.jar
    8. https://github.com/tdlib/td (install as the guild and compress to jar then add to the referenced library)
    9. https://github.com/thedathoudarya/WAGU-data-in-table-view (download this repository and compress to jar then add to the referenced library)
##### you can get all the dependencies at the "dependencies.zip" file
#### Configuration
    1. Config file "telegram.properties"
     - apiId — Application identifier for accessing the Telegram  API, which can be obtained at https://my.telegram.org.
     - apiHash — Hash of the Application identifier for accessing the Telegram API, which can be obtained at https://my.telegram.org.
    2. Config file "airtable.properties"
     - token: obtained at https://airtable.com/create/tokens which has the following permissions
     - Look at the URL you can get the baseID, userTableID, basicGroupTableID and superGroupTableID
     "https://airtable.com/{baseID}/{tableID}"
    3. Config the table structure
        3.1. User table
            + field 1: 
                    name: Id
                    type: Single line text
            + field 2:
                    name: FirstName 
                    type: Single line text
            + field 3:
                    name: LastName
                    type: Single line text
            + field 4:
                    name: UserName
                    type: Single line text
            + field 5:
                    name: PhoneNumber
                    type: Single line text
            + field 6: 
                    name: IsScam
                    type: Checkbox
            + field 7: 
                    name: IsFake
                    type: Checkbox
            + field 8:
                    name: LanguageCode
                    type: Long text
            + field 9:
                    name: type
                    type: Long text
            + field 10:
                    name: MemberOfBasicGroup
                    type: Link to BasicGroup
                    Allow link to multiple records
            + field 11:
                    name: AdminOfBasicGroup
                    type: Link to BasicGroup
                    Allow link to multiple records
            + field 12:
                    name: MemberOfSuperGroup
                    type: Link to SuperGroup
                    Allow link to multiple records
            + field 13:
                    name: AdminOfSuperGroup
                    type: Link to SuperGroup
                    Allow link to multiple records
        3.2. BasicGroup table
            + field 1: 
                    name: Id
                    type: Single line text
            + field 2:
                    name: ChatID
                    type: Number
                    formatting: - Integer
                                - Allow negative number
            + field 3: 
                    name: GroupName
                    type: Single line text
            + field 4:
                    name: Permission
                    type: Long text
            + field 5:
                    name: MessageAutoDeleteTime
                    type: Number
                    formatting: - Integer
                                - No allow negative number
            + field 6:
                    name: MemberCount
                    type: Number
                    formatting: - Integer
                                - No allow negative number
            + field 7: 
                    name: Description
                    type: Long text
            + field 8:
                    name: InviteLink
                    type: URL
            + field 9: 
                    name: Message
                    type: Long text
            + field 10:
                    name: Admin
                    type: Link to User
                    Allow link to multiple records
            + field 11:
                    name: Member
                    type: Link to User
                    Allow link to multiple records
        3.3. SuperGroup table
            - similar to the BasicGroup table but there is one more field
            name: IsAllHistoryAvailable
            type: Checkbox
###### Note: Please create table with the fields as describing above.
#### Set up automatical synchronous feature
    Step 1: open "Syncronize.bat" file in the repository and change the path to your java.exe, and change the path to "Telegram Manager.jar" file
    Step 2: open the task scheduler in the window search box
    Step 3: on the right hand box, click Create Task
    Step 4: enter the name of the task
    Step 5: at the Security options, choose "Run whether user is logged on or not" and select the "Do not store password ....". Finally, select "Run with highest privileges" box
    Step 6: at the Triggers window, click New and a New Trigger window will be popped up
    Step 7: at Settings section, select "Daily"
    Step 8: on the right handside of the Settings section, Change the time to what you desired and click "OK"
    Step 9: at the Actions wintdow, click New and a New Action window will be popped up
    Step 10: at the Action box, choose Start a program
    Step 11: at the Program/script box, enter the path to the "Syncronize.bat" file
    Step 12: at the Start in (option) box, enter the path to the "Telegram Manager" directory in the repository and hit "OK"
    Step 13: hit "OK"
    Step 14: at the Actions section on the right handside click "Enable All Tasks History". Now every changes of this task will be logged at the History section
# Usage <a name="usage"></a>
Find the "TelegramManager.java" file and Run

# Contact information <a name="contact"></a>
Nguyễn Đức Tâm 20210767
Email: tam.nd210767@sis.hust.edu.vn

Nguyễn Hoàng Anh 20214945
Email: and.nh214945@sis.hust.edu.vn
