package org.baderlab.autoannotate.internal;

import static org.ops4j.peaberry.Peaberry.*;
import static org.ops4j.peaberry.util.Filters.ldap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Properties;

import org.baderlab.autoannotate.internal.io.SessionListener;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
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
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class CyActivator extends AbstractCyActivator {
	
	public static final String APP_NAME = "AutoAnnotate";  // Suitable for display in the UI
	public static final String APP_ID = "org.baderlab.autoannotate";  // Suitable as an ID for the App
	
	@Override
	public void start(BundleContext context) throws Exception {
		Injector injector = Guice.createInjector(osgiModule(context), new MainModule());
		
		// Eagerly create singleton managers to wire up event bus
		ModelManager modelManager = injector.getInstance(ModelManager.class);
		registerAllServices(context, modelManager, new Properties());
		injector.getInstance(PanelManager.class);
		injector.getInstance(AnnotationRenderer.class);
		
		ShowCreateDialogAction showDialogAction = injector.getInstance(ShowCreateDialogAction.class);
		showDialogAction.setPreferredMenu("Apps." + APP_NAME);
		registerAllServices(context, showDialogAction, new Properties());
		
		SessionListener sessionListener = injector.getInstance(SessionListener.class);
		registerAllServices(context, sessionListener, new Properties());
		
		// TEMPORARY
		TestGsonAction gsonAction = injector.getInstance(TestGsonAction.class);
		gsonAction.setPreferredMenu("Apps." + APP_NAME);
		registerAllServices(context, gsonAction, new Properties());
		
		
		// print all events to console
		EventBus eventBus = injector.getInstance(EventBus.class);
		eventBus.register(new Object() {
			@Subscribe public void log(Object event) {
				System.out.println("Event: " + event.getClass().getSimpleName());
			}
		});
	}
	
	
	private class MainModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(CyServiceRegistrar.class).toProvider(service(CyServiceRegistrar.class).single());
			bind(CyApplicationManager.class).toProvider(service(CyApplicationManager.class).single());
			bind(CySwingApplication.class).toProvider(service(CySwingApplication.class).single());
			bind(CyNetworkManager.class).toProvider(service(CyNetworkManager.class).single());
			bind(CyNetworkViewFactory.class).toProvider(service(CyNetworkViewFactory.class).single());
			bind(CyNetworkViewManager.class).toProvider(service(CyNetworkViewManager.class).single());
			bind(CyNetworkFactory.class).toProvider(service(CyNetworkFactory.class).single());
			bind(IconManager.class).toProvider(service(IconManager.class).single());
			
			bind(DialogTaskManager.class).toProvider(service(DialogTaskManager.class).single());
			TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
			bind(synchronousManager).toProvider(service(synchronousManager).single());
			bind(AvailableCommands.class).toProvider(service(AvailableCommands.class).single());
			bind(CommandExecutorTaskFactory.class).toProvider(service(CommandExecutorTaskFactory.class).single());
			
			bind(AnnotationManager.class).toProvider(service(AnnotationManager.class).single());
			TypeLiteral<AnnotationFactory<ShapeAnnotation>> shapeFactory = new TypeLiteral<AnnotationFactory<ShapeAnnotation>>(){};
			bind(shapeFactory).toProvider(service(shapeFactory).filter(ldap("(type=ShapeAnnotation.class)")).single());
			TypeLiteral<AnnotationFactory<TextAnnotation>> textFactory = new TypeLiteral<AnnotationFactory<TextAnnotation>>(){};
			bind(textFactory).toProvider(service(textFactory).filter(ldap("(type=TextAnnotation.class)")).single());
			
			// Create a single EventBus
			bind(EventBus.class).toInstance(new EventBus((e,c) -> e.printStackTrace()));
			
			// Call methods annotated with @AfterInjection after injection, mainly used to create UIs
			bindListener(new AfterInjectionMatcher(), new TypeListener() {
				AfterInjectionInvoker invoker = new AfterInjectionInvoker();
				public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
					encounter.register(invoker);
				}
			});
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
