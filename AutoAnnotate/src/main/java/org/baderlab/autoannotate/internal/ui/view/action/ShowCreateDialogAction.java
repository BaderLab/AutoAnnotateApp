package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialog.Tab;
import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialogManager;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowCreateDialogAction extends AbstractCyAction implements TaskFactory {

	public static final String TITLE = "New Annotation Set...";
	
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<CreateAnnotationSetDialogManager> dialogManagerProvider;
	
	public ShowCreateDialogAction() {
		super(TITLE);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AbstractTask() {
			public void run(TaskMonitor tm) {
				show(null);
			}
		});
	} 
	
	@Override
	public void actionPerformed(ActionEvent e) {
		show(null);
	}
	
	public void show(@Nullable Tab tab) {
		CyNetworkView networkView = applicationManager.getCurrentNetworkView();
		
		SwingUtil.invokeOnEDT(() -> {
			if(networkView == null) {
				JOptionPane.showMessageDialog(jFrameProvider.get(), 
					"Please select a network view first.", BuildProperties.APP_NAME, JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			CreateAnnotationSetDialogManager manager = dialogManagerProvider.get();
			manager.showDialog(networkView, tab);
		});
	}


	@Override
	public boolean isReady() {
		return true;
	}
	
}
