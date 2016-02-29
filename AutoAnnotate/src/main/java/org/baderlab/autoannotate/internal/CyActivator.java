package org.baderlab.autoannotate.internal;

import static org.cytoscape.work.ServiceProperties.*;

import java.util.Properties;

import org.baderlab.autoannotate.internal.labels.LabelFactoryModule;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.io.ModelTablePersistor;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.ui.view.action.CreateClusterTaskFactory;
import org.baderlab.autoannotate.internal.ui.view.action.ShowAboutDialogAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;


public class CyActivator extends AbstractCyActivator {
	
	private Injector injector;
	
	
	@Override
	public void start(BundleContext context) {
		injector = Guice.createInjector(
						new OSGiModule(context), // Peaberry
						new AfterInjectionModule(), 
						new CytoscapeServiceModule(), 
						new ApplicationModule(), 
						new LabelFactoryModule(),
						new WarnDialogModule());
		
		// ModelManager listens to Cytoscape events
		ModelManager modelManager = injector.getInstance(ModelManager.class);
		registerAllServices(context, modelManager, new Properties());
		
		// Register menu Actions
		PanelManager panelManager = injector.getInstance(PanelManager.class);
		AbstractCyAction showDialogAction = injector.getInstance(ShowCreateDialogAction.class);
		AbstractCyAction showHideAction = panelManager.getShowHideAction();
		AbstractCyAction showAboutAction = injector.getInstance(ShowAboutDialogAction.class);
		registerAction(context, showDialogAction);
		registerAction(context, showHideAction);
		registerAction(context, showAboutAction);
		
		// Context menu action in network view
		CreateClusterTaskFactory createClusterTaskFactory = injector.getInstance(CreateClusterTaskFactory.class);
		Properties createClusterProps = new Properties();
		createClusterProps.setProperty(IN_MENU_BAR, "false");
		createClusterProps.setProperty(PREFERRED_MENU, APPS_MENU + "." + BuildProperties.APP_NAME);
		createClusterProps.setProperty(TITLE, "Create Cluster");
		registerAllServices(context, createClusterTaskFactory, createClusterProps);
		
		// ModelTablePersistor listents to session save/load events
		ModelTablePersistor persistor = injector.getInstance(ModelTablePersistor.class);
		registerAllServices(context, persistor, new Properties());
		
		// Configuration properties
		CyProperty<Properties> configProps = injector.getInstance(Key.get(new TypeLiteral<CyProperty<Properties>>(){}));
		Properties propsReaderServiceProps = new Properties();
		propsReaderServiceProps.setProperty("cyPropertyName", "autoannotate.props");
		registerAllServices(context, configProps, propsReaderServiceProps);
		
		// If no session is loaded then this won't do anything, but if there is a session loaded 
		// then we want to load the model immediately.
		persistor.importModel();
	}
	
	
	@Override
	public void shutDown() {
		ModelTablePersistor persistor = injector.getInstance(ModelTablePersistor.class);
		persistor.exportModel();
		ModelManager modelManager = injector.getInstance(ModelManager.class);
		modelManager.dispose();
	}
	
	
	private void registerAction(BundleContext context, AbstractCyAction action) {
		action.setPreferredMenu("Apps." + BuildProperties.APP_NAME);
		registerService(context, action, CyAction.class, new Properties());
	}
	
}
