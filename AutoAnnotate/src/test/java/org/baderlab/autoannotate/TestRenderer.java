package org.baderlab.autoannotate;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
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
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.jukito.TestScope;
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
			// Normally these tasks would not be singletons, need to do this to use verify()
			bindSpy(RemoveAllAnnotationsTask.class).in(TestScope.SINGLETON);
			bindSpy(DrawClusterTask.class).in(TestScope.SINGLETON);
			bindSpy(EraseClusterTask.class).in(TestScope.SINGLETON);
			bindSpy(SelectClusterTask.class).in(TestScope.SINGLETON);
			
			TypeLiteral<TaskManager<?,?>> taskManager = new TypeLiteral<TaskManager<?,?>>(){};
			bind(taskManager).annotatedWith(Names.named("dialog")).to(SerialTestTaskManager.class);
			bind(taskManager).annotatedWith(Names.named("sync")).to(SerialTestTaskManager.class);
		}
	}
	
	
	/*
	 * This test suite tests that the correct drawing tasks are run by the AnnotationRenderer.
	 */
	@Before
	public void setup(CyApplicationManager appManager, ModelManager modelManager, 
			RemoveAllAnnotationsTask removeTask, DrawClusterTask drawTask) {
		
		CyNetworkView networkView = mock(CyNetworkView.class);
		when(appManager.getCurrentNetworkView()).thenReturn(networkView);
		
		NetworkTestSupport networkTestSupport = new NetworkTestSupport();
		CyNetworkFactory networkFactory = networkTestSupport.getNetworkFactory();
		CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
		
		CyNetwork network = networkFactory.createNetwork();
		networkManager.addNetwork(network);
		when(networkView.getModel()).thenReturn(network);

		resetTasks(removeTask, drawTask);
		
		// set up model
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		AnnotationSet as = nvs.createAnnotationSet("as1", "lc1");
		as.createCluster(createNodes(network,10), "cluster1", false);
		as.createCluster(createNodes(network,10), "cluster2", false);
		as.createCluster(createNodes(network,10), "cluster3", false);
		nvs.select(as);
		
		// Creating a cluster causes drawTask to run, want to ignore those invocations
		resetTasks(removeTask, drawTask);
	}
	
	
	private static List<CyNode> createNodes(CyNetwork network, int num) {
		List<CyNode> nodes = new ArrayList<>(10);
		for(int i = 0; i < num; i++) {
			nodes.add(network.addNode());
		}
		return nodes;
	}
	
	private static void resetTasks(Task... tasks) {
		// Don't actually want to run the tasks as they will explode.
		// Note: It would have been better to use interfaces for the tasks and bound them to mocks.
		for(Task task : tasks) {
			reset(task);
			try {
				doNothing().when(task).run(any());
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}
 	
	
	@Test
	public void testSelect(ModelManager modelManager, RemoveAllAnnotationsTask removeTask, DrawClusterTask drawTask) {
		resetTasks(removeTask, drawTask);
		
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		// Verify that annotations are cleared when no annotation set is selected
		nvs.select(null);
		
		verify(removeTask).setNetworkViewSet(nvs);
		InOrder inOrder = inOrder(removeTask, drawTask);
		inOrder.verify(removeTask).run(any());
		inOrder.verify(drawTask, times(0)).run(any());
		resetTasks(removeTask, drawTask);
		
		// Verify that 3 clusters were drawn.
		nvs.select(as);
		
		verify(removeTask).setNetworkViewSet(nvs);
		// verify that removeTask was called first
		inOrder = inOrder(removeTask, drawTask);
		inOrder.verify(removeTask).run(any());
		inOrder.verify(drawTask, times(3)).run(any());
		resetTasks(removeTask, drawTask);
		
		// Verify that annotations are cleared when no annotation set is selected
		nvs.select(null);
		
		verify(removeTask).setNetworkViewSet(nvs);
		inOrder = inOrder(removeTask, drawTask);
		inOrder.verify(removeTask).run(any());
		inOrder.verify(drawTask, times(0)).run(any());
	}
	
	
	@Test
	public void testClusterChanged(ModelManager modelManager, EraseClusterTask eraseTask, DrawClusterTask drawTask) throws Exception {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		resetTasks(eraseTask, drawTask);
		
		Cluster cluster = as.getClusters().iterator().next();
		cluster.setLabel("new_label");
		
		verify(eraseTask).setCluster(cluster);
		verify(drawTask).setCluster(cluster);
		
		InOrder inOrder = inOrder(eraseTask, drawTask);
		inOrder.verify(eraseTask).run(any());
		inOrder.verify(drawTask).run(any());
	}
	
	
	@Test
	public void testClusterAdded(ModelManager modelManager, AnnotationRenderer renderer, DrawClusterTask drawTask) {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		CyNetwork network = nvs.getNetwork();
		
		Cluster cluster = as.createCluster(createNodes(network,10), "cluster4", false);
		
		verify(drawTask).setCluster(cluster);
		verify(drawTask).run(any());
	}
	
	
	@Test
	public void testClusterRemoved(ModelManager modelManager, AnnotationRenderer renderer, EraseClusterTask eraseTask) throws Exception {
		NetworkViewSet nvs = modelManager.getActiveNetworkViewSet().get();
		AnnotationSet as = nvs.getAnnotationSets().iterator().next();
		
		Cluster cluster = as.getClusters().iterator().next();
		cluster.delete();
		
		verify(eraseTask).setCluster(cluster);
		verify(eraseTask).run(any());
	}
	
}
