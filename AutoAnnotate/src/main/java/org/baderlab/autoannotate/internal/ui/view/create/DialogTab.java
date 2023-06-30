package org.baderlab.autoannotate.internal.ui.view.create;

import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;

public interface DialogTab {

	AnnotationSetTaskParamters createAnnotationSetTaskParameters();
	
	boolean isOkButtonEnabled();

	void resetButtonPressed();
	
	void onShow();
}
