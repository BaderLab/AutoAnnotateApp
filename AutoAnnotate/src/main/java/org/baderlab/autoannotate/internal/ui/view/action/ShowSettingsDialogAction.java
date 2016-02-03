package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.ui.view.SettingsDialog;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowSettingsDialogAction extends AbstractCyAction {

	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private Provider<SettingsDialog> dialogProvider;
	
	public ShowSettingsDialogAction() {
		super("Settings...");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JDialog dialog = dialogProvider.get();
        dialog.setLocationRelativeTo(jFrameProvider.get());
        dialog.setVisible(true);
	}

}
