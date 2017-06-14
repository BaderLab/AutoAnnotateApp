package org.baderlab.autoannotate;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.render.AnnotationRenderer;
import org.baderlab.autoannotate.internal.ui.render.DrawClusterTask;
import org.baderlab.autoannotate.internal.ui.render.EraseClusterTask;
import org.baderlab.autoannotate.internal.ui.render.RemoveAllAnnotationsTask;
import org.baderlab.autoannotate.internal.ui.render.SelectClusterTask;
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
	
	public static class TestModelPersistorModule extends JukitoModule {
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
	@Before
	public void setup(CyApplicationManager appManager, ModelManager modelManager, AnnotationRenderer renderer,
			RemoveAllAnnotationsTask.Factory removeTaskFactory, DrawClusterTask.Factory drawTaskFactory,
			EraseClusterTask.Factory eraseTaskFactory, SelectClusterTask.Factory selectTaskFactory) {
		
		// set up stubbing
		CyNetworkView networkView = mock(CyNetworkView.class);
		when(appManager.getCurrentNetworkView()).thenReturn(networkView);
		
		when(drawTaskFactory.create(any())).thenReturn(mock(DrawClusterTask.class));
		when(removeTaskFactory.create(any())).thenReturn(mock(RemoveAllAnnotationsTask.class));
		when(eraseTaskFactory.create(any())).thenReturn(mock(EraseClusterTask.class));
		when(selectTaskFactory.create(any(), anyBoolean())).thenReturn(mock(SelectClusterTask.class));
		
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
		reset(removeTaskFactory, drawTaskFactory, eraseTaskFactory, selectTaskFactory);
		when(drawTaskFactory.create(any())).thenReturn(mock(DrawClusterTask.class));
		when(removeTaskFactory.create(any())).thenReturn(mock(RemoveAllAnnotationsTask.class));
		when(eraseTaskFactory.create(any())).thenReturn(mock(EraseClusterTask.class));
		when(selectTaskFactory.create(any(), anyBoolean())).thenReturn(mock(SelectClusterTask.class));
	}
	
	
	private static List<CyNode> createNodes(CyNetwork network, int num) {
		List<CyNode> nodes = new ArrayList<>(10);
		for(int i = 0; i < num; i++) {
			nodes.add(network.addNode());
		}
		return nodes;
	}
	
	
	@Test
	public void testSelect(ModelManager modelManager, AnnotationRenderer renderer, 
						   EraseClusterTask.Factory eraseTaskFactory, DrawClusterTask.Factory drawTaskFactory) throws Exception {
		
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		nvs.select(null);
		
		InOrder inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any());
		inOrder.verify(drawTaskFactory, times(0)).create(any());
		
		// Verify that 3 clusters were drawn.
		nvs.select(as);
		
		// verify that removeTask was called first
		inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any());
		inOrder.verify(drawTaskFactory, times(3)).create(any());
		
		// Verify that annotations are cleared when no annotation set is selected
		nvs.select(null);
		
		inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any());
		inOrder.verify(drawTaskFactory, times(3)).create(any());
	}
	
	
	@Test
	public void testClusterChanged(ModelManager modelManager, EraseClusterTask.Factory eraseTaskFactory,
								   DrawClusterTask.Factory drawTaskFactory) throws Exception {
		
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		Cluster cluster = as.getClusters().iterator().next();
		cluster.setLabel("new_label");
		
		InOrder inOrder = inOrder(eraseTaskFactory, drawTaskFactory);
		inOrder.verify(eraseTaskFactory, times(0)).create(any());
		inOrder.verify(drawTaskFactory).create(any());
	}
	
	
	@Test
	public void testClusterAdded(ModelManager modelManager, AnnotationRenderer renderer, DrawClusterTask.Factory drawTaskFactory) {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		CyNetwork network = nvs.getNetwork();
		
		Cluster cluster = as.createCluster(createNodes(network,10), "cluster4", false);
		
		verify(drawTaskFactory).create(cluster);
	}
	
	
	@Test
	public void testClusterRemoved(ModelManager modelManager, AnnotationRenderer renderer, EraseClusterTask.Factory eraseTaskFactory) {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		Cluster cluster = as.getClusters().iterator().next();
		cluster.delete();
		
		verify(eraseTaskFactory).create(cluster);
	}
	
}
