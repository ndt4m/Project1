package hust.soict.cybersec.tm;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;


public class MainChatListHandler implements Client.ResultHandler
{
    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                if (((TdApi.Error) object).code == 404) {
                    synchronized (TelegramManager.mainChatList) {
                        TelegramManager.haveFullMainChatList = true;
                        TelegramManager.authorizationLock.lock();
                        try {
                            TelegramManager.gotAuthorization.signal();
                        } finally {
                            TelegramManager.authorizationLock.unlock();
                        }
                    }
                } else {
                    System.err.println("Receive an error for LoadChats:\n" + object);
                }
                break;
            case TdApi.Ok.CONSTRUCTOR:
                // chats had already been received through updates, let's retry request
                TelegramManager.getMainChatList();
                break;
            default:
                System.err.println("Receive wrong response from TDLib:\n" + object);
        }
    }
}
