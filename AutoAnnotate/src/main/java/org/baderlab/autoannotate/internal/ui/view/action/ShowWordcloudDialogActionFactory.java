package org.baderlab.autoannotate.internal.ui.view.action;

import javax.swing.JComponent;

import com.google.inject.Inject;

public class ShowWordcloudDialogActionFactory {

	@Inject private ShowWordcloudDialogAction.Factory factory;
	
	public ShowWordcloudDialogAction createDelimitersAction() {
		return factory.create("Set Delimiters...", "delimiter show").updateEnablement();
	}
	
	public ShowWordcloudDialogAction createDelimitersAction(JComponent parent) {
		return factory.create("Set Delimiters...", "delimiter show", parent).updateEnablement();
	}

	public ShowWordcloudDialogAction createWordsAction() {
		return factory.create("Set Excluded Words...", "ignore show").updateEnablement();
	}
	
	public ShowWordcloudDialogAction createWordsAction(JComponent parent) {
		return factory.create("Set Excluded Words...", "ignore show", parent).updateEnablement();
	}
}
