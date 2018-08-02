package org.baderlab.autoannotate.internal.ui.view.copy;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class CopyAnnotationsDialog extends JDialog {

	@Inject private ModelManager modelManager;
	@Inject private CyRootNetworkManager rootNetworkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private IconManager iconManager;
	@Inject private CopyAnnotationsEnabler copyAnnotationsEnabler;
	
	private final CyNetworkView destination;
	
	public static interface Factory {
		CopyAnnotationsDialog create(CyNetworkView destination);
	}
	
	@AssistedInject
	public CopyAnnotationsDialog(@Assisted CyNetworkView destination, JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Copy Annotation Sets");
		this.destination = destination;
	}
	
	
	@AfterInjection
	public void createContents() {
		JPanel bodyPanel = createBodyPanel();
		JPanel buttonPanel = createButtonPanel();
		
		setLayout(new BorderLayout());
		add(bodyPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	private JPanel createBodyPanel() {
		JLabel title = new JLabel("Copy Annotations to: " + CopyAnnotationsEnabler.getName(destination));
		JLabel networkTitle = new JLabel("Copy annotations from");
		JLabel asTitle = new JLabel("Annotation sets to copy");
		
		JList<ComboItem<CyNetworkView>> networkList = createNetworkSourcesJList(destination);
		
		CyNetworkView first = networkList.getModel().getElementAt(0).getValue();
		NetworkViewSet nvs = modelManager.getNetworkViewSet(first);
		
		JTable annotationSetTable = createTable();
		annotationSetTable.setModel(new AnnotationSetTableModel(nvs.getAnnotationSets()));
		
		JButton selectAllButton = new JButton("Select All");
		JButton selectNoneButton = new JButton("Select None");
		
		JCheckBox allClustersCheckBox = new JCheckBox("Copy annotations even when clusters are incomplete");
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(title)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(networkTitle)
					.addComponent(networkList)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(asTitle)
					.addComponent(annotationSetTable)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
				)
			)
			.addComponent(allClustersCheckBox)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(title)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(networkTitle)
				.addComponent(asTitle)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(networkList)
				.addComponent(annotationSetTable)
				.addGroup(layout.createSequentialGroup()
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
				)
			)
			.addComponent(allClustersCheckBox)
		);
		
		return panel;
	}
	
	
	private JPanel createButtonPanel() {
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		JButton copyButton = new JButton("Copy Annotations");
//		copyButton.addActionListener(e -> createButtonPressed());
		
		LookAndFeelUtil.makeSmall(cancelButton, copyButton);
		JPanel panel = LookAndFeelUtil.createOkCancelPanel(copyButton, cancelButton);
		return panel;
	}
	
	
	
	private class AnnotationSetTableModel extends AbstractTableModel {
		private List<AnnotationSet> annotationSets;
		private boolean[] selected;
		
		public AnnotationSetTableModel(List<AnnotationSet> annotationSets) {
			this.annotationSets = annotationSets;
			this.selected = new boolean[annotationSets.size()];
		}

		@Override
		public int getRowCount() {
			return annotationSets.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if(columnIndex == 0)
				return selected[rowIndex];
			if(columnIndex == 1)
				return annotationSets.get(rowIndex).getName();
			return null;
		}
	}
	
	
	private JList<ComboItem<CyNetworkView>> createNetworkSourcesJList(CyNetworkView destination) {		
		List<Pair<CyNetworkView,String>> networkViews = copyAnnotationsEnabler.getCompatibleNetworkViews(destination);
		DefaultListModel<ComboItem<CyNetworkView>> listModel = new DefaultListModel<>();
		networkViews.forEach(pair -> listModel.addElement(new ComboItem<>(pair)));
		return new JList<>(listModel);
	}
	
	
	private JTable createTable() {
		JTable table = new JTable();
		table.setTableHeader(null);
		table.setShowGrid(false);
		table.setDefaultRenderer(Boolean.class, new CheckBoxTableCellRenderer());
		return table;
	}
	
	
	private class CheckBoxTableCellRenderer implements TableCellRenderer {
		final JPanel panel;
		final JCheckBox chk;
		
		CheckBoxTableCellRenderer() {
			chk = new JCheckBox();
			chk.putClientProperty("JComponent.sizeVariant", "mini"); // Aqua LAF only
			panel = new JPanel(new BorderLayout());
			panel.add(chk, BorderLayout.WEST);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Color bg = UIManager.getColor("Table.background");
			chk.setSelected((boolean)value);
			chk.setToolTipText((boolean)value ? "Show" : "Hide");
			chk.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			panel.setBackground(isSelected ? UIManager.getColor("Table.selectionBackground") : bg);
			panel.setBorder(new EmptyBorder(0, 0, 0, 0));
			return panel;
		}
	}
	
	
	
	
	
}
