package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;

public class TestLabelMakerFactory implements LabelMakerFactory<TestLabelMakerOptions> {

	@Override
	public String getName() {
		return "Test";
	}

	@Override
	public TestLabelMakerOptions getDefaultContext() {
		return new TestLabelMakerOptions("default");
	}

	@Override
	public LabelMakerUI<TestLabelMakerOptions> createUI(TestLabelMakerOptions context) {
		return new TestLabelMakerUI(context);
	}

	@Override
	public LabelMaker createLabelMaker(TestLabelMakerOptions context) {
		return new TestLabelMaker(context);
	}

}
