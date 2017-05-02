package org.baderlab.autoannotate.internal.ui.view.dialog;

import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;

public interface TabPanel {

	AnnotationSetTaskParamters createAnnotationSetTaskParameters();
	
	default boolean isOkButtonEnabled() {
		return true;
	}
	
}
