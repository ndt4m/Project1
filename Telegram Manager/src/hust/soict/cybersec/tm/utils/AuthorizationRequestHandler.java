package hust.soict.cybersec.tm.utils;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.TelegramManager;


public class AuthorizationRequestHandler implements Client.ResultHandler 
{

    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                System.err.println("Receive an error:\n" + object);
                onAuthorizationStateUpdated(null); // repeat last action
                break;
            case TdApi.Ok.CONSTRUCTOR:
                // result is already received through UpdateAuthorizationState, nothing to do
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
            // case TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR: {
            //     String link = ((TdApi.AuthorizationStateWaitOtherDeviceConfirmation) TelegramManager.authorizationState).link;
            //     System.out.println("Please confirm this login link on another device: " + link);
            //     break;
            // }
            // case TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR: {
            //     String emailAddress = TelegramManager.promptString("Please enter email address: ");
            //     TelegramManager.client.send(new TdApi.SetAuthenticationEmailAddress(emailAddress), new AuthorizationRequestHandler());
            //     break;
            // }
            // case TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR: {
            //     String code = TelegramManager.promptString("Please enter email authentication code: ");
            //     TelegramManager.client.send(new TdApi.CheckAuthenticationEmailCode(new TdApi.EmailAddressAuthenticationCode(code)), new AuthorizationRequestHandler());
            //     break;
            // }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                String code = TelegramManager.promptString("Please enter authentication code: ");
                TelegramManager.client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
                break;
            }
            // case TdApi.AuthorizationStateWaitRegistration.CONSTRUCTOR: {
            //     String firstName = TelegramManager.promptString("Please enter your first name: ");
            //     String lastName = TelegramManager.promptString("Please enter your last name: ");
            //     TelegramManager.client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
            //     break;
            // }
            // case TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR: {
            //     String password = TelegramManager.promptString("Please enter password: ");
            //     TelegramManager.client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
            //     break;
            // }
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
                TelegramManager.print("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                TelegramManager.haveAuthorization = false;
                TelegramManager.print("Closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                TelegramManager.print("Closed");
                if (!TelegramManager.needQuit) {
                    TelegramManager.client = Client.create(new UpdateHandler(), null, null); // recreate client after previous has closed
                } else {
                    TelegramManager.canQuit = true;
                }
                break;
            default:
                System.err.println("Unsupported authorization state:\n" + TelegramManager.authorizationState);
        }
    }
}
