package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class HeuristicLabelMakerFactory implements LabelMakerFactory<HeuristicLabelOptions> {

	public static final String ID = "heuristic";
	
	@Inject private Provider<WordCloudAdapter> wordCloudProvider; 
	
	
	@Override
	public String getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return "WordCloud: Heuristic";
	}

	@Override
	public HeuristicLabelOptions getDefaultContext() {
		return HeuristicLabelOptions.defaults();
	}

	@Override
	public LabelMakerUI<HeuristicLabelOptions> createUI(HeuristicLabelOptions context) {
		return new HeuristicLabelMakerUI(context);
	}

	@Override
	public LabelMaker createLabelMaker(HeuristicLabelOptions context) {
		return new HeuristicLabelMaker(wordCloudProvider.get(), context); // mktodo, get from context
	}

	@Override
	public String serializeContext(HeuristicLabelOptions context) {
		return null;
	}

	@Override
	public HeuristicLabelOptions deserializeContext(String s) {
		return null;
	}

}
