package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetDeleteAction;
import org.baderlab.autoannotate.internal.ui.view.action.AnnotationSetRenameAction;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.cytoscape.application.CyApplicationManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class ManageAnnotationSetsDialog extends JDialog {

	@Inject private Provider<AnnotationSetRenameAction> renameActionProvider;
	@Inject private Provider<AnnotationSetDeleteAction> deleteActionProvider;
	
	private final NetworkViewSet networkViewSet;
	
	private JList<ComboItem<AnnotationSet>> annotationSetList;
	private JButton upButton;
	private JButton downButton;
	private JButton renameButton;
	private JButton deleteButton;
	
	
	// using guice-asssistedinject
	public interface Factory {
		ManageAnnotationSetsDialog create(NetworkViewSet networkViewSet);
	}
	
	
	@Inject
	public ManageAnnotationSetsDialog(JFrame jFrame, CyApplicationManager appManager, @Assisted NetworkViewSet networkViewSet) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Manage Annotate Sets");
		this.networkViewSet = networkViewSet;
	}
	
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		JPanel parent = new JPanel();
		parent.setBorder(BorderFactory.createEmptyBorder(7, 7, 7, 7));
		add(parent, BorderLayout.CENTER);
		
		JPanel listPanel = createListPanel();
		JPanel buttonPanel = createButtonPanel();
		JPanel bottomPanel = createBottomPanel();
		
		parent.setLayout(new BorderLayout());
		parent.add(listPanel, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.EAST);
		parent.add(bottomPanel, BorderLayout.SOUTH);
		
		updateButtonEnablement();
		pack();
	}


	private JPanel createListPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		annotationSetList = new JList<>();
		annotationSetList.setModel(getListModel());
		annotationSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if(annotationSetList.getModel().getSize() > 0) {
			annotationSetList.setSelectedIndex(0);
		}
		
		annotationSetList.getSelectionModel().addListSelectionListener(e -> updateButtonEnablement());
		
		JScrollPane scrollPane = new JScrollPane(annotationSetList, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(350, 250));
		
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}
	

	private ListModel<ComboItem<AnnotationSet>> getListModel() {
		DefaultListModel<ComboItem<AnnotationSet>> listModel = new DefaultListModel<>();
		for(AnnotationSet as : networkViewSet.getAnnotationSets()) {
			listModel.addElement(new ComboItem<>(as, as.getName()));
		}
		return listModel;
	}
	
	
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		upButton     = new JButton("Up");
		downButton   = new JButton("Down");
		renameButton = new JButton("Rename");
		deleteButton = new JButton("Delete");
		
		upButton  .addActionListener(e -> swapSelectedWith(i -> i - 1));
		downButton.addActionListener(e -> swapSelectedWith(i -> i + 1));
		
		renameButton.addActionListener(e -> rename());
		deleteButton.addActionListener(e -> delete());
		
		layout.setHorizontalGroup(
			layout.createParallelGroup(Alignment.CENTER)
				.addComponent(upButton)
				.addComponent(downButton)
				.addComponent(renameButton)
				.addComponent(deleteButton));
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(upButton)
				.addComponent(downButton)
				.addGap(15)
				.addComponent(renameButton)
				.addComponent(deleteButton));
		
		layout.linkSize(upButton, downButton, renameButton, deleteButton);
		
		return panel;
	}


	private JPanel createBottomPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton closeButton = new JButton("Close");
		buttonPanel.add(closeButton);
		closeButton.addActionListener(e -> dispose());
		
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
	
	private void updateButtonEnablement() {
		int index = annotationSetList.getSelectedIndex();
		int count = annotationSetList.getModel().getSize();
		
		upButton.setEnabled(index >= 1);
		downButton.setEnabled(index >= 0 && index < count - 1);
		renameButton.setEnabled(index >= 0);
		deleteButton.setEnabled(index >= 0);
	}
	
	private void swap(int i1, int i2) {
		DefaultListModel<ComboItem<AnnotationSet>> listModel = (DefaultListModel<ComboItem<AnnotationSet>>)annotationSetList.getModel();
		ComboItem<AnnotationSet> tmp = listModel.get(i1);
	    listModel.set(i1, listModel.get(i2));
	    listModel.set(i2, tmp);
	}
	
	private void swapSelectedWith(Function<Integer,Integer> swapWith) {
		int index1 = annotationSetList.getSelectedIndex();
		int index2 = swapWith.apply(index1);
		
		ComboItem<AnnotationSet> item1 = annotationSetList.getModel().getElementAt(index1);
		ComboItem<AnnotationSet> item2 = annotationSetList.getModel().getElementAt(index2);
		
		swap(index1, index2);
		networkViewSet.swap(item1.getValue(), item2.getValue());
		annotationSetList.setSelectedIndex(index2);
	}
	
	private void rename() {
		int index = annotationSetList.getSelectedIndex();
		ComboItem<AnnotationSet> item = annotationSetList.getSelectedValue();
		
		AnnotationSetRenameAction renameAction = renameActionProvider.get();
		renameAction.setAnnotationSet(item.getValue());
		renameAction.actionPerformed(null); // shows rename dialog
		
		// refresh the list
		// MKTODO is there a better way of doing this?
		annotationSetList.setModel(getListModel());
		annotationSetList.setSelectedIndex(index);
	}
	
	private void delete() {
		ComboItem<AnnotationSet> item = annotationSetList.getSelectedValue();
		
		AnnotationSetDeleteAction deleteAction = deleteActionProvider.get();
		deleteAction.setAnnotationSet(item.getValue());
		deleteAction.actionPerformed(null);
		
		// refresh the list
		// MKTODO is there a better way of doing this?
		annotationSetList.setModel(getListModel());
	}
	
}

