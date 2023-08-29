package org.baderlab.autoannotate.internal.ui.view.display;

import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.ui.render.SignificanceLookup;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Note, this class assumes that EnrichmentMap is available at the requried version
 * and the given network is an EnrichmentMap network.
 * 
 * To ensure these preconditions hold before creating an instance of this class
 * use {@link SignificanceLookup#isEMSignificanceAvailable(CyNetwork)}
 */
public class SignificanceEMDialogAction {
	
	@Inject private Provider<JFrame> jFrameProvider;
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	
	private final CyNetwork network;
	private String dataSet;
	
	public static interface Factory {
		SignificanceEMDialogAction create(CyNetwork network, @Nullable String dataSet);
	}
	
	@Inject
	public SignificanceEMDialogAction(@Assisted CyNetwork network, @Assisted @Nullable String dataSet) {
		this.network = network;
		this.dataSet = dataSet;
	}


	private String createCommand() {
		StringBuilder command = new StringBuilder("enrichmentmap get datasets");
		command.append(" network=\"SUID:").append(network.getSUID()).append('"');
		return command.toString();
	}
	
	private List<String> getDataSetNames() {
		String command = createCommand();
		DataSetTaskObserver observer = new DataSetTaskObserver();
		var taskIterator = commandTaskFactory.createTaskIterator(observer, command);
		syncTaskManager.execute(taskIterator);
		return observer.dataSetNames;
	}
	
	private static class DataSetTaskObserver implements TaskObserver {
		List<String> dataSetNames;
		
		@SuppressWarnings("unchecked")
		@Override
		public void taskFinished(ObservableTask task) {
			dataSetNames = task.getResults(List.class);
		}
		@Override
		public void allFinished(FinishStatus finishStatus) {
		}
	}
	
	public boolean showSignificanceDialog() {
		var dataSets = getDataSetNames();
		
		var panel = new SignificanceEMPanel(dataSets).reset(dataSet);
		
		String title = BuildProperties.APP_NAME + ": Significance";
		JFrame jframe = jFrameProvider.get();
		
		int result = JOptionPane.showConfirmDialog(jframe, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null);
		if(result == JOptionPane.CANCEL_OPTION || result == JOptionPane.NO_OPTION)
			return false;
		
		this.dataSet = panel.getDataSet();
		
		return true;
	}
	
	public String getDataSet() {
		return dataSet;
	}

}
