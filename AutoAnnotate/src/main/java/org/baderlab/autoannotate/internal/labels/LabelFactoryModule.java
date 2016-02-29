package org.baderlab.autoannotate.internal.labels;

import org.baderlab.autoannotate.internal.labels.makers.HeuristicLabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.makers.SizeSortedLabelMakerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

public class LabelFactoryModule extends AbstractModule {

	/**
	 * Add label maker factories here.
	 */
	@Override
	protected void configure() {
		// The setBinder will inject a set of factories into LabelMakerManager
		Multibinder<LabelMakerFactory<?>> labelFactoryBinder = Multibinder.newSetBinder(binder(), new TypeLiteral<LabelMakerFactory<?>>() {});
		
		labelFactoryBinder.addBinding().to(HeuristicLabelMakerFactory.class);
		labelFactoryBinder.addBinding().to(SizeSortedLabelMakerFactory.class);
		//labelFactoryBinder.addBinding().to(TestLabelMakerFactory.class);
	}

}
