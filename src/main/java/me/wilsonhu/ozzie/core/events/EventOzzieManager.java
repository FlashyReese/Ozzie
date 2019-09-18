package me.wilsonhu.ozzie.core.events;

import me.wilsonhu.ozzie.OzzieManager;
import com.darkmagician6.eventapi.events.Event;

public class EventOzzieManager implements Event{
	OzzieManager manager;
	
	public EventOzzieManager(OzzieManager manager) {
		this.manager = manager;
	}
	
	public OzzieManager getOzzieManager() {
		return manager;
	}
}
