package me.flashyreese.ozzie;

import me.flashyreese.ozzie.api.OzzieApi;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URISyntaxException;

public class Application {
    public static void main(String[] args) throws IOException, LoginException {
        OzzieApi ozzieApi = new OzzieApi(args);
        ozzieApi.start();
        Runtime.getRuntime().addShutdownHook(new Thread(ozzieApi::stop));
    }
}
