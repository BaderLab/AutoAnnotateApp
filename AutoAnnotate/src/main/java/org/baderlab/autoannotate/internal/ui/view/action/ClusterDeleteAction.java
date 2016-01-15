package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.Cluster;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ClusterDeleteAction extends ClusterAction {

	@Inject private Provider<JFrame> jFrameProvider;
	
	public ClusterDeleteAction() {
		super("Delete");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Collection<Cluster> clusters = getClusters();
		
		String message = "Delete cluster?";
		if(clusters.size() > 1)
			message = "Delete " + clusters.size() + " clusters?";
		
		int result = JOptionPane.showConfirmDialog(jFrameProvider.get(), message, "Delete Clusters", JOptionPane.OK_CANCEL_OPTION);
		
		if(result == JOptionPane.OK_OPTION) {
			for(Cluster cluster : clusters) {
				cluster.delete();
			}
		}
	}

}
