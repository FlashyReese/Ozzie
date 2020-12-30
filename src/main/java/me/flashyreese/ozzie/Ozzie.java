package me.flashyreese.ozzie;

import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.plugin.Plugin;
import me.flashyreese.ozzie.api.util.Identifier;
import me.flashyreese.ozzie.command.*;

public class Ozzie implements Plugin {

    @Override
    public void initializePlugin() {
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "about"), new AboutCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "channel"), new ChannelCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "clara"), new ClaraCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "help"), new HelpCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "language"), new LanguageCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "permission"), new PermissionCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "ping"), new PingCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "plugin"), new PluginCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "prefix"), new PrefixCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "restart"), new RestartCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "stop"), new StopCommand());
        OzzieApi.INSTANCE.getCommandManager().registerCommand(new Identifier("ozzie", "token"), new TokenCommand());
    }

    @Override
    public void terminatePlugin() {
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(AboutCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(ChannelCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(ClaraCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(HelpCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(LanguageCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PermissionCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PingCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PluginCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(PrefixCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(RestartCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(StopCommand.class);
        OzzieApi.INSTANCE.getCommandManager().unregisterCommand(TokenCommand.class);
    }
}
