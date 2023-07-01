// package hust.soict.cybersec.tm.utils;
// import org.drinkless.tdlib.TdApi;

// import hust.soict.cybersec.tm.TelegramManager;

// public class OrderedChat implements Comparable<OrderedChat> 
// {
//     private final long chatId;
//     private final TdApi.ChatPosition position;
    
//     public OrderedChat (long chatId, TdApi.ChatPosition position) 
//     {
//         this.chatId = chatId;
//         this.position = position;
//     }

//     public long getChatId() {
//         return chatId;
//     }

//     @Override
//     public int compareTo(OrderedChat o) 
//     {
//         if (this.position.order != o.position.order) 
//         {
//             return o.position.order < this.position.order ? -1 : 1;
//         }
//         if (this.chatId != o.chatId) 
//         {
//             return o.chatId < this.chatId ? -1 : 1;
//         }
//         return 0;
//     }

//     @Override
//     public boolean equals(Object obj) 
//     {
//         OrderedChat o = (OrderedChat) obj;
//         return this.chatId == o.chatId && this.position.order == o.position.order;
//     }

//     public static void setChatPositions(TdApi.Chat chat, TdApi.ChatPosition[] positions) {
//         synchronized (TelegramManager.mainChatList) {
//             synchronized (chat) {
//                 for (TdApi.ChatPosition position : chat.positions) {
//                     if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
//                         boolean isRemoved = TelegramManager.mainChatList.remove(new OrderedChat(chat.id, position));
//                         assert isRemoved;
//                     }
//                 }

//                 chat.positions = positions;

//                 for (TdApi.ChatPosition position : chat.positions) {
//                     if (position.list.getConstructor() == TdApi.ChatListMain.CONSTRUCTOR) {
//                         boolean isAdded = TelegramManager.mainChatList.add(new OrderedChat(chat.id, position));
//                         assert isAdded;
//                     }
//                 }
//             }
//         }
//     }
// }