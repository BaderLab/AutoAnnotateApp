package org.baderlab.autoannotate.internal.labels;

import org.baderlab.autoannotate.internal.labels.LabelMakerManager.DefaultFactoryID;
import org.baderlab.autoannotate.internal.labels.makers.ClusterBoostedLabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.makers.MostSignificantLabelMakerFactory;
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
		// Boilerplate
		TypeLiteral<LabelMakerFactory<?>> factoryType = new TypeLiteral<LabelMakerFactory<?>>() {};
		Multibinder<LabelMakerFactory<?>> labelFactoryBinder = Multibinder.newSetBinder(binder(), factoryType);
		
		// Register factories as "plug-ins"
		labelFactoryBinder.addBinding().to(ClusterBoostedLabelMakerFactory.class);
		labelFactoryBinder.addBinding().to(SizeSortedLabelMakerFactory.class);
		labelFactoryBinder.addBinding().to(MostSignificantLabelMakerFactory.class);
//		labelFactoryBinder.addBinding().to(HeuristicLabelMakerFactory.class);
//		labelFactoryBinder.addBinding().to(MultiDebugLabelMakerFactory.class);
		
		// One factory should be the default when creating a new AnnotationSet
		bind(String.class).annotatedWith(DefaultFactoryID.class).toInstance(ClusterBoostedLabelMakerFactory.ID);
	}

}
