package org.baderlab.autoannotate.internal;

import java.util.Properties;

import org.baderlab.autoannotate.internal.command.AnnotateCommandTask;
import org.baderlab.autoannotate.internal.command.LabelClusterCommandTask;
import org.baderlab.autoannotate.internal.labels.LabelFactoryModule;
import org.baderlab.autoannotate.internal.labels.LabelQuestionButton;
import org.baderlab.autoannotate.internal.labels.makers.ClusterBoostedLabelMakerUI;
import org.baderlab.autoannotate.internal.labels.makers.MostSignificantLabelMaker;
import org.baderlab.autoannotate.internal.labels.makers.MostSignificantLabelMakerUI;
import org.baderlab.autoannotate.internal.labels.makers.SizeSortedLabelMakerUI;
import org.baderlab.autoannotate.internal.layout.CoseLayoutAlgorithmTask;
import org.baderlab.autoannotate.internal.layout.GridLayoutAnnotationSetTaskFactory;
import org.baderlab.autoannotate.internal.layout.GridLayoutClustersTaskFactory;
import org.baderlab.autoannotate.internal.model.DebugEventListener;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.CollapseTask;
import org.baderlab.autoannotate.internal.task.CopyAnnotationsTask;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.CreateSubnetworkTask;
import org.baderlab.autoannotate.internal.task.RecalculateLabelsTask;
import org.baderlab.autoannotate.internal.task.RunClusterMakerTaskFactory;
import org.baderlab.autoannotate.internal.task.RunEMAssociateTaskFactory;
import org.baderlab.autoannotate.internal.task.RunMCODETaskFactory;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.baderlab.autoannotate.internal.ui.PanelManager;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.baderlab.autoannotate.internal.ui.render.DrawClustersTask;
import org.baderlab.autoannotate.internal.ui.render.EraseClustersTask;
import org.baderlab.autoannotate.internal.ui.render.UpdateClustersTask;
import org.baderlab.autoannotate.internal.ui.render.VisibilityClearTask;
import org.baderlab.autoannotate.internal.ui.render.VisibilityTask;
import org.baderlab.autoannotate.internal.ui.view.ManageAnnotationSetsDialog;
import org.baderlab.autoannotate.internal.ui.view.action.CreateClusterTask;
import org.baderlab.autoannotate.internal.ui.view.action.LayoutClustersAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowWordcloudDialogAction;
import org.baderlab.autoannotate.internal.ui.view.copy.CopyAnnotationsDialog;
import org.baderlab.autoannotate.internal.ui.view.copy.NetworkList;
import org.baderlab.autoannotate.internal.ui.view.create.ClusterIDsOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.create.ClusterMCODEOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.create.ClusterMakerOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.create.ClusterOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.create.ClusterSizeOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialog;
import org.baderlab.autoannotate.internal.ui.view.create.InstallWarningPanel;
import org.baderlab.autoannotate.internal.ui.view.create.LabelOptionsPanel;
import org.baderlab.autoannotate.internal.ui.view.create.NormalModeTab;
import org.baderlab.autoannotate.internal.ui.view.create.QuickModeTab;
import org.baderlab.autoannotate.internal.ui.view.display.SignificancePanelFactory;
import org.baderlab.autoannotate.internal.ui.view.summary.AttributeAggregationPanel;
import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialog;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * Guice module, binds AutoAnnotate managers and event bus.
 */
public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		//bind(DebugEventListener.class).asEagerSingleton();
		bind(PanelManager.class).asEagerSingleton();
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
		installFactory(ClusterOptionsPanel.Factory.class);
		installFactory(ClusterMakerOptionsPanel.Factory.class);
		installFactory(ClusterMCODEOptionsPanel.Factory.class);
		installFactory(ClusterIDsOptionsPanel.Factory.class);
		installFactory(InstallWarningPanel.Factory.class);
		installFactory(LabelQuestionButton.Factory.class);
		installFactory(ManageAnnotationSetsDialog.Factory.class);
		installFactory(NormalModeTab.Factory.class);
		installFactory(QuickModeTab.Factory.class);
		installFactory(CollapseAllTaskFactory.Factory.class);
		installFactory(GridLayoutClustersTaskFactory.Factory.class);
		installFactory(RunClusterMakerTaskFactory.Factory.class);
		installFactory(RunMCODETaskFactory.Factory.class);
		installFactory(AnnotateCommandTask.Factory.class);
		installFactory(LabelClusterCommandTask.Factory.class);
		installFactory(GridLayoutAnnotationSetTaskFactory.Factory.class);
		installFactory(SummaryNetworkTask.Factory.class);
		installFactory(CollapseTask.Factory.class);
		installFactory(CreateAnnotationSetTask.Factory.class);
		installFactory(CreateClusterTask.Factory.class);
		installFactory(CreateSubnetworkTask.Factory.class);
		installFactory(RecalculateLabelsTask.Factory.class);
		installFactory(DrawClustersTask.Factory.class);
		installFactory(EraseClustersTask.Factory.class);
		installFactory(UpdateClustersTask.Factory.class);
		installFactory(CopyAnnotationsDialog.Factory.class);
		installFactory(NetworkList.Factory.class);
		installFactory(CopyAnnotationsTask.Factory.class);
		installFactory(CreateAnnotationSetDialog.Factory.class);
		installFactory(CoseLayoutAlgorithmTask.Factory.class);
		installFactory(LayoutClustersAction.Factory.class);
		installFactory(ShowWordcloudDialogAction.Factory.class);
		installFactory(SizeSortedLabelMakerUI.Factory.class);
		installFactory(ClusterBoostedLabelMakerUI.Factory.class);
		installFactory(MostSignificantLabelMakerUI.Factory.class);
		installFactory(MostSignificantLabelMaker.Factory.class);
		installFactory(SummaryNetworkDialog.Factory.class);
		installFactory(AttributeAggregationPanel.Factory.class);
		installFactory(RunEMAssociateTaskFactory.Factory.class);
		installFactory(SignificancePanelFactory.Factory.class);
		installFactory(ClusterSizeOptionsPanel.Factory.class);
		installFactory(VisibilityTask.Factory.class);
		installFactory(VisibilityClearTask.Factory.class);
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