package org.baderlab.autoannotate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.baderlab.autoannotate.internal.ui.render.DrawClustersTask;
import org.baderlab.autoannotate.internal.ui.render.EraseClustersTask;
import org.baderlab.autoannotate.internal.ui.render.UpdateClustersTask;
import org.baderlab.autoannotate.internal.ui.view.display.Significance;
import org.baderlab.autoannotate.util.LogSilenceRule;
import org.baderlab.autoannotate.util.SerialTestTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskManager;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

@RunWith(JukitoRunner.class)
public class TestRenderer {
	
	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	public static class TestModule extends JukitoModule {
		@Override
		protected void configureTest() {
			TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
			bind(taskManager).annotatedWith(Names.named("dialog")).to(SerialTestTaskManager.class);
			bind(taskManager).annotatedWith(Names.named("sync")).to(SerialTestTaskManager.class);
		}
	}
	
	
	/*
	 * This test suite tests that the correct drawing tasks are run by the AnnotationRenderer.
	 */
	@SuppressWarnings("unchecked")
	@Before
	public void setup(CyApplicationManager appManager, ModelManager modelManager, AnnotationRenderer renderer,
			DrawClustersTask.Factory drawTaskFactory, EraseClustersTask.Factory eraseTaskFactory) {
		
		// set up stubbing
		CyNetworkView networkView = mock(CyNetworkView.class);
		when(appManager.getCurrentNetworkView()).thenReturn(networkView);
		
		when(drawTaskFactory.create(any(Collection.class))).thenReturn(mock(DrawClustersTask.class));
		when(drawTaskFactory.create(any(Cluster.class))).thenReturn(mock(DrawClustersTask.class));
		when(eraseTaskFactory.create(any(Collection.class))).thenReturn(mock(EraseClustersTask.class));
		when(eraseTaskFactory.create(any(Cluster.class))).thenReturn(mock(EraseClustersTask.class));
		
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetworkFactory networkFactory = networkTestSupport.getNetworkFactory();
		CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
		
		CyNetwork network = networkFactory.createNetwork();
		networkManager.addNetwork(network);
		when(networkView.getModel()).thenReturn(network);

		// create model fixture
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		AnnotationSet as = nvs.createAnnotationSet("as1", "lc1");
		as.createCluster(createNodes(network,10), "cluster1", false);
		as.createCluster(createNodes(network,10), "cluster2", false);
		as.createCluster(createNodes(network,10), "cluster3", false);
		nvs.select(as);
		
		// Reset invocation counts from fixture initialization.
		// Unfortunately reset() also resets the stubbing so we need to redo it.
		reset(drawTaskFactory, eraseTaskFactory);
		
		when(drawTaskFactory.create(any(Collection.class))).thenReturn(mock(DrawClustersTask.class));
		when(drawTaskFactory.create(any(Cluster.class))).thenReturn(mock(DrawClustersTask.class));
		when(eraseTaskFactory.create(any(Collection.class))).thenReturn(mock(EraseClustersTask.class));
		when(eraseTaskFactory.create(any(Cluster.class))).thenReturn(mock(EraseClustersTask.class));
	}
	
	
	private static List<CyNode> createNodes(CyNetwork network, int num) {
		List<CyNode> nodes = new ArrayList<>(10);
		for(int i = 0; i < num; i++) {
			nodes.add(network.addNode());
		}
		return nodes;
	}
	
	
	@SuppressWarnings("unchecked")
	@Ignore
	public void testSelect(ModelManager modelManager, AnnotationRenderer renderer, 
						   EraseClustersTask.Factory eraseTaskFactory, DrawClustersTask.Factory drawTaskFactory) throws Exception {
		
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		nvs.select(null);
		
		InOrder inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any(Collection.class));
		inOrder.verify(drawTaskFactory, times(1)).create(any(Collection.class));
		
		// Verify that 3 clusters were drawn.
		nvs.select(as);
		
		// verify that removeTask was called first
		inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any(Collection.class));
		inOrder.verify(drawTaskFactory, times(1)).create(any(Collection.class));
		
		// Verify that annotations are cleared when no annotation set is selected
		nvs.select(null);
		
		inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any(Collection.class));
		inOrder.verify(drawTaskFactory, times(1)).create(any(Collection.class));
	}
	
	
	@Test
	public void testClusterChanged(ModelManager modelManager, EraseClustersTask.Factory eraseTaskFactory,
								   UpdateClustersTask.Factory updateTaskFactory) throws Exception {
		
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		Cluster cluster = as.getClusters().iterator().next();
		cluster.setLabel("new_label", false);
		
		InOrder inOrder = inOrder(eraseTaskFactory, updateTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any(Cluster.class));
		// This is debounced now, so ignore.
//		inOrder.verify(updateTaskFactory).create(any(Collection.class));
	}
	
	
	@Test
	public void testClusterAdded(ModelManager modelManager, AnnotationRenderer renderer, DrawClustersTask.Factory drawTaskFactory) {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		CyNetwork network = nvs.getNetwork();
		
		Cluster cluster = as.createCluster(createNodes(network,10), "cluster4", false);
		
		verify(drawTaskFactory).create(cluster);
	}
	
	
	@Test
	public void testClusterRemoved(ModelManager modelManager, AnnotationRenderer renderer, EraseClustersTask.Factory eraseTaskFactory) {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		Cluster cluster = as.getClusters().iterator().next();
		cluster.delete();
		
		verify(eraseTaskFactory).create(cluster);
	}
	
	@Test
	public void testSignificance() {
		List<Double> sigs = new ArrayList<>(Arrays.asList(1.0, 2.0, -3.0, -1.0, 0.0, null));
		
		assertFalse(Significance.MAXIMUM.isMoreSignificant(null, 1.0));
		assertTrue(Significance.MAXIMUM.isMoreSignificant(1.0, null));
		assertFalse(Significance.GREATEST_MAGNITUDE.isMoreSignificant(null, 1.0));
		assertTrue(Significance.GREATEST_MAGNITUDE.isMoreSignificant(1.0, null));
		
		for(int i = 0; i < 10; i++) {
			Collections.shuffle(sigs);
			
			Double s1 = mostSig(sigs, Significance.GREATEST_MAGNITUDE);
			assertTrue("got " + s1 + " for " + sigs.toString(), s1 != null && s1 == -3.0);
			
			Double s2 = mostSig(sigs, Significance.MAXIMUM);
			assertTrue("got " + s2 + " for " + sigs.toString(), s2 != null && s2 == 2.0);
			
			Double s3 = mostSig(sigs, Significance.MINIMUM);
			assertTrue("got " + s3 + " for " + sigs.toString(), s3 != null && s3 == -3.0);
		}
	}
	
	private static Double mostSig(List<Double> vals, Significance sigOp) {
		Double mostSigVal = null;
		for(Double value : vals) {
			if(sigOp.isMoreSignificant(value, mostSigVal)) {
				mostSigVal = value;
			}
		}
		return mostSigVal;
	}
	
}
