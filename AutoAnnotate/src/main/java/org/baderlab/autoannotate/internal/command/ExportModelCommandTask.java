package org.baderlab.autoannotate.internal.command;

import org.baderlab.autoannotate.internal.model.io.ModelTablePersistor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class ExportModelCommandTask extends AbstractTask {

	@Inject private ModelTablePersistor persistor;
	
	@Override
	public void run(TaskMonitor tm) {
		persistor.exportModel();
	}
}
