package org.baderlab.autoannotate.internal.labels;

import javax.swing.JPanel;

public interface LabelMakerUI<C> {

	
	JPanel getPanel();
	
	C getContext();
	
}
