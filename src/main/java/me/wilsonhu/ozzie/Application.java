package me.wilsonhu.ozzie;

public class Application {

    public static void main(String[] args) throws Exception {
        Ozzie ozzie = new Ozzie(args);
        ozzie.start();
        //ozzie.getRConServer().execute();
        //Todo: Add back fucking quotes, it's not Ozzie without her quotes qq, Laura Ily pls love me
        //Todo: Start Documenting this shit
        //Todo: Get started on Vaadin WebApp(Maybe Laravel if I do implement what's below xdxd) so I can fully deploy this shit
        /*
        Todo: Should I use an actual DB now?
         or stick this shit, managing data via json is way easier and small amounts of updates are fine
         but I don't know how this shit will perform with a huge user base, in theory it should be fine
         due the the fact that how I am organizing the data, seek times should be very low. If not I will
         move to firebase(should be easy, haven't touched it)
         */
        //Todo: add a shutdown hook and fucking link those plugins to disable every single one of them xd
        //Todo: ScheduledExecutorService and Move to Firebase and Move to Java 11 LTS, Java 8 LTS end of life coming soon, fucking learn lambda
    }
}
