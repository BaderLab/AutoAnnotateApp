package org.baderlab.autoannotate;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.util.SerialTestTaskManager;
import org.baderlab.autoannotate.util.SimpleLabelMakerFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
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
			TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
			bind(taskManager).annotatedWith(Names.named("sync")).to(SerialTestTaskManager.class);
			install(new FactoryModuleBuilder().build(CreateAnnotationSetTask.Factory.class));
			bind(LabelMakerManager.class).toInstance(mock(LabelMakerManager.class));
		}
	}
	
	
	public static CyNetworkView createNetwork() {
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetworkFactory networkFactory = networkTestSupport.getNetworkFactory();
		CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
		
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
			.setUseClusterMaker(false)
			.setClusterAlgorithm(null)
			.setClusterMakerEdgeAttribute(null)
			.setClusterDataColumn(CLUSTER_COL)
			.setCreateGroups(false);
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

}
