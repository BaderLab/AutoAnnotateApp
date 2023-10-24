package org.baderlab.autoannotate.internal;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ops4j.peaberry.Peaberry.service;
import static org.ops4j.peaberry.util.Filters.ldap;

import java.lang.annotation.Retention;

import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnPresentationManager;
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
import org.cytoscape.task.select.DeselectAllTaskFactory;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.NetworkImageFactory;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.undo.UndoSupport;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;

/**
 * Guice module, binds Cytoscape services using Peaberry.
 * 
 * @author mkucera
 *
 */
public class CytoscapeServiceModule extends AbstractModule {

	// VisualMappingFunctionFactory
	@BindingAnnotation @Retention(RUNTIME) public @interface Continuous {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Discrete {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Passthrough {}
	
	
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
		bindService(CyColumnPresentationManager.class);
		bindService(UndoSupport.class);
		bindService(PanelTaskManager.class);
		bindService(PaletteProviderManager.class);
		bindService(NetworkImageFactory.class);
		bindService(VisualStyleFactory.class);
		bindService(DeselectAllTaskFactory.class);
		
		bindService(DialogTaskManager.class);
		TypeLiteral<SynchronousTaskManager<?>> synchronousManager = new TypeLiteral<SynchronousTaskManager<?>>(){};
		bind(synchronousManager).toProvider(service(synchronousManager).single());
		
		bindService(AnnotationManager.class);
		TypeLiteral<AnnotationFactory<ShapeAnnotation>> shapeFactory = new TypeLiteral<AnnotationFactory<ShapeAnnotation>>(){};
		bind(shapeFactory).toProvider(service(shapeFactory).filter(ldap("(type=ShapeAnnotation.class)")).single());
		TypeLiteral<AnnotationFactory<TextAnnotation>> textFactory = new TypeLiteral<AnnotationFactory<TextAnnotation>>(){};
		bind(textFactory).toProvider(service(textFactory).filter(ldap("(type=TextAnnotation.class)")).single());
		
		bind(VisualMappingFunctionFactory.class).annotatedWith(Continuous.class).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=continuous)")).single());
		bind(VisualMappingFunctionFactory.class).annotatedWith(Discrete.class).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=discrete)")).single());
		bind(VisualMappingFunctionFactory.class).annotatedWith(Passthrough.class).toProvider(service(VisualMappingFunctionFactory.class).filter(ldap("(mapping.type=passthrough)")).single());
	}
		
	private <T> void bindService(Class<T> serviceClass) {
		bind(serviceClass).toProvider(service(serviceClass).single());
	}
	
	@Provides
	public JFrame getJFrame(CySwingApplication swingApplication) {
		return swingApplication.getJFrame();
	}
	
	@Provides @Named("default")
	public Palette getDefaultPalette(PaletteProviderManager manager) {
		var providers = manager.getPaletteProviders();
		for(var provider : providers) {
			var p = provider.getPalette(getDefaultPaletteName());
			if(p != null) {
				return p;
			}
		}
		return null;
	}
	
	private static String getDefaultPaletteName() {
		// Copied from AbstractChartEditor
		return "Set3 colors";
	}
}
