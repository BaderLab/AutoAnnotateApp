package org.baderlab.autoannotate.internal;

import java.util.Properties;

import org.baderlab.autoannotate.internal.command.AnnotateCommandTask;
import org.baderlab.autoannotate.internal.command.LabelClusterCommandTask;
import org.baderlab.autoannotate.internal.labels.LabelFactoryModule;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CollapseTask;
import org.baderlab.autoannotate.internal.task.LayoutAnnotationSetTaskFactory;
import org.baderlab.autoannotate.internal.task.LayoutClustersTaskFactory;
import org.baderlab.autoannotate.internal.task.RunClusterMakerTaskFactory;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.PanelManagerImpl;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.baderlab.autoannotate.internal.ui.render.DrawClusterTask;
import org.baderlab.autoannotate.internal.ui.render.EraseClusterTask;
import org.baderlab.autoannotate.internal.ui.render.RemoveAllAnnotationsTask;
import org.baderlab.autoannotate.internal.ui.render.SelectClusterTask;
import org.baderlab.autoannotate.internal.ui.view.LabelOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.ManageAnnotationSetsDialog;
import org.baderlab.autoannotate.internal.ui.view.dialog.EasyModePanel;
import org.baderlab.autoannotate.internal.ui.view.dialog.NormalModePanel;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

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
		bind(LabelFactoryModule.class).asEagerSingleton();
		installFactories();
		
		// Create a single EventBus
		bind(EventBus.class).toInstance(new EventBus((e,c) -> e.printStackTrace()));
		
		// Set up CyProperty
		PropsReader propsReader = new PropsReader(BuildProperties.APP_ID, "autoannotate.props");
		bind(new TypeLiteral<CyProperty<Properties>>(){}).toInstance(propsReader);
	}
	
	
	private void installFactories() {
		installFactory(LabelOptionsPanel.Factory.class);
		installFactory(ManageAnnotationSetsDialog.Factory.class);
		installFactory(NormalModePanel.Factory.class);
		installFactory(EasyModePanel.Factory.class);
		installFactory(CollapseAllTaskFactory.Factory.class);
		installFactory(LayoutClustersTaskFactory.Factory.class);
		installFactory(RunClusterMakerTaskFactory.Factory.class);
		installFactory(AnnotateCommandTask.Factory.class);
		installFactory(LabelClusterCommandTask.Factory.class);
		installFactory(LayoutAnnotationSetTaskFactory.Factory.class);
		installFactory(SummaryNetworkTask.Factory.class);
		installFactory(CollapseTask.Factory.class);
		
		
		installFactory(DrawClusterTask.Factory.class);
		installFactory(EraseClusterTask.Factory.class);
		installFactory(SelectClusterTask.Factory.class);
		installFactory(RemoveAllAnnotationsTask.Factory.class);
		
		
//		install(new FactoryModuleBuilder().implement(Task.class, DrawClusterTask.class).build(RenderTaskFactory.class));
		
	}
	
	private void installFactory(Class<?> factoryInterface) {
		install(new FactoryModuleBuilder().build(factoryInterface));
	}
}

class PropsReader extends AbstractConfigDirPropsReader {
	public PropsReader(String name, String fileName) {
		super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
	}
}