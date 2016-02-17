package org.baderlab.autoannotate.internal;

import static org.ops4j.peaberry.Peaberry.service;
import static org.ops4j.peaberry.util.Filters.ldap;

import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

/**
 * Guice module, binds Cytoscape services using Peaberry.
 * 
 * @author mkucera
 *
 */
public class CytoscapeServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		// Bind cytoscape OSGi services
		bind(CyServiceRegistrar.class).toProvider(service(CyServiceRegistrar.class).single());
		bind(CyApplicationManager.class).toProvider(service(CyApplicationManager.class).single());
		bind(CySwingApplication.class).toProvider(service(CySwingApplication.class).single());
		bind(CyNetworkManager.class).toProvider(service(CyNetworkManager.class).single());
		bind(CyNetworkViewFactory.class).toProvider(service(CyNetworkViewFactory.class).single());
		bind(CyNetworkViewManager.class).toProvider(service(CyNetworkViewManager.class).single());
		bind(CyNetworkFactory.class).toProvider(service(CyNetworkFactory.class).single());
		bind(IconManager.class).toProvider(service(IconManager.class).single());
		bind(CyLayoutAlgorithmManager.class).toProvider(service(CyLayoutAlgorithmManager.class).single());
		bind(CyGroupManager.class).toProvider(service(CyGroupManager.class).single());
		bind(CyGroupFactory.class).toProvider(service(CyGroupFactory.class).single());
		bind(AvailableCommands.class).toProvider(service(AvailableCommands.class).single());
		bind(CommandExecutorTaskFactory.class).toProvider(service(CommandExecutorTaskFactory.class).single());
		bind(CySessionManager.class).toProvider(service(CySessionManager.class).single());
		bind(CyEventHelper.class).toProvider(service(CyEventHelper.class).single());
		bind(OpenBrowser.class).toProvider(service(OpenBrowser.class).single());
		bind(VisualMappingManager.class).toProvider(service(VisualMappingManager.class).single());
		
		bind(CyNetworkTableManager.class).toProvider(service(CyNetworkTableManager.class).single());
		bind(CyTableManager.class).toProvider(service(CyTableManager.class).single());
		bind(CyTableFactory.class).toProvider(service(CyTableFactory.class).single());
		
		bind(DialogTaskManager.class).toProvider(service(DialogTaskManager.class).single());
		TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
		bind(synchronousManager).toProvider(service(synchronousManager).single());
		
		TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
		bind(taskManager).annotatedWith(Names.named("dialog")).toProvider(service(DialogTaskManager.class).single());
		bind(taskManager).annotatedWith(Names.named("sync")).toProvider(service(synchronousManager).single());
		
		bind(AnnotationManager.class).toProvider(service(AnnotationManager.class).single());
		TypeLiteral<AnnotationFactory<ShapeAnnotation>> shapeFactory = new TypeLiteral<AnnotationFactory<ShapeAnnotation>>(){};
		bind(shapeFactory).toProvider(service(shapeFactory).filter(ldap("(type=ShapeAnnotation.class)")).single());
		TypeLiteral<AnnotationFactory<TextAnnotation>> textFactory = new TypeLiteral<AnnotationFactory<TextAnnotation>>(){};
		bind(textFactory).toProvider(service(textFactory).filter(ldap("(type=TextAnnotation.class)")).single());
	}
		
	@Provides
	public JFrame getJFrame(CySwingApplication swingApplication) {
		return swingApplication.getJFrame();
	}

}
