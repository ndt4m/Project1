package hust.soict.cybersec.tm;
import java.io.IOException;
import java.io.PrintStream;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class DefaultHandler implements Client.ResultHandler 
{
        @Override
        public void onResult(TdApi.Object object)  {
            try {
                TelegramManager.print(object.toString());
                PrintStream output = new PrintStream("output.txt");
                output.println(object.toString());
                output.close();
            }catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }
