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
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileUtil;
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
		bindService(CyServiceRegistrar.class);
		bindService(CyApplicationManager.class);
		bindService(CySwingApplication.class);
		bindService(CyNetworkManager.class);
		bindService(CyNetworkViewFactory.class);
		bindService(CyNetworkViewManager.class);
		bindService(CyNetworkFactory.class);
		bindService(IconManager.class);
		bindService(CyLayoutAlgorithmManager.class);
		bindService(CyGroupManager.class);
		bindService(CyGroupFactory.class);
		bindService(CyGroupAggregationManager.class);
		bindService(CyGroupSettingsManager.class);
		bindService(AvailableCommands.class);
		bindService(CommandExecutorTaskFactory.class);
		bindService(CySessionManager.class);
		bindService(CyEventHelper.class);
		bindService(OpenBrowser.class);
		bindService(VisualMappingManager.class);
		bindService(CyNetworkTableManager.class);
		bindService(CyTableManager.class);
		bindService(CyTableFactory.class);
		bindService(FileUtil.class);
		bindService(CyRootNetworkManager.class);
		
		bindService(DialogTaskManager.class);
		TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
		bind(synchronousManager).toProvider(service(synchronousManager).single());
		
		TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
		bind(taskManager).annotatedWith(Names.named("dialog")).toProvider(service(DialogTaskManager.class).single());
		bind(taskManager).annotatedWith(Names.named("sync")).toProvider(service(synchronousManager).single());
		
		bindService(AnnotationManager.class);
		TypeLiteral<AnnotationFactory<ShapeAnnotation>> shapeFactory = new TypeLiteral<AnnotationFactory<ShapeAnnotation>>(){};
		bind(shapeFactory).toProvider(service(shapeFactory).filter(ldap("(type=ShapeAnnotation.class)")).single());
		TypeLiteral<AnnotationFactory<TextAnnotation>> textFactory = new TypeLiteral<AnnotationFactory<TextAnnotation>>(){};
		bind(textFactory).toProvider(service(textFactory).filter(ldap("(type=TextAnnotation.class)")).single());
	}
		
	private <T> void bindService(Class<T> serviceClass) {
		bind(serviceClass).toProvider(service(serviceClass).single());
	}
	
	@Provides
	public JFrame getJFrame(CySwingApplication swingApplication) {
		return swingApplication.getJFrame();
	}

}
