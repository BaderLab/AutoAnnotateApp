package org.baderlab.autoannotate.internal.model;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.SafeRunner.EventType;

/**
 * Used to ignore certain categories of Cytoscape events while a block of code is running.
 */
class SafeRunnerImpl {

	// Counters allow re-entrancy
	private final Map<EventType,Integer> counters = new EnumMap<>(EventType.class);
	
	private void ignoreEventsWhile(List<EventType> eventTypes, Runnable runnable) {
		synchronized(counters) {
			for(EventType eventType : eventTypes) {
				counters.compute(eventType, (et,x) -> x == null ? 1 : x + 1);
			}
		}
		try {
			runnable.run();
		} finally {
			synchronized(counters) {
				for(EventType eventType : eventTypes) {
					counters.compute(eventType, (et, x) -> x - 1);
				}
			}
		}
	}
	
	class SafeRunnerIgnore implements SafeRunner {
		private final List<EventType> eventTypes;
		
		SafeRunnerIgnore(EventType[] eventTypes) {
			this.eventTypes = Arrays.asList(eventTypes);
		}

		@Override
		public void whileRunning(Runnable runnable) {
			ignoreEventsWhile(eventTypes, runnable);
		}
	}
	
	
	boolean shouldIgnore(EventType eventType) {
		synchronized(counters) {
			return counters.getOrDefault(eventType, 0) >= 0;
		}
	}
	
	boolean shouldIgnore(EventType ... eventTypes) {
		synchronized(counters) {
			return Arrays.stream(eventTypes).anyMatch(this::shouldIgnore);
		}
	}
	
}
