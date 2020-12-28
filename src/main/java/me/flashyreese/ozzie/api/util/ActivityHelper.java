/*
 * Copyright (C) 2019-2020 Yao Chung Hu / FlashyReese
 *
 * Ozzie is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Ozzie is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ozzie.  If not, see http://www.gnu.org/licenses/
 *
 */
package me.flashyreese.ozzie.api.util;

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

    private static int getRandomNumberInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, (max + 1)).findFirst().getAsInt();
    }

    public static String getRandomQuote() {
        String quote = "Opzzie";
        try {
            quote = getQuotes().get(getRandomNumberInts(0, getQuotes().size() - 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return quote;
    }

}