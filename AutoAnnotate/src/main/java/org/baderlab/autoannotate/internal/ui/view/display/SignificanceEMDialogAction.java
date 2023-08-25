package org.baderlab.autoannotate.internal.ui.view.display;

import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class SignificanceEMDialogAction {
	
	@Inject private Provider<JFrame> jFrameProvider;
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private AvailableCommands availableCommands;
	
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

	public boolean isApplicableToNetwork() {
		return isEMNetwork() && isCommandAvailable();
	}
	
	private boolean isEMNetwork() {
		// There's more than one way to do this. For now lets just check for a common EM column.
		return network.getDefaultNodeTable().getColumn("EnrichmentMap::Name") != null;
	}
	
	private boolean isCommandAvailable() {
		if(!availableCommands.getNamespaces().contains("enrichmentmap"))
			return false;
		if(!availableCommands.getCommands("enrichmentmap").contains("get datasets"))
			return false;
		return true;
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
