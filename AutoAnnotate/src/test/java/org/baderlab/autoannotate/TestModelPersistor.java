package org.baderlab.autoannotate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.model.io.ModelTablePersistor;
import org.baderlab.autoannotate.util.LogSilenceRule;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation.ShapeType;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import com.google.common.eventbus.EventBus;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

@RunWith(JukitoRunner.class)
public class TestModelPersistor {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	public static class TestModelPersistorModule extends JukitoModule {
		@Override
		protected void configureTest() {
			NetworkTestSupport networkTestSupport = new NetworkTestSupport();
			TableTestSupport tableTestSupport = new TableTestSupport();
			bind(CyNetworkFactory.class).toInstance(networkTestSupport.getNetworkFactory());
			bind(CyNetworkTableManager.class).toInstance(networkTestSupport.getNetworkTableManager());
			bind(CyNetworkManager.class).toInstance(networkTestSupport.getNetworkManager());
			bind(CyTableFactory.class).toInstance(tableTestSupport.getTableFactory());
			
			bind(EventBus.class).toInstance(new EventBus());
			
			TypeLiteral<LabelMakerFactory<?>> labelMakerFactoryType = new TypeLiteral<LabelMakerFactory<?>>() {};
			Multibinder<LabelMakerFactory<?>> labelFactoryBinder = Multibinder.newSetBinder(binder(), labelMakerFactoryType);
			labelFactoryBinder.addBinding().toInstance(mock(LabelMakerFactory.class));
		}
	}
	
	@Before
	public void setup(CyNetworkFactory networkFactory, CyNetworkManager networkManager, CyNetworkViewManager viewManager, 
			          CyApplicationManager appManager, ModelManager modelManager, Set<LabelMakerFactory<?>> factoryPlugIns) {
		// Set up mocks
		CyNetwork network = networkFactory.createNetwork();
		networkManager.addNetwork(network);
		
		CyNetworkView networkView = mock(CyNetworkView.class);
		when(networkView.getModel()).thenReturn(network);
		when(appManager.getCurrentNetworkView()).thenReturn(networkView);
		when(viewManager.getNetworkViews(network)).thenReturn(Collections.singleton(networkView));
		when(factoryPlugIns.iterator().next().getID()).thenReturn("nullFactory");
		
		// Set up model
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		
		AnnotationSet as1 = nvs.createAnnotationSet("as1", "lc1");
		AnnotationSet as2 = nvs.createAnnotationSet("as2", "lc2");
		
		nvs.select(as1);
		
		DisplayOptions options = as2.getDisplayOptions();
		options.setBorderWidth(10);
		options.setFontScale(20);
		options.setOpacity(30);
		options.setShapeType(ShapeType.RECTANGLE);
		options.setShowClusters(false);
		options.setShowLabels(false);
		options.setUseConstantFontSize(true);
		
		List<CyNode> nodes1 = new ArrayList<>(10);
		for(int i = 0; i < 10; i++) {
			nodes1.add(network.addNode());
		}
		List<CyNode> nodes2 = new ArrayList<>(20);
		for(int i = 0; i < 20; i++) {
			nodes2.add(network.addNode());
		}
		
		as2.createCluster(nodes1, "cluster1", false);
		as2.createCluster(nodes2, "cluster2", false);
	}
	
	
	@Test
	public void testExportThenImport(CyApplicationManager appManager, ModelManager modelManager, ModelTablePersistor persistor) {
		CyNetworkView networkView = appManager.getCurrentNetworkView();
		assertEquals(1, modelManager.getNetworkViewSets().size());
		
		// We will export the model, then import it back, and the result should be the same as what we started with.
		persistor.exportModel();

		// clear the manager
		NetworkViewAboutToBeDestroyedEvent event = new NetworkViewAboutToBeDestroyedEvent(mock(CyNetworkViewManager.class), networkView);
		modelManager.handleEvent(event);
		assertEquals(0, modelManager.getNetworkViewSets().size());
		
		persistor.importModel();

		assertEquals(1, modelManager.getNetworkViewSets().size());
		
		NetworkViewSet nvs = modelManager.getNetworkViewSet(networkView);
		List<AnnotationSet> annotationSets = new ArrayList<>(nvs.getAnnotationSets());
		annotationSets.sort(Comparator.comparing(AnnotationSet::getName));
		assertEquals(2, annotationSets.size());
		
		AnnotationSet as1 = annotationSets.get(0);
		assertEquals("as1", as1.getName());
		assertEquals("lc1", as1.getLabelColumn());
		assertTrue(as1.isActive());
		
		AnnotationSet as2 = annotationSets.get(1);
		assertEquals("as2", as2.getName());
		assertEquals("lc2", as2.getLabelColumn());
		assertFalse(as2.isActive());
		
		DisplayOptions options = as2.getDisplayOptions();
		assertEquals(10, options.getBorderWidth());
		assertEquals(20, options.getFontScale());
		assertEquals(30, options.getOpacity());
		assertEquals(ShapeType.RECTANGLE, options.getShapeType());
		assertFalse(options.isShowClusters());
		assertFalse(options.isShowLabels());
		assertTrue(options.isUseConstantFontSize());
		
		List<Cluster> clusters = new ArrayList<>(as2.getClusters());
		clusters.sort(Comparator.comparing(Cluster::getLabel));
		
		assertEquals(2, clusters.size());
		
		assertEquals("cluster1", clusters.get(0).getLabel());
		assertEquals(10, clusters.get(0).getNodes().size());
		assertEquals("cluster2", clusters.get(1).getLabel());
		assertEquals(20, clusters.get(1).getNodes().size());
	}
}
