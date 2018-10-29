package org.baderlab.autoannotate.internal.layout;

import org.baderlab.autoannotate.internal.layout.tasks.CoseLayoutAlgorithm;
import org.baderlab.autoannotate.internal.layout.tasks.GridLayoutAlgorithm;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class ClusterLayoutAlgorithmModule extends AbstractModule {

	@Override
	protected void configure() {
		// boilerplate
		TypeLiteral<ClusterLayoutAlgorithm<?>> factoryType = new TypeLiteral<ClusterLayoutAlgorithm<?>>() {};
		Multibinder<ClusterLayoutAlgorithm<?>> layoutBinder = Multibinder.newSetBinder(binder(), factoryType);
		
		// register factories as "plug-ins"
		layoutBinder.addBinding().to(CoseLayoutAlgorithm.class);
		layoutBinder.addBinding().to(GridLayoutAlgorithm.class);
		
		// one algorithm should be the default
		bind(String.class).annotatedWith(Names.named("default")).toInstance(CoseLayoutAlgorithm.ID);
	}


}
