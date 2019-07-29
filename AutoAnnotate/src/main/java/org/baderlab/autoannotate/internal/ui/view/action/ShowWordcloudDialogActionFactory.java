package org.baderlab.autoannotate.internal.ui.view.action;

import com.google.inject.Inject;

public class ShowWordcloudDialogActionFactory {

	@Inject private ShowWordcloudDialogAction.Factory factory;
	
	public ShowWordcloudDialogAction createDelimitersAction() {
		return factory.create("Delimiters...", "delimiter show");
	}

	public ShowWordcloudDialogAction createWordsAction() {
		return factory.create("Excluded Words...", "ignore show");
	}
}
