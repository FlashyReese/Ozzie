package me.flashyreese.ozzie;

import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.plugin.OzziePlugin;
import me.flashyreese.ozzie.api.util.Identifier;
import me.flashyreese.ozzie.command.*;

public class Ozzie implements OzziePlugin {
    @Override
    public void initializePlugin() {
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "about"), new AboutCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "channel"), new ChannelCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "clara"), new ClaraCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "help"), new HelpCommand("", "", ""));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "permission"), new PermissionCommand("", "", ""));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "ping"), new PingCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "plugin"), new PluginCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "restart"), new RestartCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "stop"), new StopCommand("", "ber", "bla"));
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "token"), new TokenCommand("", "ber", "bla"));
    }

    @Override
    public void terminatePlugin() {
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(AboutCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(ChannelCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(ClaraCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(HelpCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PermissionCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PingCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PluginCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(RestartCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(StopCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(TokenCommand.class);
    }
}
