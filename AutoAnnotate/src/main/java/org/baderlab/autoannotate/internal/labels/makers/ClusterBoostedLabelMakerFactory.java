package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClusterBoostedLabelMakerFactory implements LabelMakerFactory<ClusterBoostedOptions> {

	@Inject private Provider<WordCloudAdapter> wordCloudProvider; 
	
	@Override
	public String getName() {
		return "WordCloud: Clustered";
	}

	@Override
	public ClusterBoostedOptions getDefaultContext() {
		return new ClusterBoostedOptions(4, 8);
	}

	@Override
	public LabelMakerUI<ClusterBoostedOptions> createUI(ClusterBoostedOptions context) {
		return new ClusterBoostedLabelMakerUI(context);
	}

	@Override
	public LabelMaker createLabelMaker(ClusterBoostedOptions context) {
		return new ClusterBoostedLabelMaker(wordCloudProvider.get(), context);
	}

}
