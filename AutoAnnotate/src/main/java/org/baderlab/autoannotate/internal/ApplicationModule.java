package org.baderlab.autoannotate.internal;

import java.util.Properties;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.PanelManagerImpl;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

/**
 * Guice module, binds AutoAnnotate managers and event bus.
 * 
 * @author mkucera
 *
 */
public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(PanelManager.class).to(PanelManagerImpl.class).asEagerSingleton();
		bind(ModelManager.class).asEagerSingleton();
		bind(AnnotationRenderer.class).asEagerSingleton();
		
		// Create a single EventBus
		bind(EventBus.class).toInstance(new EventBus((e,c) -> e.printStackTrace()));
		
		// Set up CyProperty
		bind(new TypeLiteral<CyProperty<Properties>>(){}).toInstance(new PropsReader(BuildProperties.APP_ID, "autoannotate.props"));
	}
}

class PropsReader extends AbstractConfigDirPropsReader {
	public PropsReader(String name, String fileName) {
		super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
	}
}