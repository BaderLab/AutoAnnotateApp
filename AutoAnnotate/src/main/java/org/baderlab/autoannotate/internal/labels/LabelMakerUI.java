package org.baderlab.autoannotate.internal.labels;

import java.util.Collections;
import java.util.Map;

import javax.swing.JPanel;

public interface LabelMakerUI<C> {

	JPanel getPanel();
	
	C getContext();
	
	default Map<String,String> getParametersForDisplay(C context) {
		return Collections.emptyMap();
	}

	void reset(Object context);
	
}
