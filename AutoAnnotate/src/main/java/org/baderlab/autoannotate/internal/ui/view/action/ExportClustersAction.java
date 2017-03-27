package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class ExportClustersAction extends AbstractCyAction {

	private static final String TITLE = "Export Clusters To File";
	
	@Inject private FileUtil fileUtil;
	@Inject private Provider<JFrame> jframeProvider;
	@Inject private ModelManager modelManager;
	@Inject private DialogTaskManager dialogTaskManager;
	
	
	public ExportClustersAction() {
		super(TITLE + "...");		
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		Optional<AnnotationSet> as = modelManager.getActiveNetworkViewSet().flatMap(NetworkViewSet::getActiveAnnotationSet);
		if(as.isPresent()) {
			List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("txt Files", "txt"));
			File file = fileUtil.getFile(jframeProvider.get(), TITLE, FileUtil.SAVE, filter);
			if(file != null) {
				Task exportTask = new ExportTask(as.get(), file);
				dialogTaskManager.execute(new TaskIterator(exportTask));
			}
		}
	}
	
	private static class ExportTask extends AbstractTask {

		private final AnnotationSet as;
		private final File file;
		
		public ExportTask(AnnotationSet as, File file) {
			this.as = as;
			this.file = file;
		}
		
		@Override
		public void run(TaskMonitor tm) throws Exception {
			tm.setTitle(TITLE);
			
			List<Cluster> clusters = new ArrayList<>(as.getClusters());
			clusters.sort(Comparator.comparing(Cluster::getNodeCount));
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.append("Cluster").append("\t").append("Nodes");
				writer.newLine();
				
				for(Cluster cluster : clusters) {
					writer.append(cluster.getLabel()).append("\t").append(String.valueOf(cluster.getNodeCount()));
					writer.newLine();
				}
			}
		}
		
	}
	
}
