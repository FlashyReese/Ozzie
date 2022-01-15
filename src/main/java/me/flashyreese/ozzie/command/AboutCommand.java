package me.flashyreese.ozzie.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import me.flashyreese.ozzie.api.OzzieApi;
import me.flashyreese.ozzie.api.command.guild.DiscordCommand;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandManager;
import me.flashyreese.ozzie.api.command.guild.DiscordCommandSource;
import me.flashyreese.ozzie.api.l10n.ParsableText;
import me.flashyreese.ozzie.api.l10n.TranslatableText;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

public class AboutCommand extends DiscordCommand {
    public AboutCommand() {
        super("ozzie.help.category.information", "ozzie.about.description", "ozzie.about");
    }

    @Override
    public LiteralArgumentBuilder<DiscordCommandSource> getArgumentBuilder() {
        return DiscordCommandManager.literal("about")
                .requires(this::hasPermission)
                .executes(this::about);
    }

    private int about(CommandContext<DiscordCommandSource> commandContext) {
        MessageReceivedEvent event = commandContext.getSource().getEvent();
        AtomicLong users = new AtomicLong();
        event.getJDA().getGuilds().forEach(guild -> guild.getMembers().stream().filter(member -> member.getOnlineStatus().equals(OnlineStatus.ONLINE) || member.getOnlineStatus().equals(OnlineStatus.IDLE) || member.getOnlineStatus().equals(OnlineStatus.DO_NOT_DISTURB)).forEach(member -> users.getAndIncrement()));
        String dev = "FlashyReese";
        String codev = "Anyone?";
        if (event.isFromGuild()) {
            for (Member m : event.getGuild().getMembers()) {
                if (m.getUser().getIdLong() == 141594071033577472L) {
                    dev = m.getAsMention();
                }
            }
        }
        long millis = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        EmbedBuilder embed = new EmbedBuilder();
        try {
            embed.setColor(Color.red)
                    .setAuthor(new ParsableText(new TranslatableText("ozzie.about.bot", commandContext), OzzieApi.INSTANCE.getBotName()).toString(), "https://cutt.ly/Ozzie", event.getJDA().getSelfUser().getAvatarUrl())
                    .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
                    .setDescription(new ParsableText(new TranslatableText("ozzie.about.info", commandContext), OzzieApi.INSTANCE.getBotName(), "https://cutt.ly/Ozzie").toString())
                    .addField(new TranslatableText("ozzie.about.dev", commandContext).toString(), dev, true)
                    .addField(new TranslatableText("ozzie.about.codev", commandContext).toString(), codev, true)
                    //.addBlankField(true)
                    .addField(new TranslatableText("ozzie.about.up_time", commandContext).toString(), String.format(
                            new TranslatableText("ozzie.about.up_time_display", commandContext).toString(),
                            days, hours % 24, minutes % 60
                    ), false)//Todo: Figure a way to mirage a custom datetime format per region using settings. orrrrr... using custom cron format
                    .addField(new TranslatableText("ozzie.about.version", commandContext).toString(), OzzieApi.INSTANCE.getVersion().toString(), false)
                    //.addBlankField(true)
                    .addField(new TranslatableText("ozzie.about.servers", commandContext).toString(), OzzieApi.INSTANCE.getShardManager().getGuilds().size() + "", true)
                    .addField(new TranslatableText("ozzie.about.voice_channels", commandContext).toString(), OzzieApi.INSTANCE.getShardManager().getVoiceChannels().size() + "", true)
                    .addField(new TranslatableText("ozzie.about.text_channels", commandContext).toString(), OzzieApi.INSTANCE.getShardManager().getTextChannels().size() + "", true)
                    .addField(new TranslatableText("ozzie.about.online_users", commandContext).toString(), users + "", true)
                    .addField(new TranslatableText("ozzie.about.shard", commandContext).toString(),
                            String.format("%s", OzzieApi.INSTANCE.getShardManager().getShardsTotal() == 1 ?
                                    new TranslatableText("ozzie.about.shard.single_instance", commandContext).toString() :
                                    String.format("%s/%s %s", OzzieApi.INSTANCE.getShardManager().getShardsRunning(),
                                            OzzieApi.INSTANCE.getShardManager().getShardsTotal(),
                                            OzzieApi.INSTANCE.getShardManager().getShardsQueued() == 0 ? "" :
                                                    new ParsableText(new TranslatableText("ozzie.about.shard.queued", commandContext),
                                                            Integer.toString(OzzieApi.INSTANCE.getShardManager().getShardsQueued())))), true)
                    .setFooter(new TranslatableText("ozzie.about.by_dev", commandContext).toString(), null);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        event.getChannel().sendMessageEmbeds(embed.build()).queue();
        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
