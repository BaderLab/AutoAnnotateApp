package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.ui.view.AboutDialog;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowAboutDialogAction extends AbstractCyAction {

	@Inject Provider<AboutDialog> dialogProvider;
	@Inject Provider<JFrame> jFrameProvider;
	
	public ShowAboutDialogAction() {
		super("About...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AboutDialog aboutDialog = dialogProvider.get();
		aboutDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutDialog.pack();
		aboutDialog.setLocationRelativeTo(jFrameProvider.get());
		aboutDialog.setVisible(true);
	}

}
