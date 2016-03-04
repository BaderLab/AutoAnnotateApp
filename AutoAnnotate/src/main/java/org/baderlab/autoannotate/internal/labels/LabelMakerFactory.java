package org.baderlab.autoannotate.internal.labels;

public interface LabelMakerFactory<C> {

	String getName();
	
	C getDefaultContext();
	
	LabelMakerUI<C> createUI(C context);
	
	LabelMaker createLabelMaker(C context);
	
	
	default String[] getDescription() {
		return new String[0];
	}
}
