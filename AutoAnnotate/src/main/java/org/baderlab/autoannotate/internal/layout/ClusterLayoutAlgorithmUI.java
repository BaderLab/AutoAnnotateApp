package org.baderlab.autoannotate.internal.layout;

import javax.swing.JPanel;

public interface ClusterLayoutAlgorithmUI<C> {

	JPanel getPanel();
	
	C getContext();
	
	void reset(C context);
	
}
