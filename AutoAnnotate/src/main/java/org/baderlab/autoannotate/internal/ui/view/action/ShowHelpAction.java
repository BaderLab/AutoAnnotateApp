package org.baderlab.autoannotate.internal.ui.view.action;

import java.awt.event.ActionEvent;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.util.swing.OpenBrowser;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ShowHelpAction extends AbstractCyAction {

	@Inject private OpenBrowser openBrowser;
	
	public ShowHelpAction() {
		super("User Manual");
		insertSeparatorBefore = true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		openBrowser.openURL(BuildProperties.MANUAL_URL);
	}
}
