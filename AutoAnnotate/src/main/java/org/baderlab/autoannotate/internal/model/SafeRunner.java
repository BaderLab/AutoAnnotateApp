package org.baderlab.autoannotate.internal.model;

public interface SafeRunner {

	public enum EventType {
		VIEW_CHANGE,
		SELECTION
	}
	
	void whileRunning(Runnable runnable);

}