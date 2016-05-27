package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.ui.view.CreationParamsDialog;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowCreationParamsAction extends AbstractCyAction {

	@Inject private Provider<CreationParamsDialog> dialogProvider;
	@Inject private Provider<JFrame> jFrameProvider;
	
	public ShowCreationParamsAction() {
		super("Display Creation Parameters...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		CreationParamsDialog dialog = dialogProvider.get();
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(jFrameProvider.get());
		dialog.setVisible(true);
	}

}
