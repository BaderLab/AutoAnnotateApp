package org.baderlab.autoannotate.internal.ui.view.copy;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.task.CopyAnnotationsTask;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.baderlab.autoannotate.internal.util.TaskTools;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class CopyAnnotationsDialog extends JDialog {

	@Inject private ModelManager modelManager;
	@Inject private NetworkList.Factory networkListFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CopyAnnotationsTask.Factory copyTaskFactory;
	
	private final CyNetworkView destination;
	
	private JButton okButton;
	private NetworkList networkList;
	private JTable annotationSetTable;
	private JCheckBox includeIncompleteCheckbox;
	
	private Map<CyNetworkView, AnnotationSetTableModel> tableModels = new HashMap<>();
	
	
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
		updateTable();
		
		setLayout(new BorderLayout());
		add(bodyPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	private JPanel createBodyPanel() {
		JLabel networkTitle = new JLabel("Copy annotations from");
		JLabel asTitle = new JLabel("Annotation sets to copy");
		
		networkList = networkListFactory.create(destination);
		JScrollPane networkListScrollPane = new JScrollPane(networkList);
		
		annotationSetTable = new JTable();
		annotationSetTable.getTableHeader().setPreferredSize(new Dimension(10, 20));
		
		JScrollPane tableScrollPane = new JScrollPane(annotationSetTable);
		
		networkList.addListSelectionListener(e -> updateTable());
		
		JButton selectAllButton = new JButton("Select All");
		selectAllButton.addActionListener(e -> ((AnnotationSetTableModel)annotationSetTable.getModel()).selectAll());
		JButton selectNoneButton = new JButton("Select None");
		selectNoneButton.addActionListener(e -> ((AnnotationSetTableModel)annotationSetTable.getModel()).selectNone());
		
		includeIncompleteCheckbox = new JCheckBox("Include annotations for clusters that are incomplete.");
		includeIncompleteCheckbox.setToolTipText("When selected all annoations are copied, even for clusters that have missing nodes.");
		
		SwingUtil.makeSmall(networkTitle, asTitle, selectAllButton, selectNoneButton, includeIncompleteCheckbox);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(networkTitle)
					.addComponent(networkListScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(asTitle)
					.addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
				)
			)
			.addComponent(includeIncompleteCheckbox)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(networkTitle)
				.addComponent(asTitle)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(networkListScrollPane)
				.addComponent(tableScrollPane)
				.addGroup(layout.createSequentialGroup()
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
				)
			)
			.addComponent(includeIncompleteCheckbox)
		);
		
		layout.linkSize(SwingConstants.HORIZONTAL, selectAllButton, selectNoneButton);
		
		return panel;
	}
	
	private void updateTable() {
		CyNetworkView netView = networkList.getSelectedValue();
		if(netView == null) {
			annotationSetTable.setModel(new AnnotationSetTableModel(null));
		} else {
			AnnotationSetTableModel tableModel = tableModels.computeIfAbsent(netView, (key) -> {
				NetworkViewSet nvs = modelManager.getNetworkViewSet(netView);
				return new AnnotationSetTableModel(nvs.getAnnotationSets());
			});
			annotationSetTable.setModel(tableModel);
			tableModel.addTableModelListener(e -> updateButtonEnablement());
		}
		annotationSetTable.getColumnModel().getColumn(0).setMaxWidth(27);
		annotationSetTable.getColumnModel().getColumn(2).setMaxWidth(60);
		updateButtonEnablement();
	}
	
	
	private void updateButtonEnablement() {
		boolean empty = ((AnnotationSetTableModel)annotationSetTable.getModel()).getSelectedAnnotationSets().isEmpty();
		okButton.setEnabled(!empty);
	}
	
	private JPanel createButtonPanel() {
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		okButton = new JButton("Copy Annotations");
		okButton.addActionListener(e -> copyAnnotations());
		okButton.setEnabled(false);
		
		LookAndFeelUtil.makeSmall(cancelButton, okButton);
		JPanel panel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
		return panel;
	}
	
	
	private void copyAnnotations() {
		List<AnnotationSet> annotationSetsToCopy = ((AnnotationSetTableModel)annotationSetTable.getModel()).getSelectedAnnotationSets();
		
		CopyAnnotationsTask copyAnnotationsTask = copyTaskFactory.create(annotationSetsToCopy, destination);
		copyAnnotationsTask.setIncludeIncompleteClusters(includeIncompleteCheckbox.isSelected());
		
		Task closeTask = TaskTools.taskOf(() -> dispose());
		
		TaskIterator tasks = new TaskIterator(copyAnnotationsTask, closeTask);
		dialogTaskManager.execute(tasks);
	}
}
