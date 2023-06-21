package hust.soict.cybersec.tm.crawling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import hust.soict.cybersec.tm.entity.BasicGroup;

public class Crawler <T>
{
    protected final Lock authorizationLock = new ReentrantLock();
    protected final Condition gotAuthorization = authorizationLock.newCondition();
    protected boolean haveReceivedRespond = false;

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

    public List<T> getCollection() {
        //System.out.println(((BasicGroup) this.collection.get(0)).getMemberIds() + "==23=424234");
        return collection;
    }

    public void setCollection(List<T> collection) {
        this.collection = collection;
    }

    public void addCollection(T entity)
    {
        //System.out.println(((BasicGroup) entity).getMemberIds() + " o trong crawler");
        this.collection.add(entity);
        //System.out.println(((BasicGroup) collection.get(collection.size() - 1)).getMemberIds() + " van o day");
    }
}
