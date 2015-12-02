package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
import org.baderlab.autoannotate.internal.ui.action.DeleteAnnotationSetAction;
import org.baderlab.autoannotate.internal.ui.action.ShowCreateDialogAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.util.swing.IconManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class AnnotationSetPanel extends JPanel implements CytoPanelComponent {
	
	@Inject private ModelManager modelManager;
	@Inject private Provider<IconManager> iconManagerProvider;
	@Inject private Provider<ShowCreateDialogAction> showDialogActionProvider;
	@Inject private Provider<DeleteAnnotationSetAction> deleteSetActionProvider;
	
	private ActionListener selectListener;
	private JComboBox<ComboItem<AnnotationSet>> annotationSetCombo;
	private JTable clusterTable;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void annotationSetAdded(ModelEvents.AnnotationSetAdded event) {
		System.out.println("AnnotationSetPanel.annotationSetAdded()");
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
		annotationSetCombo.removeActionListener(selectListener);
		annotationSetCombo.setSelectedItem(new ComboItem<>(annotationSet)); // works when annotationSet is null
		annotationSetCombo.addActionListener(selectListener);
		updateClusterTable();
	}
	
	private void selectAnnotationSet() {
		int index = annotationSetCombo.getSelectedIndex();
		if(index != -1) {
			AnnotationSet annotationSet = annotationSetCombo.getItemAt(index).getValue(); // may be null
			modelManager.getActiveNetworkViewSet().select(annotationSet);
		}
	}
	
	private void updateClusterTable() {
		int index = annotationSetCombo.getSelectedIndex();
		AnnotationSet annotationSet = annotationSetCombo.getItemAt(index).getValue();
		ClusterTableModel clusterModel = new ClusterTableModel(annotationSet);
		int widthCol0 = clusterTable.getColumnModel().getColumn(0).getPreferredWidth();
		int widthCol1 = clusterTable.getColumnModel().getColumn(1).getPreferredWidth();
		clusterTable.setModel(clusterModel);
		clusterTable.getColumnModel().getColumn(0).setPreferredWidth(widthCol0);
		clusterTable.getColumnModel().getColumn(1).setPreferredWidth(widthCol1);
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel comboPanel = createComboPanel();
		JPanel tablePanel = createTablePanel();
		JPanel buttonPanel = createButtonPanel();
		
		add(comboPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private JPanel createComboPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		annotationSetCombo = createAnnotationSetCombo();
		annotationSetCombo.addActionListener(selectListener = e -> selectAnnotationSet());
		
		JButton actionButton = new JButton();
		actionButton.setFont(iconManagerProvider.get().getIconFont(12));
		actionButton.setText(IconManager.ICON_CARET_DOWN);
		actionButton.addActionListener(this::showPopupMenu);
		
		panel.add(annotationSetCombo, BorderLayout.CENTER);
		panel.add(actionButton, BorderLayout.EAST);
		return panel;
	}
	
	private JComboBox<ComboItem<AnnotationSet>> createAnnotationSetCombo() {
		JComboBox<ComboItem<AnnotationSet>> combo = new JComboBox<>();
		combo.addItem(new ComboItem<>(null, "(none)"));
		combo.setSelectedIndex(0);
		NetworkViewSet networkViewSet = modelManager.getActiveNetworkViewSet();
		if(networkViewSet != null) {
			for(AnnotationSet annotationSet : networkViewSet.getAnnotationSets()) {
				combo.addItem(new ComboItem<>(annotationSet, annotationSet.getName()));
			}
		}
		return combo;
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
		JMenuItem deleteMenuItem = new JMenuItem("Delete");
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(createMenuItem);
		menu.add(renameMenuItem);
		menu.add(deleteMenuItem);
		
		createMenuItem.addActionListener(showDialogActionProvider.get());
		deleteMenuItem.addActionListener(deleteSetActionProvider.get());
		
		Component c = (Component)event.getSource();
		menu.show(c, 0, c.getHeight());
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
