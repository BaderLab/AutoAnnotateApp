package org.baderlab.autoannotate.internal.task;

@SuppressWarnings("serial")
public class RunClusterMakerException extends RuntimeException {
	
	public RunClusterMakerException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		return "Error running clustering task.";
	}

}
