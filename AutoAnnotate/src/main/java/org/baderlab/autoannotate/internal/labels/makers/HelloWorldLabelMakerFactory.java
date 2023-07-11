package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

public class HelloWorldLabelMakerFactory implements LabelMakerFactory<Object> {

	public static final String ID = "helloWorld";
	
	
	@Override
	public String getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return "Hello World!";
	}
	
	@Override
	public boolean requiresWordCloud() {
		return false;
	}

	@Override
	public Object getDefaultContext() {
		return new Object();
	}

	@Override
	public LabelMakerUI<Object> createUI(Object context) {
		return new HelloWorldLabelMakerUI(this);
	}

	@Override
	public LabelMaker createLabelMaker(Object context) {
		return new HelloWorldLabelMaker();
	}

	@Override
	public String serializeContext(Object context) {
		return null;
	}

	@Override
	public Object deserializeContext(String s) {
		return null;
	}

}
