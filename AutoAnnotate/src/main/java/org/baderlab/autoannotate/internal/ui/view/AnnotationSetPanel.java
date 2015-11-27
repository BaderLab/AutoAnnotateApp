package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.ComboItem;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.IconManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@SuppressWarnings("serial")
@Singleton
public class AnnotationSetPanel extends JPanel implements CytoPanelComponent {
	
	@Inject private ModelManager modelManager;
	@Inject private IconManager iconManager;
	
	private JComboBox<ComboItem<AnnotationSet>> annotationSetCombo;
	private JTable clusterTable;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel comboPanel = createComboPanel();
		JPanel tablePanel = createTablePanel();
		JPanel buttonPanel = createButtonPanel();
		
		updateNetworkView();
		
		add(comboPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	private JPanel createComboPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		annotationSetCombo = new JComboBox<>();
		annotationSetCombo.addActionListener(e -> annotationSetSelected());
		
		JButton actionButton = new JButton();
		actionButton.setFont(iconManager.getIconFont(12));
		actionButton.setText(IconManager.ICON_CARET_DOWN);
		actionButton.addActionListener(this::showPopupMenu);
		
		panel.add(annotationSetCombo, BorderLayout.CENTER);
		panel.add(actionButton, BorderLayout.EAST);
		return panel;
	}
	
	
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		clusterTable = createClusterTable();
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panel.add(clusterTableScroll, BorderLayout.CENTER);
		return panel;
	}
	
	
	private JTable createClusterTable() {
		JTable table = new JTable(new ClusterTableModel()); // create with dummy model
		table.getColumnModel().getColumn(0).setPreferredWidth(100);
		table.getColumnModel().getColumn(1).setPreferredWidth(10);
				
		table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//table.getSelectionModel().addListSelectionListener(new ClusterTableSelctionAction(annotationSet));
		table.setAutoCreateRowSorter(true);
		return table;
	}

	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		
		JButton mergeButton = new JButton("Merge");
		JButton deleteButton = new JButton("Delete");
		
		panel.add(mergeButton);
		panel.add(deleteButton);
		return panel;
	}
	
	
	private void showPopupMenu(ActionEvent event) {
		JMenuItem createMenuItem = new JMenuItem("Create...");
		JMenuItem renameMenuItem = new JMenuItem("Rename");
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(createMenuItem);
		menu.add(renameMenuItem);
		
		Component c = (Component)event.getSource();
		menu.show(c, 0, c.getHeight());
	}
	
	
	private void updateNetworkView() {
		annotationSetCombo.removeAllItems();
		NetworkViewSet networkViewSet = modelManager.getActiveNetworkViewSet();
		if(networkViewSet != null) {
			for(AnnotationSet annotationSet : networkViewSet.getAnnotationSets()) {
				annotationSetCombo.addItem(new ComboItem<>(annotationSet, annotationSet.getName()));
			}
		}
	}
	
	
	@Subscribe
	public void annotationSetAdded(ModelEvents.AnnotationSetAdded event) {
		AnnotationSet aset = event.getAnnotationSet();
		annotationSetCombo.addItem(new ComboItem<>(aset, aset.getName()));
	}
	
	@Subscribe
	public void annotationSetDeleted(ModelEvents.AnnotationSetDeleted event) {
		annotationSetCombo.removeItem(new ComboItem<>(event.getAnnotationSet()));
	}
	
	@Subscribe
	public void annotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		AnnotationSet annotationSet = event.getAnnotationSet();
		ComboItem<AnnotationSet> item = new ComboItem<>(annotationSet);
		if(!item.equals(annotationSetCombo.getSelectedItem())) {
			annotationSetCombo.setSelectedItem(item);
		}
		annotationSetSelected();
	}
	
	private void annotationSetSelected() {
		AnnotationSet annotationSet = annotationSetCombo.getItemAt(annotationSetCombo.getSelectedIndex()).getValue();
		ClusterTableModel clusterModel = new ClusterTableModel(annotationSet);
		int widthCol0 = clusterTable.getColumnModel().getColumn(0).getPreferredWidth();
		int widthCol1 = clusterTable.getColumnModel().getColumn(1).getPreferredWidth();
		clusterTable.setModel(clusterModel);
		clusterTable.getColumnModel().getColumn(0).setPreferredWidth(widthCol0);
		clusterTable.getColumnModel().getColumn(1).setPreferredWidth(widthCol1);
	}
	
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return CyActivator.APP_NAME;
	}

	@Override
	public Icon getIcon() {
		// MKTODO
		return null;
	}

}
