package org.baderlab.autoannotate;

import static org.baderlab.autoannotate.NetworkTestUtil.createNode;
import static org.junit.Assert.assertEquals;

import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSetFactory;
import org.baderlab.autoannotate.internal.layout.CoseLayoutAlgorithmTask;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.task.CreateAnnotationSetTask;
import org.baderlab.autoannotate.internal.task.SummaryNetworkTask;
import org.baderlab.autoannotate.util.LogSilenceRule;
import org.baderlab.autoannotate.util.SerialTestTaskManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.UndoSupport;
import org.jukito.JukitoRunner;
import org.jukito.UseModules;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
@UseModules(NetworkTestUtil.TestModule.class)
public class TestSummaryNetworkAction {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	@Inject CreateAnnotationSetTask.Factory taskFactory;
	@Inject ModelManager modelManager;
	@Inject SummaryNetworkTask.Factory summaryTaskFactory;
	@Inject CyNetworkManager networkManager;
	@Inject UndoSupport undoSupport;
	@Inject CoseLayoutAlgorithmTask.Factory coseTaskFactory;
	@Inject AggregatorSetFactory aggregatorFactory;
	
	private CyNetworkView networkView;
	
	
	@Before
	public void setUp(CyNetworkFactory networkFactory, CyNetworkManager networkManager) {
		networkView = NetworkTestUtil.createNetwork(networkFactory, networkManager);
		CyNetwork network = networkView.getModel();
		
		createNode(network, "n1", "cluster_1");
		createNode(network, "n2", "cluster_1");
		createNode(network, "n3", "cluster_1");
		
		createNode(network, "n4", "cluster_2");
		createNode(network, "n5", "cluster_2");
		
		createNode(network, "n6", "cluster_3");
		
		createNode(network, "n7", "cluster_4");
		createNode(network, "n8", "cluster_5");
		createNode(network, "n9", "cluster_6");
	}
	
	
	@Test
	public void testSummaryNetwork() {
		test(false);
	}
	
	@Test
	public void testSummaryNetworkUnclustered() {
		test(true);
	}
	
	
	private void test(boolean includeUnclustered) {
		AnnotationSetTaskParamters params = NetworkTestUtil.basicBuilder(networkView)
				.setMaxClusters(3)
				.build();
		
		AnnotationSet as = NetworkTestUtil.createAnnotationSet(params, taskFactory, modelManager);
		
		CyNetwork network = as.getParent().getNetwork();
		
		AggregatorSet nodeAggSet = aggregatorFactory.createFor(network.getDefaultNodeTable());
		AggregatorSet edgeAggSet = aggregatorFactory.createFor(network.getDefaultNodeTable());
		
		SummaryNetworkTask summaryTask = summaryTaskFactory.create(as.getClusters(), nodeAggSet, edgeAggSet, includeUnclustered);
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(summaryTask);
		CyNetwork summaryNetwork = summaryTask.getResults(CyNetwork.class);
		
		assertEquals(includeUnclustered ? 6 : 3, summaryNetwork.getNodeCount());
	}
	
}
