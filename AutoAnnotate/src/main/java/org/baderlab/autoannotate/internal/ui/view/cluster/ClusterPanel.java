package org.baderlab.autoannotate.internal.ui.view.cluster;

import static org.baderlab.autoannotate.internal.util.TaskTools.allFinishedObserver;
import static org.baderlab.autoannotate.internal.util.TaskTools.taskOf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.BuildProperties;
import org.baderlab.autoannotate.internal.CyActivator;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.Cluster;
import org.baderlab.autoannotate.internal.model.ModelEvents;
import org.baderlab.autoannotate.internal.model.ModelEvents.NetworkViewSetChanged.Type;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CollapseAllTaskFactory;
import org.baderlab.autoannotate.internal.task.Grouping;
import org.baderlab.autoannotate.internal.ui.view.action.RedrawAction;
import org.baderlab.autoannotate.internal.ui.view.action.ShowCreateDialogAction;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class ClusterPanel extends JPanel implements CytoPanelComponent, CyDisposable {
	
	@Inject private ModelManager modelManager;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private IconManager iconManager;

	@Inject private CollapseAllTaskFactory.Factory collapseTaskFactoryFactory;
	@Inject private Provider<ClusterTableSelectionListener> selectionListenerProvider;
	@Inject private Provider<AnnotationSetMenu> annotationSetMenuProvider;
	@Inject private Provider<ClusterMenu> clusterMenuProvider;
	@Inject private Provider<ShowCreateDialogAction> showActionProvider;
	@Inject private Provider<RedrawAction> redrawActionProvider;
	@Inject private ClusterSignificancePanel clusterSignificancePanel;
	
	private JComboBox<ComboItem<AnnotationSet>> annotationSetCombo;
	private JTable clusterTable;
	private ItemListener itemListener;
	private ClusterTableSelectionListener clusterSelectionListener;
	private ListSelectionListener clusterThumbnailListener;
	
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
			annotationSetCombo.removeItemListener(itemListener);
			annotationSetCombo.setSelectedItem(new ComboItem<>(annotationSet.orElse(null)));
			annotationSetCombo.addItemListener(itemListener);
			updateClusterTable();
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.NetworkViewSetSelected event) {
		setNetworkViewSet(event.getNetworkViewSet());
	}
	
	@Subscribe
	public void handle(ModelEvents.NetworkViewSetChanged event) {
		if(event.getChangeType() == Type.ANNOTATION_SET_ORDER) {
			setNetworkViewSet(Optional.of(event.getNetworkViewSet())); // refreshes the panel
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.AnnotationSetChanged event) {
		AnnotationSet as = event.getAnnotationSet();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		DefaultComboBoxModel<ComboItem<AnnotationSet>> model = (DefaultComboBoxModel) annotationSetCombo.getModel();
		int index = model.getIndexOf(new ComboItem<>(as));
		
		annotationSetCombo.removeItemListener(itemListener);
		boolean select = annotationSetCombo.getSelectedIndex() == index;
		model.removeElementAt(index);
		ComboItem<AnnotationSet> item = new ComboItem<>(as,as.getName());
		model.insertElementAt(item, index);
		if(select) {
			model.setSelectedItem(item);
		}
		annotationSetCombo.addItemListener(itemListener);
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersChanged event) {
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		for(Cluster cluster : event.getClusters()) {
			model.updateCluster(cluster); // does nothing if the cluster isn't in the table
		}
	}
	
	@Subscribe
	public void handle(ModelEvents.ClustersLabelsUpdated event) {
		ClusterTableModel model = (ClusterTableModel) clusterTable.getModel();
		for(Cluster cluster : event.getAnnotationSet().getClusters()) {
			model.updateCluster(cluster); // does nothing if the cluster isn't in the table
		}
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
	public void handle(ModelEvents.ClusterSelectedInNetwork event) {
		// Fired when the user explicitly selects a cluster in the network view using the context menu.
		Cluster cluster = event.getCluster();
		
		ListSelectionModel selectionModel = clusterTable.getSelectionModel();
		ClusterTableModel tableModel = (ClusterTableModel)clusterTable.getModel();
		
		int modelIndex = tableModel.rowIndexOf(cluster);
		if(modelIndex >= 0) {
			selectionModel.removeListSelectionListener(clusterSelectionListener);
			selectionModel.removeListSelectionListener(clusterThumbnailListener);
			
			int viewIndex = clusterTable.convertRowIndexToView(modelIndex);
			selectionModel.addSelectionInterval(viewIndex, viewIndex);
			
			selectionModel.addListSelectionListener(clusterSelectionListener);
			selectionModel.addListSelectionListener(clusterThumbnailListener);
		}
		
		clusterThumbnailListener.valueChanged(null);
	}
	
//	@Subscribe
//	public void handle(ModelEvents.ClustersSelected event) {
//		ListSelectionModel selectionModel = clusterTable.getSelectionModel();
//		ClusterTableModel tableModel = (ClusterTableModel)clusterTable.getModel();
//		
//		selectionModel.removeListSelectionListener(clusterSelectionListener);
//		selectionModel.removeListSelectionListener(clusterThumbnailListener);
//		
//		selectionModel.clearSelection();
//		for(Cluster cluster : event.getClusters()) {
//			int modelIndex = tableModel.rowIndexOf(cluster);
//			if(modelIndex >= 0) {
//				int viewIndex = clusterTable.convertRowIndexToView(modelIndex);
//				selectionModel.addSelectionInterval(viewIndex, viewIndex);
//			}
//		}
//		
//		selectionModel.addListSelectionListener(clusterSelectionListener);
//		selectionModel.addListSelectionListener(clusterThumbnailListener);
//		
//		clusterThumbnailListener.valueChanged(null);
//	}
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JPanel comboPanel = createComboPanel();
		JPanel tablePanel = createTablePanel();
		JPanel signfPanel = clusterSignificancePanel;
		
		signfPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		add(comboPanel, BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
		add(signfPanel, BorderLayout.SOUTH);
	}

	
	public void setNetworkViewSet(Optional<NetworkViewSet> nvs) {
		annotationSetCombo.removeItemListener(itemListener);
		annotationSetCombo.removeAllItems();
		annotationSetCombo.addItem(new ComboItem<>(null, "(none)"));
		if(nvs.isPresent()) {
			for(AnnotationSet as : nvs.get().getAnnotationSets()) {
				annotationSetCombo.addItem(new ComboItem<>(as, as.getName()));
			}
			Optional<AnnotationSet> as = nvs.get().getActiveAnnotationSet();
			annotationSetCombo.setSelectedItem(new ComboItem<>(as.orElse(null)));
		}
		annotationSetCombo.addItemListener(itemListener);
		updateClusterTable();
	}
	
	private void selectAnnotationSet() {
		int index = annotationSetCombo.getSelectedIndex();
		if(index != -1) {
			AnnotationSet annotationSet = annotationSetCombo.getItemAt(index).getValue(); // may be null
			if(annotationSet != null) {
				NetworkViewSet networkViewSet = annotationSet.getParent();
				runSelectAnnotationSetTasks(networkViewSet, annotationSet);
			} else if(annotationSetCombo.getItemCount() > 1) {
				NetworkViewSet networkViewSet = annotationSetCombo.getItemAt(1).getValue().getParent();
				runSelectAnnotationSetTasks(networkViewSet, null);
			}
		}
	}
	
	
	private void runSelectAnnotationSetTasks(NetworkViewSet networkViewSet, AnnotationSet toSelect) {
		// Disable the combo so the user can't somehow start a new task while this one is still running
		annotationSetCombo.setEnabled(false);
		
		TaskIterator tasks = new TaskIterator();
		
		// Expand all the groups
		CollapseAllTaskFactory collapseAllTaskFactory = collapseTaskFactoryFactory.create(Grouping.EXPAND, networkViewSet);
		tasks.append(collapseAllTaskFactory.createTaskIterator());
		
		// Select the annotation set (fires event that redraws annotations)
		tasks.append(taskOf(() -> networkViewSet.select(toSelect)));
		
		// Enable the combo box
		TaskObserver observer = allFinishedObserver(() -> annotationSetCombo.setEnabled(true));
		
		dialogTaskManager.execute(tasks, observer);
	}
	
	
	private void updateClusterTable() {
		int index = annotationSetCombo.getSelectedIndex();
		AnnotationSet annotationSet = annotationSetCombo.getItemAt(index).getValue();
		ClusterTableModel clusterModel = new ClusterTableModel(annotationSet);
		
		int widths[] = getColumnWidths(clusterTable);
		clusterTable.setModel(clusterModel);
		setColumnWidths(clusterTable, widths);
		
		var colModel = clusterTable.getColumnModel();
		colModel.getColumn(ClusterTableModel.NODES_COL).setCellRenderer(new ClusterTableNodesCellRenderer());
		colModel.getColumn(ClusterTableModel.COLLAPSED_COL).setCellRenderer(new ClusterTableCollapsedCellRenderer(iconManager));
		
		// sort
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(clusterTable.getModel());
		clusterTable.setRowSorter(sorter);
		sorter.setComparator(ClusterTableModel.NODES_COL, Comparator.naturalOrder());
		List<SortKey> sortKeys = new ArrayList<>(2);
		sortKeys.add(new RowSorter.SortKey(ClusterTableModel.NODES_COL,   SortOrder.DESCENDING));
		sortKeys.add(new RowSorter.SortKey(ClusterTableModel.CLUSTER_COL, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		sorter.sort();
	}
	
	
	private static int[] getColumnWidths(JTable table) {
		int n = table.getColumnModel().getColumnCount();
		int[] widths = new int[n];
		for(int i = 0; i < n; i++) {
			widths[i] = table.getColumnModel().getColumn(i).getPreferredWidth();
		}
		return widths;
	}
	
	
	private static void setColumnWidths(JTable table, int... widths) {
		int n = table.getColumnModel().getColumnCount();
		for(int i = 0; i < n; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
		}
	}
	
	
	
	private JPanel createComboPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		annotationSetCombo = createAnnotationSetCombo();
		annotationSetCombo.addItemListener(itemListener = e -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				selectAnnotationSet();
			}
		});
		
		JButton addButton    = SwingUtil.createIconButton(iconManager, IconManager.ICON_PLUS, "Create Annotation Set");
		JButton redrawButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_REFRESH, "Redraw Annotations");
		JButton menuButton   = SwingUtil.createIconButton(iconManager, IconManager.ICON_BARS, "Menu");
		
		addButton.addActionListener(showActionProvider.get());
		redrawButton.addActionListener(redrawActionProvider.get());
		menuButton.addActionListener(this::showAnnotationSetPopupMenu);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(addButton);
		buttonPanel.add(redrawButton);
		buttonPanel.add(menuButton);
		
		panel.add(annotationSetCombo, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.EAST);
		return panel;
	}
	
	private JComboBox<ComboItem<AnnotationSet>> createAnnotationSetCombo() {
		JComboBox<ComboItem<AnnotationSet>> combo = new JComboBox<>();
		combo.addItem(new ComboItem<>(null, "(none)"));
		combo.setSelectedIndex(0);
		
		modelManager.getActiveNetworkViewSet().ifPresent(nvs -> {
			for(AnnotationSet annotationSet : nvs.getAnnotationSets()) {
				combo.addItem(new ComboItem<>(annotationSet, annotationSet.getName()));
			}
		});
		
		return combo;
	}
	
	
	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		clusterTable = new JTable(new ClusterTableModel()); // create with dummy model
		setColumnWidths(clusterTable, 190, 20, 20);
		clusterTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		clusterSelectionListener = selectionListenerProvider.get().init(clusterTable);
		clusterTable.getSelectionModel().addListSelectionListener(clusterSelectionListener);
		clusterTable.getSelectionModel().addListSelectionListener(clusterThumbnailListener = e -> {
			clusterSignificancePanel.updateCluster(getSelectedCluster());
		});
		clusterTable.setAutoCreateRowSorter(true);
		
		var colModel = clusterTable.getColumnModel();
		colModel.getColumn(ClusterTableModel.NODES_COL).setCellRenderer(new ClusterTableNodesCellRenderer());
		colModel.getColumn(ClusterTableModel.COLLAPSED_COL).setCellRenderer(new ClusterTableCollapsedCellRenderer(iconManager));
		
		clusterTable.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) { showPopup(e); }
			@Override public void mouseReleased(MouseEvent e) { showPopup(e); }
			
			private void showPopup(MouseEvent e) {
				if(!e.isPopupTrigger())
					return;
				
				// Add the row that was right clicked to the selection.
				int rowIndex = clusterTable.rowAtPoint(e.getPoint());
				if(rowIndex < 0) 
					return;
				ListSelectionModel model = clusterTable.getSelectionModel();
				if(!model.isSelectedIndex(rowIndex)) {
					model.setSelectionInterval(rowIndex, rowIndex);
				}
				
				showClusterPopupMenu(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panel.add(clusterTableScroll, BorderLayout.CENTER);
		return panel;
	}

	
	private Cluster getSelectedCluster() {
		int rowIndex = clusterTable.getSelectedRow();
		if(rowIndex >= 0) {
			var model = (ClusterTableModel) clusterTable.getModel();
			int modelIndex = clusterTable.convertRowIndexToModel(rowIndex);
			return model.getCluster(modelIndex);
		}
		return null;
	}
	
	
	private List<Cluster> getSelectedClusters() {
		var model = (ClusterTableModel) clusterTable.getModel();
		int[] rows = clusterTable.getSelectedRows();
		List<Cluster> clusters = new ArrayList<>(rows.length);
		for(int i : rows) {
			int modelIndex = clusterTable.convertRowIndexToModel(i);
			Cluster cluster = model.getCluster(modelIndex);
			clusters.add(cluster);
		}
		return clusters;
	}
	
	private Optional<AnnotationSet> getCurrentAnnotationSet() {
		Optional<AnnotationSet> as = Optional.empty();
		int index = annotationSetCombo.getSelectedIndex();
		if(index != -1) {
			as = Optional.ofNullable(annotationSetCombo.getItemAt(index).getValue()); // may be null
		}
		return as;
	}
	
	private void showAnnotationSetPopupMenu(ActionEvent event) {
		var as = getCurrentAnnotationSet();
		AnnotationSetMenu menu = annotationSetMenuProvider.get();
		Component c = (Component)event.getSource();
		menu.show(as, c, 0, c.getHeight());
	}
	
	private void showClusterPopupMenu(Component component, int x, int y) {
		List<Cluster> clusters = getSelectedClusters();
		ClusterMenu menu = clusterMenuProvider.get();
		menu.show(clusters, component, x, y);
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
		return BuildProperties.APP_NAME;
	}

	@Override
	public Icon getIcon() {
		URL url = CyActivator.class.getResource("auto_annotate_logo_16by16_v5.png");
		return url == null ? null : new ImageIcon(url);
	}

}
