package org.baderlab.autoannotate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.util.LogSilenceRule;
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
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

@RunWith(JukitoRunner.class)
public class TestCreateAnnotationSetTask {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	private CyNetwork network;
	private CyNetworkView networkView;
	private Map<String, CyNode> nodes;
	
	public static class TestModule extends JukitoModule {
		@Override
		protected void configureTest() {
			TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
			bind(taskManager).annotatedWith(Names.named("sync")).to(SerialTestTaskManager.class);
			install(new FactoryModuleBuilder().build(CreateAnnotationSetTask.Factory.class));
			bind(LabelMakerManager.class).toInstance(mock(LabelMakerManager.class));
		}
	}
	
	
	@Before
	public void setUp() {
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetworkFactory networkFactory = networkTestSupport.getNetworkFactory();
		CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
		
		networkView = mock(CyNetworkView.class);
		network = networkFactory.createNetwork();
		networkManager.addNetwork(network);
		when(networkView.getModel()).thenReturn(network);
		
		network.getDefaultNodeTable().createColumn("my_cluster", String.class, false);
		
		nodes = new HashMap<>();
		nodes.put("n1", createNode(network, "n1", "cluster_1"));
		nodes.put("n2", createNode(network, "n2", "cluster_1"));
		nodes.put("n3", createNode(network, "n3", "cluster_1"));
		
		nodes.put("n4", createNode(network, "n4", "cluster_2"));
		nodes.put("n5", createNode(network, "n5", "cluster_2"));
		
		nodes.put("n6", createNode(network, "n6", "cluster_3"));
		
		nodes.put("n7", createNode(network, "n7", null));
		nodes.put("n8", createNode(network, "n8", null));
		nodes.put("n9", createNode(network, "n9", null));
	}
	
	
	private static CyNode createNode(CyNetwork network, String name, String cluster) {
		CyNode node = network.addNode();
		CyRow row = network.getRow(node);
		row.set(CyNetwork.NAME, name);
		row.set("my_cluster", cluster);
		return node;
	}
	
	
	private AnnotationSetTaskParamters createParams(boolean createSingletonClusters) {
		return new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(CyNetwork.NAME)
			.setLabelMakerFactory(new SimpleLabelMakerFactory())
			.setLabelMakerContext(null)
			.setUseClusterMaker(false)
			.setClusterAlgorithm(null)
			.setClusterMakerEdgeAttribute(null)
			.setClusterDataColumn("my_cluster")
			.setCreateSingletonClusters(createSingletonClusters)
			.setCreateGroups(false)
			.build();
	}
	
	@Test
	public void testCreateAnnotationSet(CreateAnnotationSetTask.Factory taskFactory, ModelManager modelManager) {
		test(false, taskFactory, modelManager);
	}
	
	@Test
	public void testCreateAnnotationSetWithSingletonClusters(CreateAnnotationSetTask.Factory taskFactory, ModelManager modelManager) { 
		test(true, taskFactory, modelManager);
	}
	
	private void test(boolean createSingletonClusters, CreateAnnotationSetTask.Factory taskFactory, ModelManager modelManager) {
		AnnotationSetTaskParamters params = createParams(createSingletonClusters);

		CreateAnnotationSetTask task = taskFactory.create(params);
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(new TaskIterator(task));

		AnnotationSet as = modelManager.getExistingNetworkViewSet(networkView).flatMap(NetworkViewSet::getActiveAnnotationSet).get();
		assertNotNull(as);
		if(createSingletonClusters)
			assertEquals(6, as.getClusterCount());
		else
			assertEquals(3, as.getClusterCount());
		
		List<Cluster> clusters = new ArrayList<>(as.getClusters());
		clusters.sort(Comparator.comparing(Cluster::getLabel));
		
		Cluster cluster1 = clusters.get(0);
		assertEquals(3, cluster1.getNodeCount());
		assertTrue(cluster1.contains(nodes.get("n1")));
		assertTrue(cluster1.contains(nodes.get("n2")));
		assertTrue(cluster1.contains(nodes.get("n3")));
		
		Cluster cluster2 = clusters.get(1);
		assertEquals(2, cluster2.getNodeCount());
		assertTrue(cluster2.contains(nodes.get("n4")));
		assertTrue(cluster2.contains(nodes.get("n5")));
		
		Cluster cluster3 = clusters.get(2);
		assertEquals(1, cluster3.getNodeCount());
		assertTrue(cluster3.contains(nodes.get("n6")));
		
		if(createSingletonClusters) {
			Cluster cluster4 = clusters.get(3);
			assertEquals(1, cluster4.getNodeCount());
			assertTrue(cluster4.contains(nodes.get("n7")));
			
			Cluster cluster5 = clusters.get(4);
			assertEquals(1, cluster5.getNodeCount());
			assertTrue(cluster5.contains(nodes.get("n8")));
			
			Cluster cluster6 = clusters.get(5);
			assertEquals(1, cluster6.getNodeCount());
			assertTrue(cluster6.contains(nodes.get("n9")));
		}
	}

}
