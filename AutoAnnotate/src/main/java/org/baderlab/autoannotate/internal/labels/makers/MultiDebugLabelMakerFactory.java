package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class MultiDebugLabelMakerFactory implements LabelMakerFactory<Object> {

	public static final String ID = "multiDebug";
	
	@Inject private Provider<WordCloudAdapter> wordCloudProvider; 

	
	@Override
	public String getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return "Multi Debug";
	}

	@Override
	public Object getDefaultContext() {
		return new Object();
	}

	@Override
	public LabelMakerUI<Object> createUI(Object context) {
		return null;
	}

	@Override
	public LabelMaker createLabelMaker(Object context) {
		return new MultiDebugLabelMaker(wordCloudProvider.get());
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
