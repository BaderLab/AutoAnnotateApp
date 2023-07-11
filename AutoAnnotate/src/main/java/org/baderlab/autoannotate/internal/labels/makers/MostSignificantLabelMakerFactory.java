package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

import com.google.inject.Inject;

public class MostSignificantLabelMakerFactory implements LabelMakerFactory<MostSignificantOptions> {

	public static final String ID = "mostSignificant";
	
	@Inject private MostSignificantLabelMakerUI.Factory uiFactory;
	
	
	@Override
	public String getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return "Name of most significant node in cluster";
	}
	
	@Override
	public MostSignificantOptions getDefaultContext() {
		return new MostSignificantOptions(null);
	}

	@Override
	public LabelMakerUI<MostSignificantOptions> createUI(MostSignificantOptions context) {
		return uiFactory.create(this);
	}

	@Override
	public LabelMaker createLabelMaker(MostSignificantOptions context) {
		return new MostSignificantLabelMaker(context);
	}

	@Override
	public String serializeContext(MostSignificantOptions context) {
		return context.getSignificanceColumn();
	}

	@Override
	public MostSignificantOptions deserializeContext(String s) {
		return new MostSignificantOptions(s);
	}

}
