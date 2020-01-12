package me.wilsonhu.ozzie;

import java.util.Locale;

public class Application {

    public static void main(String[] args) throws Exception {
        Ozzie ozzie = new Ozzie(args);
        ozzie.start();
        for (Locale locale : ozzie.getI18nManager().getAvailableLocales()) {
            //System.out.printf("%10s - %s, %s \n", locale.toString(), locale.getDisplayName(), locale.getDisplayCountry());
        }
        //System.out.println(Locale.getDefault().toString());//Use this v:
    }
}
