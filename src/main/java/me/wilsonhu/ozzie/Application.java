package me.wilsonhu.ozzie;

public class Application {

    public static void main(String[] args) throws Exception {
        Ozzie ozzie = new Ozzie(args);
        ozzie.start();
        /*Locale locales[] = SimpleDateFormat.getAvailableLocales();
        for (Locale locale : locales) {
            if(locale.toString().isEmpty())continue;
            //System.out.printf("%10s - %s, %s \n", locale.toString(), locale.getDisplayName(), locale.getDisplayCountry());
        }*/
    }
}
