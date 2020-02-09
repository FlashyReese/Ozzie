package me.wilsonhu.ozzie.core.rcon;

import me.wilsonhu.ozzie.core.i18n.TranslatableText;
import me.wilsonhu.ozzie.schemas.UserSchema;

import java.io.*;
import java.net.Socket;
import java.util.Objects;


public class UserThread extends Thread{
    private Socket socket;
    private RConServer server;
    private PrintWriter writer;
    private boolean authenticated;
    private long userId;
    private long serverId;

    public UserThread(Socket socket, RConServer server) {
        this.socket = socket;
        this.server = server;
        this.setAuthenticated(false);
        this.setUserId(0L);
    }

    public void run() {
        try {
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = socket.getOutputStream();
            writer = new PrintWriter(output, true);
            do{
                writer.println(new TranslatableText("ozzie.provideidserver"));
                String serverID = reader.readLine();
                if(serverID.trim().equalsIgnoreCase("exit")){
                    writer.close();
                    reader.close();
                    input.close();
                    output.close();
                    socket.close();
                    return;
                }
                writer.println(new TranslatableText("ozzie.provideiduser"));
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
                authenticate(serverID, userID, password);
            }while(!isAuthenticated());
            String clientMessage;
            do {
                clientMessage = reader.readLine();
                if(clientMessage.trim().equalsIgnoreCase("exit")){
                    setAuthenticated(false);
                    break;
                }
                server.getOzzie().getCommandManager().onRConCommand(server.getOzzie().getCommandManager().getCommands(), clientMessage, writer, getUserId(), getServerId());
                server.getOzzie().getCommandManager().onRConCommand(server.getOzzie().getCommandManager().getPluginCommands(), clientMessage, writer, getUserId(), getServerId());
                //writer.println("[Test] " + clientMessage);
            } while (isAuthenticated());
            writer.close();
            reader.close();
            input.close();
            output.close();
            socket.close();
        } catch (IOException ex) {
            System.out.println("Error in UserThread: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMessage(String message) {
        writer.println(message);
    }

    private void authenticate(String serverID, String userID, String password){
        long id = Long.parseLong(userID);
        long serverid = Long.parseLong(serverID);
        UserSchema userSchema = server.getOzzie().getConfigurationManager().getUserSettings(id);
        if(userSchema.getPassword().equals(password)){
            if(Objects.requireNonNull(server.getOzzie().getShardManager().getGuildById(serverid)).isMember(Objects.requireNonNull(server.getOzzie().getShardManager().getUserById(id)))){
                setAuthenticated(true);
                setUserId(id);
                setServerId(serverid);
                String userName = Objects.requireNonNull(server.getOzzie().getShardManager().getUserById(id)).getName();
                String serverName = Objects.requireNonNull(server.getOzzie().getShardManager().getGuildById(serverID)).getName();
                writer.println(new TranslatableText("ozzie.loggedin"));
                writer.println(String.format("%s %s %s %s",new TranslatableText("ozzie.loggedinas").toString(), userName, new TranslatableText("ozzie.on").toString(), serverName));
            }else{
                setAuthenticated(false);
                writer.println(new TranslatableText("ozzie.invalidpass"));
            }
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }


    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }
}
