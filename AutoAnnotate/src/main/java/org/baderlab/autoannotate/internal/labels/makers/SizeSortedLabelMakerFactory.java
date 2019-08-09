package org.baderlab.autoannotate.internal.labels.makers;

import java.util.function.Supplier;

import org.baderlab.autoannotate.internal.labels.LabelMaker;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.WordCloudAdapter;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class SizeSortedLabelMakerFactory implements LabelMakerFactory<SizeSortedOptions> {

	public static final String ID = "sizeSorted";
	
	@Inject private Provider<WordCloudAdapter> wordCloudProvider;
	@Inject private SizeSortedLabelMakerUI.Factory uiProvider;
	
	@Override
	public String getID() {
		return ID;
	}
	
	@Override
	public String getName() {
		return "WordCloud: Biggest Words";
	}

	@Override
	public SizeSortedOptions getDefaultContext() {
		return SizeSortedOptions.defaults();
	}

	@Override
	public Supplier<SizeSortedOptions> getCommandTunables() {
		return new SizeSortedOptions.Tunables();
	}
	
	@Override
	public LabelMakerUI<SizeSortedOptions> createUI(SizeSortedOptions context) {
		return uiProvider.create(context);
	}

	@Override
	public LabelMaker createLabelMaker(SizeSortedOptions context) {
		return new SizeSortedLabelMaker(wordCloudProvider.get(), context);
	}

	@Override
	public String[] getDescription() {
		return new String[] {
			"Uses WordCloud to calculate the labels.",
			"Words in the label are the most frequent words."
		};
	}

	@Override
	public String serializeContext(SizeSortedOptions context) {
		return Integer.toString(context.getMaxWords());
	}

	@Override
	public SizeSortedOptions deserializeContext(String s) {
		try {
			int maxWords = Integer.parseInt(s);
			return new SizeSortedOptions(maxWords);
		} catch (Exception e) {
			return null;
		}
	}
}
