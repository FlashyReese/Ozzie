package me.wilsonhu.ozzie.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ActivityHelper {
    private static ArrayList<String> getQuotes() throws IOException {
        ArrayList<String> quotes = new ArrayList<String>();
        File file = new File("quotes.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        while ((st = br.readLine()) != null)
            quotes.add(st);
        br.close();
        return quotes;
    }

    private static int getRandomNumberInts(int min, int max){
        Random random = new Random();
        return random.ints(min,(max+1)).findFirst().getAsInt();
    }

    public static String getRandomQuote() {
        String quote = "Opzzie";
        try {
            quote = getQuotes().get(getRandomNumberInts(0, getQuotes().size()-1));
        }catch(IOException e) {
            e.printStackTrace();
        }
        return quote;
    }

}