package org.baderlab.autoannotate.internal;

import java.awt.event.ActionEvent;

import org.baderlab.autoannotate.internal.io.ModelExporter;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class TestGsonAction extends AbstractCyAction {

	@Inject private Provider<ModelExporter> exporterProvider;
	
	
	public TestGsonAction() {
		super("Test Gson");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ModelExporter exporter = exporterProvider.get();
		StringBuilder sb = new StringBuilder();
		exporter.exportJSON(sb);
		System.out.println(sb.toString());
	}

}
