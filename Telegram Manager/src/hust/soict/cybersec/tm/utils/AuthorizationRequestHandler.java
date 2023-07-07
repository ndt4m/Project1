package hust.soict.cybersec.tm.utils;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.TelegramManager;


public class AuthorizationRequestHandler implements Client.ResultHandler 
{
    private static final Properties TELEGRAM = new Properties();
    private static final int apiId;
    private static final String apiHash;
    
    static {
        try {
            TELEGRAM.load(new FileInputStream("telegram.properties"));
        } catch (IOException e) {
            
        }
        apiId = Integer.parseInt(TELEGRAM.getProperty("apiId"));
        apiHash = TELEGRAM.getProperty("apiHash");
    }

    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                System.err.println("Receive an error:\n" + object);
                onAuthorizationStateUpdated(null); 
                break;
            case TdApi.Ok.CONSTRUCTOR:
                
                break;
            default:
                System.err.println("Receive wrong response from TDLib:\n" + object);
        }
    }

    protected static void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        if (authorizationState != null) {
            TelegramManager.authorizationState = authorizationState;
        }
        switch (TelegramManager.authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
                request.databaseDirectory = "tdlib";
                request.useMessageDatabase = true;
                request.useSecretChats = true;
                request.apiId = 25245925;
                request.apiHash = "8e7c340bb15ab94ae3c8dc78fc44b91f";
                request.systemLanguageCode = "en";
                request.deviceModel = "Desktop";
                request.applicationVersion = "1.0";
                request.enableStorageOptimizer = true;

                TelegramManager.client.send(request, new AuthorizationRequestHandler());
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                String phoneNumber = TelegramManager.promptString("Please enter phone number: ");
                TelegramManager.client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
                break;
            }
            
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                String code = TelegramManager.promptString("Please enter authentication code: ");
                TelegramManager.client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                TelegramManager.haveAuthorization = true;
                TelegramManager.authorizationLock.lock();
                try {
                    TelegramManager.gotAuthorization.signal();
                } finally {
                    TelegramManager.authorizationLock.unlock();
                }
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                TelegramManager.haveAuthorization = false;
                
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                TelegramManager.haveAuthorization = false;
                
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                
                if (!TelegramManager.needQuit) {
                    TelegramManager.client = Client.create(new UpdateHandler(), null, null); 
                } else {
                    TelegramManager.canQuit = true;
                }
                break;
            default:
                System.err.println("Unsupported authorization state:\n" + TelegramManager.authorizationState);
        }
    }
}
