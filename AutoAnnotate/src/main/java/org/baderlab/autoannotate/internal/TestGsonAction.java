package org.baderlab.autoannotate.internal;

import java.awt.event.ActionEvent;

import org.baderlab.autoannotate.internal.io.JsonModelExporter;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class TestGsonAction extends AbstractCyAction {

	@Inject private Provider<JsonModelExporter> exporterProvider;
	
	
	public TestGsonAction() {
		super("Test Gson");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JsonModelExporter exporter = exporterProvider.get();
		StringBuilder sb = new StringBuilder();
		exporter.exportJSON(sb);
		System.out.println(sb.toString());
	}

}
