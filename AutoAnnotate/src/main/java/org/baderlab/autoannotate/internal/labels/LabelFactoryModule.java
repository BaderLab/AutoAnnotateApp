package org.baderlab.autoannotate.internal.labels;

import org.baderlab.autoannotate.internal.labels.LabelMakerManager.DefaultFactory;
import org.baderlab.autoannotate.internal.labels.makers.ClusterBoostedLabelMakerFactory;
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
		// The MapBinder will inject a set of factories into LabelMakerManager
		
		// Boilerplate
		TypeLiteral<String> stringType = new TypeLiteral<String>() {};
		TypeLiteral<LabelMakerFactory<?>> labelMakerFactoryType = new TypeLiteral<LabelMakerFactory<?>>() {};
		MapBinder<String,LabelMakerFactory<?>> labelFactoryBinder = MapBinder.newMapBinder(binder(), stringType, labelMakerFactoryType);
		
		// Register factories as "plug-ins"
		labelFactoryBinder.addBinding("clusterBoosted").to(ClusterBoostedLabelMakerFactory.class);
		labelFactoryBinder.addBinding("sizeSorted").to(SizeSortedLabelMakerFactory.class);
//		labelFactoryBinder.addBinding("heuristic").to(HeuristicLabelMakerFactory.class);
//		labelFactoryBinder.addBinding("test").to(MultiDebugLabelMakerFactory.class);
		
		// One factory should be the default when creating a new AnnotationSet
		bind(String.class).annotatedWith(DefaultFactory.class).toInstance("clusterBoosted");
	}

}
