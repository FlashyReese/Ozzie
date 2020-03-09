package me.wilsonhu.ozzie.core.rcon;

import me.wilsonhu.ozzie.Ozzie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;


public class RConServer {
    //Todo: Understand this shit better with closing sockets and i/o streams; fucking abandon ship; might unabandon due to linux lol restart on service
    private static final Logger log = LogManager.getLogger(RConServer.class);

    private int port;
    private Set<UserThread> userThreads = new HashSet<>();
    private Ozzie ozzie;

    public RConServer(Ozzie ozzie, int port) {
        log.info("Building ListenerServer...");
        this.setPort(port);
        this.setOzzie(ozzie);
        log.info("ListenerServer built!");
    }


    public void execute() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                log.info(socket.getInetAddress().toString() + " connected");
                UserThread newUser = new UserThread(socket, this);
                getUserThreads().add(newUser);
                newUser.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void broadcast(String message, UserThread excludeUser) {
        for (UserThread aUser : getUserThreads()) {
            if (aUser != excludeUser) {
                aUser.sendMessage(message);
            }
        }
    }

    public int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    private Set<UserThread> getUserThreads() {
        return userThreads;
    }

    public void setUserThreads(Set<UserThread> userThreads) {
        this.userThreads = userThreads;
    }

    public Ozzie getOzzie() {
        return ozzie;
    }

    public void setOzzie(Ozzie ozzie) {
        this.ozzie = ozzie;
    }

}
