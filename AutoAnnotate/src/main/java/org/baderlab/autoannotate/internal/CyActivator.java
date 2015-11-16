package org.baderlab.autoannotate.internal;

import static org.ops4j.peaberry.Peaberry.*;

import java.util.Properties;

import org.baderlab.autoannotate.internal.ui.action.ShowCreateDialogAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.osgi.framework.BundleContext;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		Injector injector = Guice.createInjector(osgiModule(context), new MainModule());
		
		ShowCreateDialogAction showDialogAction = injector.getInstance(ShowCreateDialogAction.class);
		showDialogAction.setPreferredMenu("Apps.AutoAnnotate");
		registerAllServices(context, showDialogAction, new Properties());
		
	}
	
	
	private class MainModule extends AbstractModule {
		@Override
		protected void configure() {
			// Bind Cytoscape services for injection
			bind(CyApplicationManager.class).toProvider(service(CyApplicationManager.class).single());
			bind(CySwingApplication.class).toProvider(service(CySwingApplication.class).single());
			bind(CyNetworkManager.class).toProvider(service(CyNetworkManager.class).single());
			bind(CyNetworkViewFactory.class).toProvider(service(CyNetworkViewFactory.class).single());
			bind(CyNetworkViewManager.class).toProvider(service(CyNetworkViewManager.class).single());
			bind(CyNetworkFactory.class).toProvider(service(CyNetworkFactory.class).single());
		}
	}

}
