package org.baderlab.autoannotate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.AnnotationSetBuilder;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.AnnotationSetAdded;
import org.baderlab.autoannotate.internal.model.ModelEvents.AnnotationSetChanged;
import org.baderlab.autoannotate.internal.model.ModelEvents.AnnotationSetDeleted;
import org.baderlab.autoannotate.internal.model.ModelEvents.AnnotationSetSelected;
import org.baderlab.autoannotate.internal.model.ModelEvents.ClusterAdded;
import org.baderlab.autoannotate.internal.model.ModelEvents.ClusterChanged;
import org.baderlab.autoannotate.internal.model.ModelEvents.ClusterRemoved;
import org.baderlab.autoannotate.internal.model.ModelEvents.DisplayOptionChanged;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.eventbus.EventBus;

@RunWith(JukitoRunner.class)
public class TestModelManager {
	
	public static class Module extends JukitoModule {
		protected void configureTest() {
			bind(EventBus.class).toInstance(new EventBus());
		}
	}
	
	private EventBusTracker eventTracker;
	
	@Before
	public void setupEventBus(EventBus eventBus) {
		eventTracker = new EventBusTracker();
		eventBus.register(eventTracker);
	}
	
	@Before
	public void setupMocks(CyApplicationManager appManager) {
		when(appManager.getCurrentNetworkView()).thenReturn(mock(CyNetworkView.class));
	}
	
	
	public static List<CyNode> createNodes(int numNodes) {
		List<CyNode> nodes = Stream.generate(()->mock(CyNode.class)).limit(numNodes).collect(Collectors.toList());
		nodes.forEach(n -> when(n.getSUID()).thenReturn(SUIDFactory.getNextSUID()));
		return nodes;
	}
	
	
	@Test
	public void testGetNetworkViewSet(CyApplicationManager appManager, ModelManager modelManager) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		assertEquals(Optional.empty(), modelManager.getExistingNetworkViewSet(networkView));
		
		// register the network view with the model manager
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView); 
		
		assertNotNull(nvs);
		assertEquals(Optional.of(nvs), modelManager.getExistingNetworkViewSet(networkView));
		assertEquals(networkView, nvs.getNetworkView());
		assertTrue(modelManager.isNetworkViewSetSelected(nvs));
		assertEquals(Optional.of(nvs), modelManager.getActiveNetworkViewSet());
		assertTrue(nvs.isSelected());
	}
	
	
	@Test
	public void testNetworkViewSet(CyApplicationManager appManager, ModelManager modelManager) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView); 
		
		assertEquals(modelManager, nvs.getParent());
		assertEquals(networkView, nvs.getNetworkView());
		
		// empty
		assertEquals(Optional.empty(), nvs.getActiveAnnotationSet());
		assertTrue(nvs.getAnnotationSets().isEmpty());
		assertTrue(nvs.getAllClusters().isEmpty());
		assertTrue(eventTracker.isEmpty());
	}
	
	
	@Test
	public void testAnnotationSetLifeCycle(CyApplicationManager appManager, ModelManager modelManager) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView); 
		
		// create
		AnnotationSet as = nvs.createAnnotationSet("as_name", "lab_col");
		assertNotNull(as);
		assertEquals(nvs, as.getParent());
		assertEquals("as_name", as.getName());
		assertEquals("lab_col", as.getLabelColumn());
		assertNotNull(as.getDisplayOptions());
		assertTrue(as.getClusters().isEmpty());
		assertEquals(0, as.getClusterCount());
		assertFalse(as.hasCollapsedCluster());
		assertEquals(1, nvs.getAnnotationSets().size());
		assertEquals(as, nvs.getAnnotationSets().iterator().next());
		
		assertEquals(1, eventTracker.size());
		ModelEvents.AnnotationSetAdded addedEvent = (AnnotationSetAdded) eventTracker.popFirst();
		assertEquals(as, addedEvent.getAnnotationSet());
		
		// select
		assertEquals(Optional.empty(), nvs.getActiveAnnotationSet());
		assertFalse(as.isActive());
		nvs.select(as);
		assertTrue(as.isActive());
		assertEquals(Optional.of(as), nvs.getActiveAnnotationSet());
		assertEquals(1, eventTracker.size());
		ModelEvents.AnnotationSetSelected selectedEvent = (AnnotationSetSelected) eventTracker.popFirst();
		assertEquals(Optional.of(as), selectedEvent.getAnnotationSet());
		assertEquals(nvs, selectedEvent.getNetworkViewSet());
		
		// de-select
		nvs.select(null);
		assertFalse(as.isActive());
		assertEquals(1, eventTracker.size());
		selectedEvent = (AnnotationSetSelected) eventTracker.popFirst();
		assertEquals(Optional.empty(), selectedEvent.getAnnotationSet());
		// re-select to test that delete will de-select
		nvs.select(as);
		eventTracker.clear();
		
		// change name
		as.setName("new_name");
		assertEquals("new_name", as.getName());
		assertEquals(1, eventTracker.size());
		ModelEvents.AnnotationSetChanged changedEvent = (AnnotationSetChanged) eventTracker.popFirst();
		assertEquals(as, changedEvent.getAnnotationSet());
		
		// delete
		as.delete();
		assertFalse(as.isActive());
		assertTrue(nvs.getAnnotationSets().isEmpty());
		assertEquals(Optional.empty(), nvs.getActiveAnnotationSet());
		assertEquals(2, eventTracker.size());
		selectedEvent = (AnnotationSetSelected) eventTracker.popFirst();
		assertEquals(Optional.empty(), selectedEvent.getAnnotationSet());
		ModelEvents.AnnotationSetDeleted deletedEvent = (AnnotationSetDeleted) eventTracker.popFirst();
		assertEquals(as, deletedEvent.getAnnotationSet());
		
		// nvs no longer holds as, but as is still viable while references to it exist
		assertEquals(nvs, as.getParent());
	}
	
	
	@Test
	public void testClusterLifeCycle(CyApplicationManager appManager, ModelManager modelManager) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView); 
		AnnotationSet as = nvs.createAnnotationSet("as_name", "lab_col");
		eventTracker.clear();
		
		List<CyNode> nodes1 = createNodes(10);
		List<CyNode> nodes2 = createNodes(20);
		
		Cluster c1 = as.createCluster(nodes1, "nodes1", false);
		Cluster c2 = as.createCluster(nodes2, "nodes2", false);
		
		assertEquals(2, eventTracker.size());
		ModelEvents.ClusterAdded addedEvent = (ClusterAdded) eventTracker.popFirst();
		assertEquals(c1, addedEvent.getCluster());
		addedEvent = (ClusterAdded) eventTracker.popFirst();
		assertEquals(c2, addedEvent.getCluster());
		
		assertEquals(as, c1.getParent());
		assertEquals(as, c2.getParent());
		
		nodes1.remove(5);
		assertEquals(10, c1.getNodeCount());
		assertEquals("nodes1", c1.getLabel());
		assertEquals(20, c2.getNodeCount());
		assertEquals("nodes2", c2.getLabel());
		
		List<CyNode> nodesToRemove = nodes2.subList(0, 5);
		nodesToRemove.forEach(n->assertTrue(c2.contains(n)));
		c2.removeNodes(nodesToRemove);
		assertEquals(15, c2.getNodeCount());
		nodesToRemove.forEach(n->assertFalse(c2.contains(n)));
		assertEquals(1, eventTracker.size());
		ModelEvents.ClusterChanged changedEvent = (ClusterChanged) eventTracker.popFirst();
		assertEquals(c2, changedEvent.getCluster());
		
		c1.delete();
		assertEquals(1, as.getClusterCount());
		assertFalse(as.getClusters().contains(c1));
		assertEquals(1, eventTracker.size());
		ModelEvents.ClusterRemoved removedEvent = (ClusterRemoved) eventTracker.popFirst();
		assertEquals(c1, removedEvent.getCluster());
	}
	
	
	@Test
	public void testAnnotationSetBuilder(CyApplicationManager appManager, ModelManager modelManager) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		
		AnnotationSetBuilder builder = nvs.getAnnotationSetBuilder("as_name", "lab_col");
		builder.setBorderWidth(10);
		builder.setFontScale(20);
		builder.setOpacity(30);
		builder.setShapeType(ShapeType.RECTANGLE);
		builder.setShowClusters(false);
		builder.setShowLabels(false);
		builder.setUseConstantFontSize(true);
		
		List<CyNode> nodes1 = createNodes(10);
		builder.addCluster(nodes1, "nodes1", false);
		
		AnnotationSet as = builder.build();
		
		// One of the main reasons AnnotationSetBuilder exists is to avoid multiple
		// events that would occur using the various create() and set() methods directly
		// on the model objects.
		assertEquals(1, eventTracker.size());
		ModelEvents.AnnotationSetAdded addedEvent = (AnnotationSetAdded) eventTracker.popFirst();
		assertEquals(as, addedEvent.getAnnotationSet());
		
		assertEquals(nvs, as.getParent());
		assertEquals("as_name", as.getName());
		assertEquals("lab_col", as.getLabelColumn());
		assertFalse(as.isActive());
		
		assertEquals(1, as.getClusterCount());
		Cluster c = as.getClusters().iterator().next();
		assertEquals("nodes1", c.getLabel());
		assertEquals(as, c.getParent());
		
		DisplayOptions options = as.getDisplayOptions();
		assertEquals(10, options.getBorderWidth());
		assertEquals(20, options.getFontScale());
		assertEquals(30, options.getOpacity());
		assertEquals(ShapeType.RECTANGLE, options.getShapeType());
		assertFalse(options.isShowClusters());
		assertFalse(options.isShowLabels());
		assertTrue(options.isUseConstantFontSize());
		
		// is the builder reusable? nope
		try {
			builder.build();
			fail();
		} catch(IllegalStateException e) { }
		// try twice just to make sure
		try {
			builder.build();
			fail();
		} catch(IllegalStateException e) { }
		
		assertTrue(eventTracker.isEmpty());
	}
	
	
	@Test
	public void testDisplayOptions(CyApplicationManager appManager, ModelManager modelManager) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView); 
		AnnotationSet as = nvs.createAnnotationSet("as_name", "lab_col");
		eventTracker.clear();
		
		DisplayOptions options = as.getDisplayOptions();
		assertEquals(DisplayOptions.SHAPE_DEFAULT, options.getShapeType());
		assertEquals(DisplayOptions.SHOW_CLUSTERS_DEFAULT, options.isShowClusters());
		assertEquals(DisplayOptions.SHOW_LABELS_DEFAULT, options.isShowLabels());
		assertEquals(DisplayOptions.USE_CONSTANT_FONT_SIZE_DEFAULT, options.isUseConstantFontSize());
		assertEquals(DisplayOptions.OPACITY_DEFAULT, options.getOpacity());
		assertEquals(DisplayOptions.WIDTH_DEFAULT, options.getBorderWidth());
		assertEquals(DisplayOptions.FONT_SCALE_DEFAULT, options.getFontScale());
		
		options.setBorderWidth(20);
		options.setFontScale(30);
		options.setShapeType(ShapeType.RECTANGLE);
		
		ModelEvents.DisplayOptionChanged event;
		assertEquals(3, eventTracker.size());
		
		event = (DisplayOptionChanged) eventTracker.popFirst();
		assertEquals(options, event.getDisplayOptions());
		assertEquals(DisplayOptionChanged.Option.BORDER_WIDTH, event.getOption());
		event = (DisplayOptionChanged) eventTracker.popFirst();
		assertEquals(options, event.getDisplayOptions());
		assertEquals(DisplayOptionChanged.Option.FONT_SCALE, event.getOption());
		event = (DisplayOptionChanged) eventTracker.popFirst();
		assertEquals(options, event.getDisplayOptions());
		assertEquals(DisplayOptionChanged.Option.SHAPE_TYPE, event.getOption());
	}
	

}
