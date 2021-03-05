package org.baderlab.autoannotate.internal.ui.view.create;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Saves dialog settings so the user doesn't have to reenter data every time.
 *
 */
@Singleton
public class CreateAnnotationSetDialogManager implements NetworkViewAboutToBeDestroyedListener {
	
	@Inject private CreateAnnotationSetDialog.Factory dialogFactory;
	@Inject private Provider<JFrame> jFrameProvider;
	
	private Map<CyNetworkView,CreateAnnotationSetDialog> dialogs = new HashMap<>();

	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		dialogs.remove(e.getNetworkView());
	}
	
	public void showDialog(CyNetworkView networkView) {
		CreateAnnotationSetDialog dialog = dialogs.get(networkView);
		
		if(dialog == null) {
			dialog = dialogFactory.create(networkView);
			dialogs.put(networkView, dialog);
		}
		
		dialog.onShow();
		
		dialog.setLocationRelativeTo(jFrameProvider.get());
		dialog.setVisible(true);
	}
	
}
