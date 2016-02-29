package org.baderlab.autoannotate.internal.labels;

import org.baderlab.autoannotate.internal.labels.LabelMakerManager.DefaultFactory;
import org.baderlab.autoannotate.internal.labels.makers.HeuristicLabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.makers.SizeSortedLabelMakerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

public class LabelFactoryModule extends AbstractModule {

	/**
	 * Add label maker factories here.
	 */
	@Override
	protected void configure() {
		// The setBinder will inject a set of factories into LabelMakerManager
		TypeLiteral<String> stringType = new TypeLiteral<String>() {};
		TypeLiteral<LabelMakerFactory<?>> labelMakerFactoryType = new TypeLiteral<LabelMakerFactory<?>>() {};
		MapBinder<String,LabelMakerFactory<?>> labelFactoryBinder = MapBinder.newMapBinder(binder(), stringType, labelMakerFactoryType);
		
		labelFactoryBinder.addBinding("heuristic").to(HeuristicLabelMakerFactory.class);
		labelFactoryBinder.addBinding("sizeSorted").to(SizeSortedLabelMakerFactory.class);
		
		bind(String.class).annotatedWith(DefaultFactory.class).toInstance("heuristic");
	}

}
