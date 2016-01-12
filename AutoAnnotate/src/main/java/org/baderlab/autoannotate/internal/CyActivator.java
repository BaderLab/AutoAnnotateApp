package org.baderlab.autoannotate.internal;

import static org.cytoscape.work.ServiceProperties.*;
import static org.ops4j.peaberry.Peaberry.*;
import static org.ops4j.peaberry.util.Filters.ldap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.io.ModelTablePersistor;
import org.baderlab.autoannotate.internal.ui.CreateClusterTaskFactory;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.baderlab.autoannotate.internal.ui.view.WarnDialogModule;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
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
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
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
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;


public class CyActivator extends AbstractCyActivator {
	
	public static final String APP_NAME = "AutoAnnotate";  // Suitable for display in the UI
	public static final String APP_ID = "autoannotate";  // Suitable as an ID for the App
	
	private Injector injector;
	
	@Override
	public void start(BundleContext context) {
		injector = Guice.createInjector(osgiModule(context), new MainModule(), new WarnDialogModule());
		
		ModelManager modelManager = injector.getInstance(ModelManager.class);
		registerAllServices(context, modelManager, new Properties());
		PanelManager panelManager = injector.getInstance(PanelManager.class);
		injector.getInstance(AnnotationRenderer.class);
		
		AbstractCyAction showDialogAction = injector.getInstance(ShowCreateDialogAction.class);
		showDialogAction.setPreferredMenu("Apps." + APP_NAME);
		registerService(context, showDialogAction, CyAction.class, new Properties());
		
		AbstractCyAction showHideAction = panelManager.getShowHideAction();
		showHideAction.setPreferredMenu("Apps." + APP_NAME);
		registerService(context, showHideAction, CyAction.class, new Properties());
		
		CreateClusterTaskFactory createClusterTaskFactory = injector.getInstance(CreateClusterTaskFactory.class);
		Properties createClusterProps = new Properties();
		createClusterProps.setProperty(IN_MENU_BAR, "false");
		createClusterProps.setProperty(PREFERRED_MENU, APPS_MENU+".AutoAnnotate");
		createClusterProps.setProperty(TITLE, "Create Cluster");
		registerAllServices(context, createClusterTaskFactory, createClusterProps);
		
		ModelTablePersistor persistor = injector.getInstance(ModelTablePersistor.class);
		registerAllServices(context, persistor, new Properties());
		
		// Configuration properties
		CyProperty<Properties> configProps = injector.getInstance(Key.get(new TypeLiteral<CyProperty<Properties>>(){}));
		Properties propsReaderServiceProps = new Properties();
		propsReaderServiceProps.setProperty("cyPropertyName", APP_ID+".props");
		registerAllServices(context, configProps, propsReaderServiceProps);
		
		// If no session is loaded then this won't do anything, but if there is a session loaded 
		// then we want to load the model immediately.
		persistor.importModel();
	}
	
	
	@Override
	public void shutDown() {
		ModelTablePersistor persistor = injector.getInstance(ModelTablePersistor.class);
		persistor.exportModel();
		// MKTODO also dispose the panels?
	}
	
	
	private class MainModule extends AbstractModule {
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
			
			bind(CyNetworkTableManager.class).toProvider(service(CyNetworkTableManager.class).single());
			bind(CyTableManager.class).toProvider(service(CyTableManager.class).single());
			bind(CyTableFactory.class).toProvider(service(CyTableFactory.class).single());
			
			bind(DialogTaskManager.class).toProvider(service(DialogTaskManager.class).single());
			TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
			bind(synchronousManager).toProvider(service(synchronousManager).single());
			
			bind(AnnotationManager.class).toProvider(service(AnnotationManager.class).single());
			TypeLiteral<AnnotationFactory<ShapeAnnotation>> shapeFactory = new TypeLiteral<AnnotationFactory<ShapeAnnotation>>(){};
			bind(shapeFactory).toProvider(service(shapeFactory).filter(ldap("(type=ShapeAnnotation.class)")).single());
			TypeLiteral<AnnotationFactory<TextAnnotation>> textFactory = new TypeLiteral<AnnotationFactory<TextAnnotation>>(){};
			bind(textFactory).toProvider(service(textFactory).filter(ldap("(type=TextAnnotation.class)")).single());
			
			// Create a single EventBus
			bind(EventBus.class).toInstance(new EventBus((e,c) -> e.printStackTrace()));
			
			// Set up CyProperty
			bind(new TypeLiteral<CyProperty<Properties>>(){}).toInstance(new PropsReader(APP_ID, APP_ID+".props"));
			
			// Call methods annotated with @AfterInjection after injection, mainly used to create UIs
			bindListener(new AfterInjectionMatcher(), new TypeListener() {
				AfterInjectionInvoker invoker = new AfterInjectionInvoker();
				public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
					encounter.register(invoker);
				}
			});
		}
		
		@Provides
		public JFrame getJFrame(CySwingApplication swingApplication) {
			return swingApplication.getJFrame();
		}
	}
	
	
	class PropsReader extends AbstractConfigDirPropsReader {
		public PropsReader(String name, String fileName) {
			super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
		}
	}
	
	/**
	 * Guice matcher that matches types that have a method annotated with @AfterInjection
	 */
	private static class AfterInjectionMatcher extends AbstractMatcher<TypeLiteral<?>> {
		public boolean matches(TypeLiteral<?> typeLiteral) {
			Method[] methods = typeLiteral.getRawType().getDeclaredMethods();
			return Arrays.stream(methods).anyMatch(m -> m.isAnnotationPresent(AfterInjection.class));
		}
	}
	
	/**
	 * Invokes methods annotated with @AfterInjection
	 */
	static class AfterInjectionInvoker implements InjectionListener<Object> {
		public void afterInjection(Object injectee) {
			Method[] methods = injectee.getClass().getDeclaredMethods();
			for(Method method : methods) {
				if(method.isAnnotationPresent(AfterInjection.class)) {
					try {
						method.setAccessible(true);
						method.invoke(injectee);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
