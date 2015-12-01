package org.baderlab.autoannotate.internal;

import java.awt.event.ActionEvent;

import org.baderlab.autoannotate.internal.io.ModelExporter;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;

public class TestGsonAction extends AbstractCyAction {

	@Inject private ModelExporter porter;
	
	
	public TestGsonAction() {
		super("Test Gson");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		StringBuilder sb = new StringBuilder();
		
		porter.exportJSON(sb);
		
		System.out.println(sb.toString());
		
	}

}
