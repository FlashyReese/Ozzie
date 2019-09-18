package me.wilsonhu.ozzie.manager.shard;

import java.util.ArrayList;

import me.wilsonhu.ozzie.OzzieManager;

public class OzzieShardManager {
	
	private OzzieManager ozzieManager;
	private int shardTotal;
	private ArrayList<OzzieShard> shardList = new ArrayList<OzzieShard>();
	
	public OzzieShardManager(OzzieManager ozzieManager, int shardTotal) {
		this.setShardTotal(shardTotal);
		this.setOzzieManager(ozzieManager);
		for(int i = 0; i<shardTotal; i++) {
			this.getShardList().add(new OzzieShard(getOzzieManager(), i, shardTotal));
		}
	}
	
	public OzzieShard getShard(int number) {
		for(OzzieShard shard: this.getShardList()) {
			if(shard.getShard() == number) {
				return shard;
			}
		}
		return null;
	}
	
	public void stopAllShards() {
		for(OzzieShard shard: this.getShardList()) {
			if(shard.t.isAlive()) {
				if(shard.getOzzie().isRunning()) {
					shard.getOzzie().stop();
				}
			}
		}
	}
	public void startAllShards() {
		for(OzzieShard shard: this.getShardList()) {
			shard.start();
		}
	}
	
	public int getShardTotal() {
		return shardTotal;
	}

	public void setShardTotal(int shardTotal) {
		this.shardTotal = shardTotal;
	}

	public ArrayList<OzzieShard> getShardList() {
		return shardList;
	}

	public OzzieManager getOzzieManager() {
		return ozzieManager;
	}
	public void setOzzieManager(OzzieManager ozzieManager) {
		this.ozzieManager = ozzieManager;
	}
}
