package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;


public class Crawler <T>
{
    protected final Lock authorizationLock = new ReentrantLock();
    protected final Condition gotAuthorization = authorizationLock.newCondition();
    protected boolean haveReceivedRespond = false;
    protected long currentUserId;
    protected Map<Long, TdApi.Chat> chats;
    protected Client client;

    private List<T> collection = new ArrayList<>();
     
    public Crawler()
    {

    }

    public Crawler(Client client)
    {
        this.client = client;
    }

    public Crawler(Client client, Map<Long, TdApi.Chat> chats)
    {
        this.client = client;
        this.chats = chats;
        blockingSend(new TdApi.GetMe(), new Client.ResultHandler() {
            @Override 
            public void onResult(TdApi.Object object) {
                switch (object.getConstructor()) {
                    case TdApi.User.CONSTRUCTOR:
                        currentUserId = ((TdApi.User) object).id;
                        //System.out.println("currentUserId: " + currentUserId);
                        break;
                    case TdApi.Error.CONSTRUCTOR:
                        System.out.println("\u001B[31m" + ((TdApi.Error) object).message + "\u001B[0m");
                        break;
                    default:

                }
                haveReceivedRespond = true;
                authorizationLock.lock();
                try {
                    gotAuthorization.signal();
                } finally {
                    authorizationLock.unlock();
                }
            }
        });
    }


    public void blockingSend(TdApi.Function<?> query, Client.ResultHandler resultHandler)
    {
        haveReceivedRespond = false;
        client.send(query, resultHandler);
        authorizationLock.lock();
        try {
            while (!haveReceivedRespond) {
                try {
                    gotAuthorization.await();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } finally {
            authorizationLock.unlock();
        }
    }

    public List<T> getCollection() {
        return collection;
    }

    public void setCollection(List<T> collection) {
        this.collection = collection;
    }

    public void addCollection(T entity)
    {
        this.collection.add(entity);
    }
}
