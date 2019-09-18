package me.wilsonhu.ozzie.manager.shard;

import me.wilsonhu.ozzie.Ozzie;
import me.wilsonhu.ozzie.OzzieManager;

public class OzzieShard implements Runnable{
	public Thread t;
	private String threadName;
	private int shard;

	private int shardTotal;
	private Ozzie ozzie;
	private OzzieManager manager;
	
	public OzzieShard(OzzieManager manager, int shard, int shardTotal) {
		this.manager = manager;
		this.shard = shard;
		this.shardTotal = shardTotal;
		this.threadName = String.format("[%s/%s]", shard, shardTotal);
		System.out.println("Creating Shard " +  threadName);
	}
	
	@Override
	public void run() {
		ozzie = new Ozzie(shard, shardTotal, manager);
		ozzie.start();
	}
	
	
	public void start () {
		System.out.println("Starting Shard " +  threadName);
		if (t == null) {
			t = new Thread (this, threadName);
			t.start ();
		}
	}

	public Ozzie getOzzie() {
		return ozzie;
	}
	

	public int getShard() {
		return shard;
	}

	public int getShardTotal() {
		return shardTotal;
	}
}
