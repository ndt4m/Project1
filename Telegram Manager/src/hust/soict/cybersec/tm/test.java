package hust.soict.cybersec.tm;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import hust.soict.cybersec.tm.utils.FlatMapUtil;

import com.google.common.reflect.TypeToken;

public class Test {
    public static void main(String[] args) 
    {

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        java.lang.reflect.Type type = new TypeToken<Map<String, Object>>(){}.getType();
        try {
            FileReader fr1 = new FileReader("C:\\Users\\HP\\Desktop\\Project1\\Telegram Manager\\test1.json");
            FileReader fr2 = new FileReader("C:\\Users\\HP\\Desktop\\Project1\\Telegram Manager\\test2.json");
            Map<String, Object> leftMap = gson.fromJson(fr1, type);
            Map<String, Object> rightMap = gson.fromJson(fr2, type);
            Map<String, Object> leftFlatMap = FlatMapUtil.flatten(leftMap);
            Map<String, Object> rightFlatMap = FlatMapUtil.flatten(rightMap);
            MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);
            
            System.out.println("Entries only on left\n--------------------------");
            System.out.println(difference.entriesOnlyOnLeft().size());
            
            System.out.println("\n\nEntries only on right\n--------------------------");
            System.out.println(difference.entriesOnlyOnRight().size());
            
            System.out.println("\n\nEntries differing\n--------------------------");
            System.out.println(difference.entriesDiffering().size());
            
            System.out.println("\n\nEntries in common\n--------------------------");
            difference.entriesInCommon().forEach((key, value) -> System.out.println(key + ": " + value));
            fr1.close();
            fr2.close();
        } catch (IOException e) {
            System.err.println("[-] Error in reading the json file.");
           
        }
    }
}

