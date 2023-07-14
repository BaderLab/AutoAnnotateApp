package org.baderlab.autoannotate;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters.ClusterIDParameters;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.baderlab.autoannotate.internal.ui.render.DrawClustersTask;
import org.baderlab.autoannotate.internal.ui.view.action.CreateClusterTask;
import org.baderlab.autoannotate.util.SerialTestTaskManager;
import org.baderlab.autoannotate.util.SimpleLabelMakerFactory;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.jukito.JukitoModule;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

public class NetworkTestUtil {

	public static final String CLUSTER_COL = "my_cluster";
	
	
	public static class TestModule extends JukitoModule {
		@Override
		protected void configureTest() {
			TableTestSupport tableTestSupport = new TableTestSupport();
			NetworkViewTestSupport networkViewTestSupport = new NetworkViewTestSupport();
			
			bind(CyNetworkFactory.class).toInstance(networkViewTestSupport.getNetworkFactory());
			bind(CyNetworkTableManager.class).toInstance(networkViewTestSupport.getNetworkTableManager());
			bind(CyNetworkManager.class).toInstance(networkViewTestSupport.getNetworkManager());
			bind(CyTableFactory.class).toInstance(tableTestSupport.getTableFactory());
			bind(CyNetworkViewFactory.class).toInstance(networkViewTestSupport.getNetworkViewFactory());
			
			TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
			bind(taskManager).annotatedWith(Names.named("sync")).to(SerialTestTaskManager.class);
			bind(taskManager).annotatedWith(Names.named("dialog")).to(SerialTestTaskManager.class);
			bind(LabelMakerManager.class).toInstance(mock(LabelMakerManager.class));
			bind(IconManager.class).toInstance(mock(IconManager.class));
			bind(CyColumnPresentationManager.class).toInstance(mock(CyColumnPresentationManager.class));

			installFactory(SummaryNetworkTask.Factory.class);
			installFactory(CreateAnnotationSetTask.Factory.class);
			installFactory(CreateClusterTask.Factory.class);
			installFactory(DrawClustersTask.Factory.class);
		}
		
		private void installFactory(Class<?> factoryInterface) {
			install(new FactoryModuleBuilder().build(factoryInterface));
		}
	}
	
	
	public static CyNetworkView createNetwork(CyNetworkFactory networkFactory, CyNetworkManager networkManager) {
		CyNetworkView networkView = mock(CyNetworkView.class);
		CyNetwork network = networkFactory.createNetwork();
		networkManager.addNetwork(network);
		when(networkView.getModel()).thenReturn(network);
		network.getDefaultNodeTable().createColumn(CLUSTER_COL, String.class, false);
		return networkView;
	}
	
	public static CyNode createNode(CyNetwork network, String name, String cluster) {
		CyNode node = network.addNode();
		CyRow row = network.getRow(node);
		row.set(CyNetwork.NAME, name);
		row.set(CLUSTER_COL, cluster);
		return node;
	}
	
	public static AnnotationSetTaskParamters.Builder basicBuilder(CyNetworkView networkView) {
		return new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(CyNetwork.NAME)
			.setLabelMakerFactory(new SimpleLabelMakerFactory())
			.setLabelMakerContext(null)
			.setClusterParameters(new ClusterIDParameters(CLUSTER_COL));
	}
	
	public static AnnotationSet createAnnotationSet(AnnotationSetTaskParamters params, CreateAnnotationSetTask.Factory taskFactory, ModelManager modelManager) {
		CreateAnnotationSetTask task = taskFactory.create(params);
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(new TaskIterator(task));

		CyNetworkView networkView = params.getNetworkView();
		AnnotationSet as = modelManager.getExistingNetworkViewSet(networkView).flatMap(NetworkViewSet::getActiveAnnotationSet).get();
		assertNotNull(as);
		return as;
	}

	public static List<Cluster> getSortedClusters(AnnotationSet as) {
		List<Cluster> clusters = new ArrayList<>(as.getClusters());
		clusters.sort(Comparator.comparing(Cluster::getLabel));
		return clusters;
	}
	
}
