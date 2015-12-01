package org.baderlab.autoannotate.internal.io;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;

import org.baderlab.autoannotate.internal.CyActivator;
import org.cytoscape.application.CyUserLog;
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

	@Inject private Provider<ModelExporter> serializerProvider;
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		String fileName = CyActivator.APP_ID + "_model.json";
		String tempDir = System.getProperty("java.io.tmpdir");
		File file = new File(tempDir, fileName);
		
		ModelExporter serializer = serializerProvider.get();
		
		try(FileWriter writer = new FileWriter(file)) {
			serializer.exportJSON(writer);
			event.addAppFiles(CyActivator.APP_ID, Arrays.asList(file));
		} catch(Exception e) {
			Logger log = LoggerFactory.getLogger(CyUserLog.NAME);
			log.error(CyActivator.APP_NAME + ": Failed to save data to session.", e);
		}
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		
		
	}

	
	

}
