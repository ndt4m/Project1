package hust.soict.cybersec.tm.crawling;

import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class Crawler 
{
    protected final Lock authorizationLock = new ReentrantLock();
    protected final Condition gotAuthorization = authorizationLock.newCondition();
    protected boolean haveReceivedRespond = false;

    protected Map<Long, TdApi.Chat> chats;
    protected Client client;
     
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
    }


    public void blockingSend(TdApi.Function query, Client.ResultHandler resultHandler) throws InterruptedException
    {
        haveReceivedRespond = false;
        client.send(query, resultHandler);
        authorizationLock.lock();
        try {
            while (!haveReceivedRespond) {
                gotAuthorization.await();
            }
        } finally {
            authorizationLock.unlock();
        }
        //System.out.println("admin hererere");
    }
}
