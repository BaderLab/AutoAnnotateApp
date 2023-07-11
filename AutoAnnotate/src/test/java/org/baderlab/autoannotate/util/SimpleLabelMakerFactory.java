package org.baderlab.autoannotate.util;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

public class SimpleLabelMakerFactory implements LabelMakerFactory<Void> {

	public static final String ID = "SimpleLabelMakerFactory";
	
	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return getID();
	}

	@Override
	public Void getDefaultContext() {
		return null;
	}

	@Override
	public LabelMakerUI<Void> createUI(Void context) {
		return null;
	}

	@Override
	public LabelMaker createLabelMaker(Void context) {
		return new SimpleLabelMaker();
	}

	@Override
	public String serializeContext(Void context) {
		return null;
	}

	@Override
	public Void deserializeContext(String s) {
		return null;
	}

	@Override
	public boolean requiresWordCloud() {
		return false;
	}

}
