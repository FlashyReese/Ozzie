package me.wilsonhu.ozzie;

import me.wilsonhu.ozzie.core.params.DisablePlugins;
import me.wilsonhu.ozzie.listener.EventListener;
import me.wilsonhu.ozzie.manager.plugin.Plugin;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Ozzie {
	
	private boolean running = false;
	private int shard;
	private int shardTotal;
	private boolean shardless;
	private JDA jda;
	
	private OzzieManager ozzieManager;
	
	public Ozzie(int shard, int shardTotal, OzzieManager manager) {
		this.setShard(shard);
		this.setShardTotal(shardTotal);
		this.setShardless(false);
		this.setOzzieManager(manager);
	}
	
	public Ozzie(OzzieManager manager) {
		this.setShardless(true);
		this.setOzzieManager(manager);
	}
	
	public void start() {
		if(!isRunning()) {
			try{
				if(isShardless()) {
					setJDA(new JDABuilder(AccountType.BOT).setToken(getOzzieManager().getBotToken()).build());
				}else {
					setJDA(new JDABuilder(AccountType.BOT).setToken(getOzzieManager().getBotToken()).useSharding(shard, shardTotal).build());
				}
				getJDA().addEventListener(getOzzieManager().getEventWaiter());
				getJDA().addEventListener(new EventListener(this.getOzzieManager(), this));
				if(!((DisablePlugins) this.getOzzieManager().getParameterManager().getParam(DisablePlugins.class)).isPluginless()) {
					for(Plugin pl: this.getOzzieManager().getLoadedPluginList()) {
						this.getOzzieManager().getLogger().info(String.format("Enabling %s %s", pl.getName(), pl.getVersion()));
						pl.onEnable(this);
						getJDA().addEventListener(pl);
						this.getOzzieManager().getCommandManager().addCommands(pl);
					}
				}
				me.wilsonhu.ozzie.utilities.Activity act = new me.wilsonhu.ozzie.utilities.Activity(); 
				getJDA().getPresence().setActivity(Activity.playing(act.getRandomQuote()));
				getJDA().setAutoReconnect(true);
			}catch(Exception e){
				e.printStackTrace();
			}
			setRunning(true);
		}
	}
	
	public void stop() {
		if(isRunning()) {
			getJDA().shutdownNow();
			setRunning(false);
		}
	}
	
	protected void restart() {
		this.stop();
		this.start();
	}
	

	public JDA getJDA() {
		return jda;
	}

	public void setJDA(JDA jda) {
		this.jda = jda;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public int getShard() {
		return shard;
	}

	public void setShard(int shard) {
		this.shard = shard;
	}

	public int getShardTotal() {
		return shardTotal;
	}

	public void setShardTotal(int shardTotal) {
		this.shardTotal = shardTotal;
	}

	public boolean isShardless() {
		return shardless;
	}

	public void setShardless(boolean shardLess) {
		this.shardless = shardLess;
	}

	public OzzieManager getOzzieManager() {
		return ozzieManager;
	}

	public void setOzzieManager(OzzieManager ozzieManager) {
		this.ozzieManager = ozzieManager;
	}
}
