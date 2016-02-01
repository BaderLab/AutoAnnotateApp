package org.baderlab.autoannotate;

import java.util.LinkedList;

import com.google.common.eventbus.Subscribe;

public class EventBusTracker {
	
	private LinkedList<Object> events = new LinkedList<>();
	
	@Subscribe
	public void handleAnyEvent(Object event) {
		events.add(event);
	}
	
	public int size() {
		return events.size();
	}
	
	public boolean isEmpty() {
		return events.isEmpty();
	}
	
	public Object popFirst() {
		return events.removeFirst();
	}
	
	public void clear() {
		events.clear();
	}

}
