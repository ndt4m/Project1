package org.example.Entity;

import com.google.gson.Gson;

public class MainSimple {
    public static void main(String[] args) {
        User user1 = new User("Hoang Anh", 20);
        Fields fields = new Fields("fileds", user1);
        Gson gson = new Gson();
        String myJson = gson.toJson(fields);
        System.out.println(myJson);
    }
}
