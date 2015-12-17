package org.baderlab.autoannotate.internal.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class SessionListener implements SessionAboutToBeSavedListener, SessionLoadedListener{

	private static final String JSON_FILE_NAME = CyActivator.APP_ID + "_model.json";
	
	@Inject private Provider<ModelExporter> exporterProvider;
	@Inject private Provider<ModelImporter> importerProvider;
	@Inject private ModelManager modelManager;
	
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		String tempDir = System.getProperty("java.io.tmpdir");
		File file = new File(tempDir, JSON_FILE_NAME);
		
		ModelExporter exporter = exporterProvider.get();
		
		try(FileWriter writer = new FileWriter(file)) {
			exporter.exportJSON(writer);
			event.addAppFiles(CyActivator.APP_ID, Arrays.asList(file));
		} catch(Exception e) {
			Logger log = LoggerFactory.getLogger(CyUserLog.NAME);
			log.error(CyActivator.APP_NAME + ": Failed to save data to session.", e);
		}
	}
	
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		CySession session = event.getLoadedSession();
		
		List<File> fileList = session.getAppFileListMap().get(CyActivator.APP_ID);
		for(File file : fileList) {
			if(JSON_FILE_NAME.equals(file.getName())) {
				
				ModelImporter importer = importerProvider.get();
				
				try(FileReader reader = new FileReader(file)) {
					importer.importJSON(session::getObject, reader);
				} catch(Exception e) {
					Logger log = LoggerFactory.getLogger(CyUserLog.NAME);
					log.error(CyActivator.APP_NAME + ": Failed to restore data from session.", e);
				}
				break;
			}
		}
		
		// Remove all annotations
		modelManager.deselectAll();
		
		// print to console
		StringBuilder sb = new StringBuilder();
		exporterProvider.get().exportJSON(sb);
		System.out.println("Resulting Model:");
		System.out.println(sb.toString());
	}

	
	

}
