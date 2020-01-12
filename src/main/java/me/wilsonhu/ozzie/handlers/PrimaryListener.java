package me.wilsonhu.ozzie.handlers;

import me.wilsonhu.ozzie.Ozzie;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrimaryListener extends ListenerAdapter {

    private static final Logger log = LogManager.getLogger(PrimaryListener.class);
    private Ozzie ozzie;

    public PrimaryListener(Ozzie ozzie){
        this.ozzie = ozzie;
    }

    @Override
    public void onReady(ReadyEvent event){
        log.info("Ready to go!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        getOzzie().getCommandManager().onCommand(event, null);
    }

    public Ozzie getOzzie(){
        return ozzie;
    }
}
