package org.baderlab.autoannotate.internal.ui.view.copy;

import java.awt.Component;
import java.awt.Font;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang3.tuple.Pair;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class NetworkList extends JList<CyNetworkView> {

	private List<Pair<CyNetworkView,String>> networkViews;
	
	public static interface Factory {
		NetworkList create(CyNetworkView destination);
	}
	
	@Inject
	public NetworkList(@Assisted CyNetworkView destination, CopyAnnotationsEnabler enabler, IconManager iconManager) {
		this.networkViews = enabler.getCompatibleNetworkViews(destination);
		
		setCellRenderer(new NetworkCellRenderer(iconManager));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		DefaultListModel<CyNetworkView> listModel = new DefaultListModel<>();
		networkViews.forEach(p -> listModel.addElement(p.getKey()));
		setModel(listModel);
	}
	
	
	private class NetworkCellRenderer extends DefaultListCellRenderer {

		private Font font;
		
		public NetworkCellRenderer(IconManager iconManager) {
			this.font = iconManager.getIconFont(16.0f);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			String networkName = networkViews.get(index).getValue();
			super.getListCellRendererComponent(list, networkName, index, isSelected, cellHasFocus);
			setIcon(new TextIcon(IconManager.ICON_SHARE_ALT_SQUARE, font, 16, 16));
			return this;
		}
		
	}
	
	public static String getName(CyNetworkView networkView) {
		CyNetwork network = networkView.getModel();
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}
	
}
