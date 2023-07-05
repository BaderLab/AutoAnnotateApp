package org.baderlab.autoannotate.internal.labels.makers;

import java.util.function.Supplier;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClusterBoostedLabelMakerFactory implements LabelMakerFactory<ClusterBoostedOptions> {

	public static final String ID = "clusterBoosted";
	
	@Inject private Provider<WordCloudAdapter> wordCloudProvider; 
	@Inject private ClusterBoostedLabelMakerUI.Factory uiFactory;
	
	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getName() {
		return "WordCloud: Adjacent Words (default)";
	}

	@Override
	public ClusterBoostedOptions getDefaultContext() {
		return ClusterBoostedOptions.defaults();
	}
	
	@Override
	public Supplier<ClusterBoostedOptions> getCommandTunables() {
		return new ClusterBoostedOptions.Tunables();
	}

	@Override
	public LabelMakerUI<ClusterBoostedOptions> createUI(ClusterBoostedOptions context) {
		return uiFactory.create(context, this);
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

	@Override
	public String serializeContext(ClusterBoostedOptions context) {
		return context.getMaxWords() + "," + context.getClusterBonus() + "," + context.getMinimumWordOccurrences();
	}

	@Override
	public ClusterBoostedOptions deserializeContext(String s) {
		String[] args = s.split(",");
		
		if(args.length == 2) {
			try {
				int maxWords = Integer.parseInt(args[0]);
				int boost = Integer.parseInt(args[1]);
				return new ClusterBoostedOptions(maxWords, boost, ClusterBoostedOptions.DEFAULT_MIN_OCCURS);
			} catch (Exception e) { }
		}
		else if(args.length == 3) {
			try {
				int maxWords = Integer.parseInt(args[0]);
				int boost = Integer.parseInt(args[1]);
				int minOccurs = Integer.parseInt(args[2]);
				return new ClusterBoostedOptions(maxWords, boost, minOccurs);
			} catch (Exception e) { }
		}
		
		return null;
	}

}
