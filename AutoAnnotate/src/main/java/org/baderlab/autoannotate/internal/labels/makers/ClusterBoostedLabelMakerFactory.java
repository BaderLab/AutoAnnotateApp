package org.baderlab.autoannotate.internal.labels.makers;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClusterBoostedLabelMakerFactory implements LabelMakerFactory<ClusterBoostedOptions> {

	@Inject private Provider<WordCloudAdapter> wordCloudProvider; 
	@Inject private Provider<IconManager> iconManagerProvider;
	
	@Override
	public String getName() {
		return "WordCloud: Adjacent Words (default)";
	}

	@Override
	public ClusterBoostedOptions getDefaultContext() {
		return new ClusterBoostedOptions(4, 8);
	}

	@Override
	public LabelMakerUI<ClusterBoostedOptions> createUI(ClusterBoostedOptions context) {
		return new ClusterBoostedLabelMakerUI(context, iconManagerProvider.get());
	}

	@Override
	public LabelMaker createLabelMaker(ClusterBoostedOptions context) {
		return new ClusterBoostedLabelMaker(wordCloudProvider.get(), context);
	}

	@Override
	public String[] getDescription() {
		return new String[] {
			"Uses WordCloud to calculate the labels.",
			"Words in the label are the most frequent words and their adjacent words.",
			"The higher the \"adjacent word bonus\" is, the more likely adjacent words will be in the label."
		};
	}

}
