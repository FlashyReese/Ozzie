package me.wilsonhu.ozzie.core.params;

import me.wilsonhu.ozzie.OzzieManager;
import me.wilsonhu.ozzie.manager.parameter.Param;

public class ShardSize extends Param{

	private boolean shardless = true;
	private int shardTotal;
	
	public ShardSize() {
		super("shards", "shards <total>");
	}

	@Override
	public void onCommand(String full, String[] split, OzzieManager yucenia) throws Exception {
		setShardless(false);
		this.setShardTotal(Integer.parseInt(split[1]));
		yucenia.getLogger().info("Shard Size " + this.getShardTotal());
	}

	public boolean isShardless() {
		return shardless;
	}

	public void setShardless(boolean shardless) {
		this.shardless = shardless;
	}

	public int getShardTotal() {
		return shardTotal;
	}

	public void setShardTotal(int shardTotal) {
		this.shardTotal = shardTotal;
	}
}
