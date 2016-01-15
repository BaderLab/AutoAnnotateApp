package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.autoannotate.internal.model.Cluster;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ClusterRenameAction extends ClusterAction {
	
	@Inject private Provider<JFrame> jFrameProvider;
	
	public ClusterRenameAction() {
		super("Rename...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Collection<Cluster> clusters = getClusters();
		if(clusters.isEmpty())
			return;
		
		Cluster cluster = clusters.iterator().next();
		Object result = JOptionPane.showInputDialog(jFrameProvider.get(), "Cluster Label", "Rename Cluster", JOptionPane.PLAIN_MESSAGE, null, null, cluster.getLabel());
		if(result == null)
			return;
		String label = result.toString().trim();
		cluster.setLabel(label);
	}
}
