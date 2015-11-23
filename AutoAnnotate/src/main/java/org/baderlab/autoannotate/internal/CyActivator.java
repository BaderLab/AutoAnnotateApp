package org.baderlab.autoannotate.internal;

import static org.ops4j.peaberry.Peaberry.*;
import static org.ops4j.peaberry.util.Filters.ldap;

import java.util.Properties;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.ui.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.ui.annotations.AnnotationRenderer;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		Injector injector = Guice.createInjector(osgiModule(context), new MainModule());
		
		// Eagerly create singleton managers
		injector.getInstance(ModelManager.class);
		injector.getInstance(AnnotationRenderer.class);
		
		ShowCreateDialogAction showDialogAction = injector.getInstance(ShowCreateDialogAction.class);
		showDialogAction.setPreferredMenu("Apps.AutoAnnotate");
		registerAllServices(context, showDialogAction, new Properties());
	}
	
	
	private class MainModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(CyApplicationManager.class).toProvider(service(CyApplicationManager.class).single());
			bind(CySwingApplication.class).toProvider(service(CySwingApplication.class).single());
			bind(CyNetworkManager.class).toProvider(service(CyNetworkManager.class).single());
			bind(CyNetworkViewFactory.class).toProvider(service(CyNetworkViewFactory.class).single());
			bind(CyNetworkViewManager.class).toProvider(service(CyNetworkViewManager.class).single());
			bind(CyNetworkFactory.class).toProvider(service(CyNetworkFactory.class).single());
			
			bind(DialogTaskManager.class).toProvider(service(DialogTaskManager.class).single());
			TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
			bind(synchronousManager).toProvider(service(synchronousManager).single());
			bind(CommandExecutorTaskFactory.class).toProvider(service(CommandExecutorTaskFactory.class).single());
			
			bind(AnnotationManager.class).toProvider(service(AnnotationManager.class).single());
			TypeLiteral<AnnotationFactory<ShapeAnnotation>> shapeFactory = new TypeLiteral<AnnotationFactory<ShapeAnnotation>>(){};
			bind(shapeFactory).toProvider(service(shapeFactory).filter(ldap("(type=ShapeAnnotation.class)")).single());
			TypeLiteral<AnnotationFactory<TextAnnotation>> textFactory = new TypeLiteral<AnnotationFactory<TextAnnotation>>(){};
			bind(textFactory).toProvider(service(textFactory).filter(ldap("(type=TextAnnotation.class)")).single());
			
			// Create a single EventBus for the entire App
			bind(EventBus.class).toInstance(new EventBus("AutoAnnotate Event Bus"));
		}
	}

}
