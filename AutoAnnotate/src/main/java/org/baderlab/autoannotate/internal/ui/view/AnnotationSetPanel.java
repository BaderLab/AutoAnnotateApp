package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.WordCloudAdapter;
import org.baderlab.autoannotate.internal.ui.ComboItem;
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
	@Inject private Provider<ClusterTableSelectionListener> clusterTableSelectionListenerProvider;
	@Inject private Provider<WordCloudAdapter> wordCloudAdapterProvider;
	
	private ActionListener selectListener;
	private JComboBox<ComboItem<AnnotationSet>> annotationSetCombo;
	private JTable clusterTable;
	
	
	@Inject
	public void listenToModelEvents(EventBus eventBus) {
		eventBus.register(this);
	}
	
	@Subscribe
	public void handleAnnotationSetAdded(ModelEvents.AnnotationSetAdded event) {
		AnnotationSet aset = event.getAnnotationSet();
		if(aset.getParent().isSelected()) {
			annotationSetCombo.addItem(new ComboItem<>(aset, aset.getName()));
		}
	}
	
	@Subscribe
	public void handleAnnotationSetDeleted(ModelEvents.AnnotationSetDeleted event) {
		annotationSetCombo.removeItem(new ComboItem<>(event.getAnnotationSet()));
	}
	
	@Subscribe
	public void handleAnnotationSetSelected(ModelEvents.AnnotationSetSelected event) {
		Optional<AnnotationSet> annotationSet = event.getAnnotationSet();
		if(!annotationSet.isPresent() || annotationSet.get().getParent().isSelected()) {
			annotationSetCombo.removeActionListener(selectListener);
			annotationSetCombo.setSelectedItem(new ComboItem<>(annotationSet.orElse(null)));
			annotationSetCombo.addActionListener(selectListener);
			updateClusterTable();
		}
	}
	
	@Subscribe
	public void handleNetworkViewSetSelected(ModelEvents.NetworkViewSetSelected event) {
		annotationSetCombo.removeActionListener(selectListener);
		annotationSetCombo.removeAllItems();
		annotationSetCombo.addItem(new ComboItem<>(null, "(none)"));
		NetworkViewSet nvs = event.getNetworkViewSet();
		if(nvs != null) {
			for(AnnotationSet as : nvs.getAnnotationSets()) {
				annotationSetCombo.addItem(new ComboItem<>(as, as.getName()));
			}
			Optional<AnnotationSet> as = nvs.getActiveAnnotationSet();
			annotationSetCombo.setSelectedItem(new ComboItem<>(as.orElse(null)));
		}
		annotationSetCombo.addActionListener(selectListener);
		updateClusterTable();
	}
	
	@Subscribe
	public void handleClusterChanged(ModelEvents.ClusterChanged event) {
		Cluster cluster = event.getCluster();
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		model.updateCluster(cluster);
	}
	
	@Subscribe
	public void handleClusterAdded(ModelEvents.ClusterAdded event) {
		Cluster cluster = event.getCluster();
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		model.addCluster(cluster);
	}
	
	@Subscribe
	public void handleClusterRemoved(ModelEvents.ClusterRemoved event) {
		Cluster cluster = event.getCluster();
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		model.removeCluster(cluster);
	}
	
	
	private void selectAnnotationSet() {
		int index = annotationSetCombo.getSelectedIndex();
		if(index != -1) {
			AnnotationSet annotationSet = annotationSetCombo.getItemAt(index).getValue(); // may be null
			if(annotationSet != null) {
				NetworkViewSet networkViewSet = annotationSet.getParent();
				networkViewSet.select(annotationSet);
			} else if(annotationSetCombo.getItemCount() > 1) {
				annotationSet = annotationSetCombo.getItemAt(1).getValue();
				NetworkViewSet networkViewSet = annotationSet.getParent();
				networkViewSet.select(null);
			}
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
		
		// sort
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(clusterTable.getModel());
		clusterTable.setRowSorter(sorter);
		List<SortKey> sortKeys = new ArrayList<>(2);
		sortKeys.add(new RowSorter.SortKey(ClusterTableModel.NODES_COLUMN_INDEX, SortOrder.DESCENDING));
		sortKeys.add(new RowSorter.SortKey(ClusterTableModel.CLUSTER_COLUMN_INDEX, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel comboPanel = createComboPanel();
		JPanel tablePanel = createTablePanel();
		
		add(comboPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
	}

	
	private JPanel createComboPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		annotationSetCombo = createAnnotationSetCombo();
		annotationSetCombo.addActionListener(selectListener = e -> selectAnnotationSet());
		
		JButton actionButton = new JButton();
		actionButton.setFont(iconManagerProvider.get().getIconFont(12));
		actionButton.setText(IconManager.ICON_CARET_DOWN);
		actionButton.addActionListener(this::showAnnotationSetPopupMenu);
		
		panel.add(annotationSetCombo, BorderLayout.CENTER);
		panel.add(actionButton, BorderLayout.EAST);
		return panel;
	}
	
	private JComboBox<ComboItem<AnnotationSet>> createAnnotationSetCombo() {
		JComboBox<ComboItem<AnnotationSet>> combo = new JComboBox<>();
		combo.addItem(new ComboItem<>(null, "(none)"));
		combo.setSelectedIndex(0);
		Optional<NetworkViewSet> nvs = modelManager.getActiveNetworkViewSet();
		if(nvs.isPresent()) {
			for(AnnotationSet annotationSet : nvs.get().getAnnotationSets()) {
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
		table.getColumnModel().getColumn(0).setPreferredWidth(200);
		table.getColumnModel().getColumn(1).setPreferredWidth(10);
		table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		ClusterTableSelectionListener selectionListener = clusterTableSelectionListenerProvider.get().init(table);
		table.getSelectionModel().addListSelectionListener(selectionListener);
		table.setAutoCreateRowSorter(true);
		
		JPopupMenu popupMenu = new JPopupMenu();
		ClusterTableMenuActions actions = new ClusterTableMenuActions(table, wordCloudAdapterProvider.get());
		actions.addTo(popupMenu);
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(!e.isPopupTrigger())
					return;
				
				Point point = e.getPoint();
				int rowIndex = table.rowAtPoint(point);
				if(rowIndex < 0) 
					return;
				
				ListSelectionModel model = table.getSelectionModel();
				if(!model.isSelectedIndex(rowIndex)) {
					model.setSelectionInterval(rowIndex, rowIndex);
				}
				
				actions.updateEnablement();
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		return table;
	}

	
	private void showAnnotationSetPopupMenu(ActionEvent event) {
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
