package me.wilsonhu.ozzie.core.events;

import com.darkmagician6.eventapi.events.Event;

import me.wilsonhu.ozzie.Ozzie;

public class EventOzzie implements Event{
	Ozzie ozzie;
	
	public EventOzzie(Ozzie ozzie) {
		this.ozzie = ozzie;
	}
	
	public Ozzie getOzzie() {
		return ozzie;
	}
}
