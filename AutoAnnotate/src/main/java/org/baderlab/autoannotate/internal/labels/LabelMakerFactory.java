package org.baderlab.autoannotate.internal.labels;

import java.util.function.Supplier;

public interface LabelMakerFactory<C> {

	String getID();
	
	String getName();
	
	C getDefaultContext();
	
	LabelMakerUI<C> createUI(C context);
	
	LabelMaker createLabelMaker(C context);
	
	
	String serializeContext(C context);
	
	C deserializeContext(String s);
	
	
	default boolean requiresWordCloud() {
		return false;
	}

	
	default Supplier<C> getCommandTunables() {
		return null;
	}
	
	default String[] getDescription() {
		return new String[0];
	}
	
}
