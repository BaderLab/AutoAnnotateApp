package org.baderlab.autoannotate.internal.ui.render;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_FACE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.DisplayOptions.FillType;
import org.baderlab.autoannotate.internal.model.SignificanceOptions.Highlight;
import org.baderlab.autoannotate.internal.util.HiddenTools;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class DrawClustersTask extends AbstractTask {

	@Inject private AnnotationFactory<TextAnnotation> textFactory;
	@Inject private AnnotationFactory<ShapeAnnotation> shapeFactory;
	@Inject private VisualMappingManager visualMappingManager;
	
	@Inject private AnnotationManager annotationManager;
	@Inject private AnnotationRenderer annotationRenderer;
	@Inject private SignificanceLookup significanceLookup;
	
	private final Collection<Cluster> clusters;
	
	
	public static interface Factory {
		DrawClustersTask create(Collection<Cluster> clusters);
		DrawClustersTask create(Cluster cluster);
	}
	
	
	@AssistedInject
	public DrawClustersTask(@Assisted Collection<Cluster> clusters) {
		this.clusters = clusters;
	}
	
	@AssistedInject
	public DrawClustersTask(@Assisted Cluster cluster) {
		this.clusters = Collections.singleton(cluster);
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle(BuildProperties.APP_NAME);
		taskMonitor.setStatusMessage("Drawing Annotations");
		if(clusters.isEmpty())
			return;
		
		var allAnnotations = createAnnotations();
		createHighlights();
		
		if(!allAnnotations.isEmpty())
			annotationManager.addAnnotations(allAnnotations);
	}
	
	
	private List<Annotation> createAnnotations() {
		List<Annotation> allAnnotations = new ArrayList<>();
		var significanceColors = getSignificanceColors(); // may be null
		
		for(var cluster : clusters) {
			if(!cluster.isCollapsed() && !HiddenTools.allNodesHidden(cluster)) {
				boolean isSelected = annotationRenderer.isSelected(cluster);
				AnnotationGroup group = createClusterAnnotations(cluster, isSelected, significanceColors);
				annotationRenderer.putAnnotations(cluster, group);
				allAnnotations.addAll(group.getAnnotations());
			}
		}
		return allAnnotations;
	}
	
	private Map<Cluster,Color> getSignificanceColors() {
		// Assume all clusters are from the same annotation set
		AnnotationSet as = clusters.iterator().next().getParent();
		if(as.getDisplayOptions().getFillType() == FillType.SIGNIFICANT) {
			return significanceLookup.getColors(as);
		}
		return null;
	}
	
	static Color getSelectionColor(VisualMappingManager visualMappingManager, CyNetworkView netView) {
		VisualStyle vs = visualMappingManager.getVisualStyle(netView);
		if(vs == null)
			return Color.YELLOW;
		Paint p = vs.getDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT);
		if(p instanceof Color)
			return (Color) p;
		return null;
	}
	
	private AnnotationGroup createClusterAnnotations(Cluster cluster, boolean isSelected, Map<Cluster,Color> significanceColors) {
		AnnotationSet annotationSet = cluster.getParent();
		CyNetworkView networkView = annotationSet.getParent().getNetworkView();
		
		Color selection = getSelectionColor(visualMappingManager, networkView);
		ArgsShape shapeArgs = ArgsShape.createFor(cluster, isSelected, selection, significanceColors);
		ShapeAnnotation shape = shapeFactory.createAnnotation(ShapeAnnotation.class, networkView, shapeArgs.getArgMap());
		
		List<ArgsLabel> labelArgsList = ArgsLabel.createFor(shapeArgs, cluster, isSelected, selection);
		
		if(annotationSet.getDisplayOptions().isUseWordWrap()) {
			int n = labelArgsList.size();
			for(int i = 0; i < n; i++) {
				labelArgsList.get(i).setIndexOfTotal(i+1, n);
			}
		}
		
		List<TextAnnotation> textAnnotations = new ArrayList<>(labelArgsList.size());
		
		for(ArgsLabel labelArgs : labelArgsList) {
			Map<String, String> argMap = labelArgs.getArgMap();
			TextAnnotation text = textFactory.createAnnotation(TextAnnotation.class, networkView, argMap);
			textAnnotations.add(text);
		}
		
		return new AnnotationGroup(shape, textAnnotations);
	}
	
	
	private void createHighlights() {
		var annotationSet = clusters.iterator().next().getParent();
		var highlight = annotationSet.getDisplayOptions().getSignificanceOptions().getHighlight();
		
		if(highlight == Highlight.BOLD_LABEL) {
			var sigNodes = significanceLookup.getMostSignificantNodes(annotationSet);
			for(var cluster : clusters) {
				var mostSigNode = sigNodes.get(cluster);
				if(mostSigNode != null) {
					highlightLabel(cluster, mostSigNode);
				}
			}
		}
	}
	
	private void highlightLabel(Cluster cluster, CyNode sigNode) {
		var nodeView = cluster.getNetworkView().getNodeView(sigNode);
		if(nodeView == null)
			return;
		
		var fontSize = getVP(nodeView, NODE_LABEL_FONT_SIZE);
		var fontFace = getVP(nodeView, NODE_LABEL_FONT_FACE);
		
		fontSize += 4;
		fontFace = fontFace.deriveFont(Font.BOLD);
		
		cluster.setHighlightedNode(sigNode.getSUID());
		
		nodeView.setLockedValue(NODE_LABEL_FONT_SIZE, fontSize);
		nodeView.setLockedValue(NODE_LABEL_FONT_FACE, fontFace);
	}
	
	private static <T> T getVP(View<CyNode> nodeView, VisualProperty<T> vp) {
		var value = nodeView.getVisualProperty(vp);
		if(value == null)
			value = vp.getDefault();
		return value;
	}
	
}
