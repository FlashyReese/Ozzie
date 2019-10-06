package me.wilsonhu.ozzie.core.commands;

import java.io.File;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.manager.command.Command;
import me.wilsonhu.ozzie.manager.command.CommandCategory;
import me.wilsonhu.ozzie.manager.command.CommandLevel;
import me.wilsonhu.ozzie.manager.shard.OzzieShard;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ManagePlugins extends Command{
	
	public ManagePlugins() {
		super(new String[]{"manageplugins"}, "", "%s");
		this.setCategory(CommandCategory.DEVELOPER);
		this.setLevel(CommandLevel.DEVELOPER);
	}

	@Override
	public void onCommand(String full, String argss, MessageReceivedEvent event, Ozzie ozzie){
		String line = "";
		File dir =	new File(ozzie.getOzzieManager().getDirectory().getAbsolutePath() + File.separator + ozzie.getOzzieManager().getPluginLoader().getDirectory());
		final File[] files = dir.listFiles();
		for(File f : files) {
			int ssize = f.getName().length() + humanReadableByteCount(f.length(), true).length();
			String spacer = "";
			for(int i = 0; i<=(78-ssize); i++) {
				spacer = spacer + " ";
			}
			line = line + f.getName() + spacer + this.humanReadableByteCount(f.length(), true) + "\n";
		}
		event.getChannel().sendMessage("```md\n" + line + "```").queue();
		//Shard Info v: check if running shardless first
		if(!ozzie.isShardless()) {
			for(OzzieShard shard: ozzie.getOzzieManager().getShardManager().getShardList()) {
				System.out.println(shard.getOzzie().getJDA().getShardInfo());
			}
		}else {
			System.out.println(ozzie.getJDA().getShardInfo());
		}
		
		
		//ozzie.getOzzieManager().getJsonManager().writeJson("test", "test", ozzie.getOzzieManager().getServerSettingsManager().getServerSettingsList());
		//HashMap<Long, ServerSettings> test = ozzie.getOzzieManager().getJsonManager().readJson("test", "test", new TypeToken<HashMap<Long, ServerSettings>>(){}.getType());
		//System.out.println(test.get("151831331796549632").getCustomBotPrefix());
		//new OshiSystemInfo().main(new String[] {});
	}

	public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
