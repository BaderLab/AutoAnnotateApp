package org.baderlab.autoannotate.internal.labels;

public interface LabelMakerFactory<C> {

	String getID();
	
	String getName();
	
	C getDefaultContext();
	
	LabelMakerUI<C> createUI(C context);
	
	LabelMaker createLabelMaker(C context);
	
	
	String serializeContext(C context);
	
	C deserializeContext(String s);
	
	
	default String[] getDescription() {
		return new String[0];
	}
	
	
}
