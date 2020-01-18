package me.wilsonhu.ozzie.core.socket;

import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.UserSchema;

import java.io.*;
import java.net.Socket;


public class UserThread extends Thread{
    private Socket socket;
    private ListenerServer server;
    private PrintWriter writer;
    private boolean authenticated;

    public UserThread(Socket socket, ListenerServer server) {
        this.socket = socket;
        this.server = server;
        this.setAuthenticated(false);
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            do{
                writer.println(new TranslatableText("ozzie.provideid"));
                String userID = reader.readLine();
                if(userID.trim().equalsIgnoreCase("exit")){
                    writer.close();
                    reader.close();
                    input.close();
                    output.close();
                    socket.close();
                    return;
                }
                writer.println(new TranslatableText("ozzie.providepass"));
                String password = reader.readLine();
                if(password.trim().equalsIgnoreCase("exit")){
                    writer.close();
                    reader.close();
                    input.close();
                    output.close();
                    socket.close();
                    return;
                }
                authenticate(userID, password);
            }while(!isAuthenticated());
            String clientMessage;
            do {
                clientMessage = reader.readLine();
                if(clientMessage.trim().equalsIgnoreCase("exit")){
                    setAuthenticated(false);
                    break;
                }
                writer.println("[Test] " + clientMessage);
            } while (isAuthenticated());
            writer.close();
            reader.close();
            input.close();
            output.close();
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    void sendMessage(String message) {
        writer.println(message);
    }

    private void authenticate(String userID, String password){
        long id = Long.parseLong(userID);
        UserSchema userSchema = server.getOzzie().getConfigurationManager().getUserSettings(id);
        if(userSchema.getPassword().equals(password)){
            setAuthenticated(true);
            writer.println(new TranslatableText("ozzie.loggedin"));
        }else{
            setAuthenticated(false);
            writer.println(new TranslatableText("ozzie.invalidpass"));
        }
    }


    private boolean isAuthenticated() {
        return authenticated;
    }

    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
