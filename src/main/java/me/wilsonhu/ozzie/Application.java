package me.wilsonhu.ozzie;

public class Application {

    public static void main(String[] args) throws Exception {
        Ozzie ozzie = new Ozzie(args);
        ozzie.start();
        //ozzie.getRConServer().execute();
    }
}
