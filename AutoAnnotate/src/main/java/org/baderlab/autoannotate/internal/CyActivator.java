package org.baderlab.autoannotate.internal;

import static org.baderlab.autoannotate.internal.util.TaskTools.taskFactory;
import static org.cytoscape.work.ServiceProperties.*;

import java.util.Arrays;
import java.util.Properties;

import org.baderlab.autoannotate.internal.command.AnnotateCommandTask;
import org.baderlab.autoannotate.internal.command.CollapseCommandTask;
import org.baderlab.autoannotate.internal.command.EMHighlightTask;
import org.baderlab.autoannotate.internal.command.ExpandCommandTask;
import org.baderlab.autoannotate.internal.command.ExportModelCommandTask;
import org.baderlab.autoannotate.internal.command.LabelClusterCommandTask;
import org.baderlab.autoannotate.internal.command.LayoutCommandTask;
import org.baderlab.autoannotate.internal.command.OpenDialogCommandTask;
import org.baderlab.autoannotate.internal.command.RedrawCommandTask;
import org.baderlab.autoannotate.internal.command.SummaryNetworkCommandTask;
import org.baderlab.autoannotate.internal.labels.LabelFactoryModule;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.layout.CoseLayoutAlgorithm;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.io.ModelTablePersistor;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.ui.view.action.CreateClusterTaskFactory;
import org.baderlab.autoannotate.internal.ui.view.action.SelectClusterTaskFactory;
import org.baderlab.autoannotate.internal.ui.view.action.ShowAboutDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowHelpAction;
import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialogManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;


public class CyActivator extends AbstractCyActivator {
	
	private Injector injector;
	
	
	private static Injector createInjector(BundleContext bc) {
		return Guice.createInjector(
			new OSGiModule(bc), 
			new AfterInjectionModule(), 
			new CytoscapeServiceModule(), 
			new ApplicationModule(), 
			new LabelFactoryModule(), 
			new WarnDialogModule()
		);
	}
	
	@Override
	public void start(BundleContext bc) {
		injector = createInjector(bc);
		
		// ModelManager listens to Cytoscape events
		ModelManager modelManager = injector.getInstance(ModelManager.class);
		registerAllServices(bc, modelManager);
		
		// Register menu Actions
		var panelManager = injector.getInstance(PanelManager.class);
		registerAppsMenuAction(bc, injector.getInstance(ShowCreateDialogAction.class), ShowCreateDialogAction.TITLE, 1.0f, false);
		registerAppsMenuAction(bc, panelManager.getShowHideActionTaskFactory(), PanelManager.SHOW_HIDE_TITLE, 2.0f, false);
		registerAppsMenuAction(bc, injector.getInstance(ShowHelpAction.class), ShowHelpAction.TITLE, 3.0f, true);
		registerAppsMenuAction(bc, injector.getInstance(ShowAboutDialogAction.class), ShowAboutDialogAction.TITLE, 4.0f, false);
		
		// Context menu actions in network view
		registerContextMenuAction(bc, CreateClusterTaskFactory.class, "AutoAnnotate - Create Cluster");
		registerContextMenuAction(bc, SelectClusterTaskFactory.class, "AutoAnnotate - Select Cluster");
		
		// ModelTablePersistor listens to session save/load events
		ModelTablePersistor persistor = injector.getInstance(ModelTablePersistor.class);
		registerAllServices(bc, persistor);
		
		// Dialog manager needs to listen to cytoscape events
		CreateAnnotationSetDialogManager dialogManager = injector.getInstance(CreateAnnotationSetDialogManager.class);
		registerAllServices(bc, dialogManager);
		
		// Configuration properties
		CyProperty<Properties> configProps = injector.getInstance(Key.get(new TypeLiteral<CyProperty<Properties>>(){}));
		Properties propsReaderServiceProps = new Properties();
		propsReaderServiceProps.setProperty("cyPropertyName", "autoannotate.props");
		registerAllServices(bc, configProps, propsReaderServiceProps);
		
		// Commands that depend on LabelMaker
		LabelMakerManager labelMakerManager = injector.getInstance(LabelMakerManager.class);
		for(LabelMakerFactory<?> factory : labelMakerManager.getFactories()) {
			// MKTODO make sure the factory ID doesn't contain spaces or other illegal characters
			LabelClusterCommandTask.Factory labelClusterCommandTaskFactory = injector.getInstance(LabelClusterCommandTask.Factory.class);
			TaskFactory labelTaskFactory = taskFactory(() -> labelClusterCommandTaskFactory.create(factory));
			
			AnnotateCommandTask.Factory annotateCommandTaskFactory = injector.getInstance(AnnotateCommandTask.Factory.class);
			TaskFactory annotateTaskFactory = taskFactory(() -> annotateCommandTaskFactory.create(factory));
			
			String id = factory.getID();
			String description = String.join(" ", Arrays.asList(factory.getDescription()));
			registerCommand(bc, "label-"+id, true, labelTaskFactory, "Run label algorithm '" + id + "'. " + description);
			registerCommand(bc, "annotate-"+id, true, annotateTaskFactory, "Annotate network using label algorithm '" + id + "'. " + description);
		}
		
		// Regular commands
		registerCommand(bc, "redraw", RedrawCommandTask.class, "Redraw annotations");
		registerCommand(bc, "layout", LayoutCommandTask.class, "Layout network by clusters");
		registerCommand(bc, "collapse", CollapseCommandTask.class, "Collapse all clusters");
		registerCommand(bc, "expand", ExpandCommandTask.class, "Expand all clusters");
		registerCommand(bc, "summary", SummaryNetworkCommandTask.class, "Create summary network");
		registerCommand(bc, "export model", ExportModelCommandTask.class, "Export table model (this is for debugging)");
		registerCommand(bc, "open", OpenDialogCommandTask.class, "Opens the AutoAnnotate Create dialog");
		registerCommand(bc, "eminit", EMHighlightTask.class, "Used by EnrichmentMap to initialize the network");
		
		// CoSE layout
		var coseLayout = injector.getInstance(CoseLayoutAlgorithm.class);
        Properties layoutProps = new Properties();
        layoutProps.setProperty("preferredTaskManager","menu");
        layoutProps.setProperty(MENU_GRAVITY, "32.0"); // This puts it after the Cy3D layouts.
        layoutProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
        layoutProps.setProperty(INSERT_SEPARATOR_AFTER, "true");
		registerService(bc, coseLayout, CyLayoutAlgorithm.class, layoutProps);
		
		// If no session is loaded then this won't do anything, but if there is a session loaded 
		// then we want to load the model immediately.
		persistor.importModel();
	}
	
	
	@Override
	public void shutDown() {
		// MKTODO make this smarter like how EM does it
		try {
			ModelTablePersistor persistor = injector.getInstance(ModelTablePersistor.class);
			persistor.exportModel();
			ModelManager modelManager = injector.getInstance(ModelManager.class);
			modelManager.dispose();
		} finally {
			super.shutDown();
		}
	}
	
	
	private void registerContextMenuAction(BundleContext bc, Class<? extends NodeViewTaskFactory> taskFactoryClass, String title) {
		NodeViewTaskFactory taskFactory = injector.getInstance(taskFactoryClass);
		Properties props = new Properties();
		props.setProperty(IN_MENU_BAR, "false");
		props.setProperty(PREFERRED_MENU, APPS_MENU);
		props.setProperty(TITLE, title);
		registerAllServices(bc, taskFactory, props);
	}
	
	private void registerAppsMenuAction(BundleContext bc, TaskFactory taskFactory, String title, float gravity, boolean sep) {
		var props = new Properties();
		props.setProperty(PREFERRED_MENU, "Apps.AutoAnnotate[3.2]");
		props.setProperty(MENU_GRAVITY, String.valueOf(gravity));
		props.setProperty(TITLE, title);
		if(sep)
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		registerService(bc, taskFactory, TaskFactory.class, props);
	}
	
	private void registerCommand(BundleContext bc, String name, Class<? extends Task> type, String description) {
		TaskFactory taskFactory = taskFactory(injector.getProvider(type));
		registerCommand(bc, name, false, taskFactory, description);
	}
	
	private void registerCommand(BundleContext bc, String name, boolean json, TaskFactory factory, String description) {
		Properties props = new Properties();
		props.put(ServiceProperties.COMMAND, name);
		props.put(ServiceProperties.COMMAND_NAMESPACE, "autoannotate");
		if(json)
			props.put(ServiceProperties.COMMAND_SUPPORTS_JSON, "true");
		if(description != null)
			props.put(ServiceProperties.COMMAND_LONG_DESCRIPTION, description);
		registerService(bc, factory, TaskFactory.class, props);
	}
	
}
