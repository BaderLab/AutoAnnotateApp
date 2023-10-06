package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

import com.google.inject.Inject;

public class MostSignificantLabelMakerFactory implements LabelMakerFactory<MostSignificantOptions> {

	public static final String ID = "mostSignificant";
	
	@Inject private MostSignificantLabelMakerUI.Factory uiFactory;
	@Inject private MostSignificantLabelMaker.Factory labelMakerFactory;
	
	
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
		return new MostSignificantOptions();
	}

	@Override
	public LabelMakerUI<MostSignificantOptions> createUI(MostSignificantOptions context) {
		return uiFactory.create(this);
	}

	@Override
	public LabelMaker createLabelMaker(MostSignificantOptions context) {
		return labelMakerFactory.create(context);
	}

	@Override
	public String serializeContext(MostSignificantOptions context) {
		// TODO
//		return context.getSignificanceColumn() + "," + context.getSignificance().name();
		return null;
	}

	@Override
	public MostSignificantOptions deserializeContext(String s) {
		return new MostSignificantOptions();
		// TODO
//		String[] args = s.split(",");
//		String column = args[0];
//		Significance significance = Significance.valueOf(args[1]);
//		return new MostSignificantOptions(column, significance);
	}

}
