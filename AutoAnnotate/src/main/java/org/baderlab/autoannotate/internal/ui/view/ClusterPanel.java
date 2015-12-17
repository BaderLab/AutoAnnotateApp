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

import javax.swing.DefaultComboBoxModel;
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
import org.cytoscape.model.CyDisposable;
import org.cytoscape.util.swing.IconManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class ClusterPanel extends JPanel implements CytoPanelComponent, CyDisposable {
	
	@Inject private ModelManager modelManager;
	@Inject private Provider<IconManager> iconManagerProvider;
	@Inject private Provider<ShowCreateDialogAction> showActionProvider;
	@Inject private Provider<AnnotationSetDeleteAction> deleteActionProvider;
	@Inject private Provider<AnnotationSetRenameAction> renameActionProvider;
	@Inject private Provider<ClusterTableSelectionListener> selectionListenerProvider;
	@Inject private Provider<WordCloudAdapter> wordCloudProvider;
	
	private JComboBox<ComboItem<AnnotationSet>> annotationSetCombo;
	private JTable clusterTable;
	private ActionListener annotationSetSelectionListener;
	private ClusterTableSelectionListener clusterSelectionListener;
	
	private EventBus eventBus;
	
	@Inject
	public void registerForEvents(EventBus eventBus) {
		this.eventBus = eventBus;
		eventBus.register(this);
	}
	
	@Override
	public void dispose() {
		eventBus.unregister(this);
		eventBus = null;
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetAdded event) {
		AnnotationSet aset = event.getAnnotationSet();
		if(aset.getParent().isSelected()) {
			annotationSetCombo.addItem(new ComboItem<>(aset, aset.getName()));
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetDeleted event) {
		annotationSetCombo.removeItem(new ComboItem<>(event.getAnnotationSet()));
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetSelected event) {
		Optional<AnnotationSet> annotationSet = event.getAnnotationSet();
		if(!annotationSet.isPresent() || annotationSet.get().getParent().isSelected()) {
			annotationSetCombo.removeActionListener(annotationSetSelectionListener);
			annotationSetCombo.setSelectedItem(new ComboItem<>(annotationSet.orElse(null)));
			annotationSetCombo.addActionListener(annotationSetSelectionListener);
			updateClusterTable();
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.NetworkViewSetSelected event) {
		setNetworkViewSet(event.getNetworkViewSet());
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetChanged event) {
		AnnotationSet as = event.getAnnotationSet();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		DefaultComboBoxModel<ComboItem<AnnotationSet>> model = (DefaultComboBoxModel) annotationSetCombo.getModel();
		int index = model.getIndexOf(new ComboItem<>(as));
		
		annotationSetCombo.removeActionListener(annotationSetSelectionListener);
		model.removeElementAt(index);
		ComboItem<AnnotationSet> item = new ComboItem<>(as,as.getName());
		model.insertElementAt(item, index);
		model.setSelectedItem(item);
		annotationSetCombo.addActionListener(annotationSetSelectionListener);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClusterChanged event) {
		Cluster cluster = event.getCluster();
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		model.updateCluster(cluster);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClusterAdded event) {
		Cluster cluster = event.getCluster();
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		model.addCluster(cluster);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClusterRemoved event) {
		Cluster cluster = event.getCluster();
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		model.removeCluster(cluster);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersSelected event) {
		ListSelectionModel selectionModel = clusterTable.getSelectionModel();
		ClusterTableModel tableModel = (ClusterTableModel)clusterTable.getModel();
		
		selectionModel.removeListSelectionListener(clusterSelectionListener);
		selectionModel.clearSelection();
		for(Cluster cluster : event.getClusters()) {
			int modelIndex = tableModel.rowIndexOf(cluster);
			if(modelIndex > 0) {
				int viewIndex = clusterTable.convertRowIndexToView(modelIndex);
				selectionModel.addSelectionInterval(viewIndex, viewIndex);
			}
		}
		selectionModel.addListSelectionListener(clusterSelectionListener);
	}
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel comboPanel = createComboPanel();
		JPanel tablePanel = createTablePanel();
		
		add(comboPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
	}

	
	public void setNetworkViewSet(Optional<NetworkViewSet> nvs) {
		annotationSetCombo.removeActionListener(annotationSetSelectionListener);
		annotationSetCombo.removeAllItems();
		annotationSetCombo.addItem(new ComboItem<>(null, "(none)"));
		if(nvs.isPresent()) {
			for(AnnotationSet as : nvs.get().getAnnotationSets()) {
				annotationSetCombo.addItem(new ComboItem<>(as, as.getName()));
			}
			Optional<AnnotationSet> as = nvs.get().getActiveAnnotationSet();
			annotationSetCombo.setSelectedItem(new ComboItem<>(as.orElse(null)));
		}
		annotationSetCombo.addActionListener(annotationSetSelectionListener);
		updateClusterTable();
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
	
	
	private JPanel createComboPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		annotationSetCombo = createAnnotationSetCombo();
		annotationSetCombo.addActionListener(annotationSetSelectionListener = e -> selectAnnotationSet());
		
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
		
		clusterTable = new JTable(new ClusterTableModel()); // create with dummy model
		clusterTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		clusterTable.getColumnModel().getColumn(1).setPreferredWidth(10);
		clusterTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		clusterSelectionListener = selectionListenerProvider.get().init(clusterTable);
		clusterTable.getSelectionModel().addListSelectionListener(clusterSelectionListener);
		clusterTable.setAutoCreateRowSorter(true);
		
		JPopupMenu popupMenu = new JPopupMenu();
		ClusterTableMenuActions actions = new ClusterTableMenuActions(clusterTable, wordCloudProvider.get());
		actions.addTo(popupMenu);
		
		// Add the row that was right clicked to the selection.
		clusterTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(!e.isPopupTrigger())
					return;
				
				Point point = e.getPoint();
				int rowIndex = clusterTable.rowAtPoint(point);
				if(rowIndex < 0) 
					return;
				
				ListSelectionModel model = clusterTable.getSelectionModel();
				if(!model.isSelectedIndex(rowIndex)) {
					model.setSelectionInterval(rowIndex, rowIndex);
				}
				
				actions.updateEnablement();
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panel.add(clusterTableScroll, BorderLayout.CENTER);
		return panel;
	}

	
	private void showAnnotationSetPopupMenu(ActionEvent event) {
		JMenuItem createMenuItem = new JMenuItem("Create...");
		JMenuItem renameMenuItem = new JMenuItem("Rename");
		JMenuItem deleteMenuItem = new JMenuItem("Delete");
		
		JPopupMenu menu = new JPopupMenu();
		menu.add(createMenuItem);
		menu.add(renameMenuItem);
		menu.add(deleteMenuItem);
		
		createMenuItem.addActionListener(showActionProvider.get());
		renameMenuItem.addActionListener(renameActionProvider.get());
		deleteMenuItem.addActionListener(deleteActionProvider.get());
		
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
