package hust.soict.cybersec.tm;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;


public class UpdateHandler implements Client.ResultHandler 
{
    
    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                //System.out.println("======================================update authorization============================");
                AuthorizationRequestHandler.onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                break;
            case TdApi.UpdateUser.CONSTRUCTOR:
                //System.out.println("====================================update User======================================");
                TdApi.UpdateUser updateUser = (TdApi.UpdateUser) object;
                TelegramManager.users.put(updateUser.user.id, updateUser.user);
                break;
            // case TdApi.UpdateUserStatus.CONSTRUCTOR: {
            //     //System.out.println("====================================update User Status======================================");
            //     TdApi.UpdateUserStatus updateUserStatus = (TdApi.UpdateUserStatus) object;
            //     TdApi.User user = TelegramManager.users.get(updateUserStatus.userId);
            //     synchronized (user) {
            //         user.status = updateUserStatus.status;
            //     }
            //     break;
            // }
            case TdApi.UpdateBasicGroup.CONSTRUCTOR:
                //System.out.println("====================================update Basic Group======================================");
                TdApi.UpdateBasicGroup updateBasicGroup = (TdApi.UpdateBasicGroup) object;
                TelegramManager.basicGroups.put(updateBasicGroup.basicGroup.id, updateBasicGroup.basicGroup);
                break;
            case TdApi.UpdateSupergroup.CONSTRUCTOR:
                //System.out.println("====================================update Super group======================================");
                TdApi.UpdateSupergroup updateSupergroup = (TdApi.UpdateSupergroup) object;
                TelegramManager.supergroups.put(updateSupergroup.supergroup.id, updateSupergroup.supergroup);
                break;
            // case TdApi.UpdateSecretChat.CONSTRUCTOR:
            //     //System.out.println("====================================update Secret group======================================");
            //     TdApi.UpdateSecretChat updateSecretChat = (TdApi.UpdateSecretChat) object;
            //     TelegramManager.secretChats.put(updateSecretChat.secretChat.id, updateSecretChat.secretChat);
            //     break;
            case TdApi.UpdateNewChat.CONSTRUCTOR: {
                //System.out.println("====================================update New Chat======================================");
                TdApi.UpdateNewChat updateNewChat = (TdApi.UpdateNewChat) object;
                TdApi.Chat chat = updateNewChat.chat;
                synchronized (chat) {
                    TelegramManager.chats.put(chat.id, chat);
                    TdApi.ChatPosition[] positions = chat.positions;
                    chat.positions = new TdApi.ChatPosition[0];
                    OrderedChat.setChatPositions(chat, positions);
                }
                break;
            }
            // case TdApi.UpdateChatTitle.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat title======================================");
            //     TdApi.UpdateChatTitle updateChat = (TdApi.UpdateChatTitle) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.title = updateChat.title;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatPhoto.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat photo======================================");
            //     TdApi.UpdateChatPhoto updateChat = (TdApi.UpdateChatPhoto) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.photo = updateChat.photo;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatLastMessage.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat last message======================================");
            //     TdApi.UpdateChatLastMessage updateChat = (TdApi.UpdateChatLastMessage) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.lastMessage = updateChat.lastMessage;
            //         OrderedChat.setChatPositions(chat, updateChat.positions);
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatPosition.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat position======================================");
            //     TdApi.UpdateChatPosition updateChat = (TdApi.UpdateChatPosition) object;
            //     if (updateChat.position.list.getConstructor() != TdApi.ChatListMain.CONSTRUCTOR) {
            //         break;
            //     }
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         int i;
            //         for (i = 0; i < chat.positions.length; i++) {
            //             if (chat.positions[i].list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
            //                 break;
            //             }
            //         }
            //         TdApi.ChatPosition[] new_positions = new TdApi.ChatPosition[chat.positions.length + (updateChat.position.order == 0 ? 0 : 1) - (i < chat.positions.length ? 1 : 0)];
            //         int pos = 0;
            //         if (updateChat.position.order != 0) {
            //             new_positions[pos++] = updateChat.position;
            //         }
            //         for (int j = 0; j < chat.positions.length; j++) {
            //             if (j != i) {
            //                 new_positions[pos++] = chat.positions[j];
            //             }
            //         }
            //         assert pos == new_positions.length;
            //         OrderedChat.setChatPositions(chat, new_positions);
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatReadInbox.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat read inbox======================================");
            //     TdApi.UpdateChatReadInbox updateChat = (TdApi.UpdateChatReadInbox) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId;
            //         chat.unreadCount = updateChat.unreadCount;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatReadOutbox.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat read outbox======================================");
            //     TdApi.UpdateChatReadOutbox updateChat = (TdApi.UpdateChatReadOutbox) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat unread mention count======================================");
            //     TdApi.UpdateChatUnreadMentionCount updateChat = (TdApi.UpdateChatUnreadMentionCount) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.unreadMentionCount = updateChat.unreadMentionCount;
            //     }
            //     break;
            // }
            // case TdApi.UpdateMessageMentionRead.CONSTRUCTOR: {
            //     //System.out.println("====================================update message mention read======================================");
            //     TdApi.UpdateMessageMentionRead updateChat = (TdApi.UpdateMessageMentionRead) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.unreadMentionCount = updateChat.unreadMentionCount;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatReplyMarkup.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat reply markup======================================");
            //     TdApi.UpdateChatReplyMarkup updateChat = (TdApi.UpdateChatReplyMarkup) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.replyMarkupMessageId = updateChat.replyMarkupMessageId;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatDraftMessage.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat draft message======================================");
            //     TdApi.UpdateChatDraftMessage updateChat = (TdApi.UpdateChatDraftMessage) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(updateChat.chatId);
            //     synchronized (chat) {
            //         chat.draftMessage = updateChat.draftMessage;
            //         OrderedChat.setChatPositions(chat, updateChat.positions);
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatPermissions.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat permissions======================================");
            //     TdApi.UpdateChatPermissions update = (TdApi.UpdateChatPermissions) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(update.chatId);
            //     synchronized (chat) {
            //         chat.permissions = update.permissions;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatNotificationSettings.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat notification settings======================================");
            //     TdApi.UpdateChatNotificationSettings update = (TdApi.UpdateChatNotificationSettings) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(update.chatId);
            //     synchronized (chat) {
            //         chat.notificationSettings = update.notificationSettings;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat default disavle notification======================================");
            //     TdApi.UpdateChatDefaultDisableNotification update = (TdApi.UpdateChatDefaultDisableNotification) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(update.chatId);
            //     synchronized (chat) {
            //         chat.defaultDisableNotification = update.defaultDisableNotification;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat is marked as unread======================================");
            //     TdApi.UpdateChatIsMarkedAsUnread update = (TdApi.UpdateChatIsMarkedAsUnread) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(update.chatId);
            //     synchronized (chat) {
            //         chat.isMarkedAsUnread = update.isMarkedAsUnread;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatIsBlocked.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat is blocked======================================");
            //     TdApi.UpdateChatIsBlocked update = (TdApi.UpdateChatIsBlocked) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(update.chatId);
            //     synchronized (chat) {
            //         chat.isBlocked = update.isBlocked;
            //     }
            //     break;
            // }
            // case TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR: {
            //     //System.out.println("====================================update Chat has scheduled messages======================================");
            //     TdApi.UpdateChatHasScheduledMessages update = (TdApi.UpdateChatHasScheduledMessages) object;
            //     TdApi.Chat chat = TelegramManager.chats.get(update.chatId);
            //     synchronized (chat) {
            //         chat.hasScheduledMessages = update.hasScheduledMessages;
            //     }
            //     break;
            // }

            // case TdApi.UpdateUserFullInfo.CONSTRUCTOR:
            //     //System.out.println("====================================update user full info======================================");
            //     TdApi.UpdateUserFullInfo updateUserFullInfo = (TdApi.UpdateUserFullInfo) object;
            //     TelegramManager.usersFullInfo.put(updateUserFullInfo.userId, updateUserFullInfo.userFullInfo);
            //     break;
            // case TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR:
            //     //System.out.println("====================================update basic group full info======================================");
            //     TdApi.UpdateBasicGroupFullInfo updateBasicGroupFullInfo = (TdApi.UpdateBasicGroupFullInfo) object;
            //     TelegramManager.basicGroupsFullInfo.put(updateBasicGroupFullInfo.basicGroupId, updateBasicGroupFullInfo.basicGroupFullInfo);
            //     break;
            // case TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR:
            //     //System.out.println("====================================update super group full info======================================");
            //     TdApi.UpdateSupergroupFullInfo updateSupergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
            //     TelegramManager.supergroupsFullInfo.put(updateSupergroupFullInfo.supergroupId, updateSupergroupFullInfo.supergroupFullInfo);
            //     break;
            default:
                    //TelegramManager.print("Unsupported update:\n" + object);
        }
    }
}
