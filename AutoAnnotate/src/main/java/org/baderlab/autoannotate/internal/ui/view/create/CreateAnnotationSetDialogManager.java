package org.baderlab.autoannotate.internal.ui.view.create;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.swing.JFrame;

import org.baderlab.autoannotate.internal.ui.view.create.CreateAnnotationSetDialog.Tab;
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
	
	/**
	 * @param tab If null then the tab the user was last using will be the default (or "quick" if this is the first time).
	 */
	public void showDialog(CyNetworkView networkView, @Nullable Tab tab) {
		CreateAnnotationSetDialog dialog = dialogs.get(networkView);
		
		if(dialog == null) {
			dialog = dialogFactory.create(networkView);
			dialogs.put(networkView, dialog);
		}
		
		if(tab != null)
			dialog.setTab(tab);
		
		dialog.onShow(); // updates the column combo boxes
		
		dialog.setLocationRelativeTo(jFrameProvider.get());
		dialog.setVisible(true);
	}
	
}
