package org.baderlab.autoannotate;

import static org.baderlab.autoannotate.NetworkTestUtil.createNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.util.LogSilenceRule;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;


@RunWith(JukitoRunner.class)
public class TestCreateAnnotationSetLimitTask {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	private CyNetwork network;
	private CyNetworkView networkView;
	private Map<String, CyNode> nodes;
	
	public static class TestModule extends NetworkTestUtil.TestModule { }
	
	
	@Before
	public void setUp() {
		networkView = NetworkTestUtil.createNetwork();
		network = networkView.getModel();
		
		nodes = new HashMap<>();
		nodes.put("n1", createNode(network, "n1", "cluster_1"));
		nodes.put("n2", createNode(network, "n2", "cluster_1"));
		nodes.put("n3", createNode(network, "n3", "cluster_1"));
		
		nodes.put("n4", createNode(network, "n4", "cluster_2"));
		nodes.put("n5", createNode(network, "n5", "cluster_2"));
		
		nodes.put("n6", createNode(network, "n6", "cluster_3"));
		
		nodes.put("n7", createNode(network, "n7", "cluster_4"));
		nodes.put("n8", createNode(network, "n8", "cluster_5"));
		nodes.put("n9", createNode(network, "n9", "cluster_6"));
	}
	
	
	@Test
	public void testCreateAnnotationSet(CreateAnnotationSetTask.Factory taskFactory, ModelManager modelManager) {
		AnnotationSetTaskParamters params = NetworkTestUtil.basicBuilder(networkView)
				.setMaxClusters(3)
				.build();
		
		AnnotationSet as = NetworkTestUtil.createAnnotationSet(params, taskFactory, modelManager);
		
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
	}

}
