package org.baderlab.autoannotate.internal.ui.view.dialog;

import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static org.baderlab.autoannotate.internal.ui.view.dialog.CreateAnnotationSetDialog.getColumnsOfType;
import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.labels.LabelMakerFactory;
import org.baderlab.autoannotate.internal.labels.LabelMakerManager;
import org.baderlab.autoannotate.internal.model.ClusterAlgorithm;
import org.baderlab.autoannotate.internal.task.AnnotationSetTaskParamters;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class EasyModePanel extends JPanel implements TabPanel {

	private final CyNetworkView networkView;
	private final CreateAnnotationSetDialog parent;
	
	@Inject private Provider<LabelMakerManager> labelManagerProvider;
	
	private JComboBox<String> labelCombo;
	
	public static interface Factory {
		EasyModePanel create(CreateAnnotationSetDialog parent);
	}
	
	@Inject
	public EasyModePanel(@Assisted CreateAnnotationSetDialog parent, CyApplicationManager appManager) {
		this.networkView = appManager.getCurrentNetworkView();
		this.parent = parent;
		
	}
	
	@AfterInjection
	private void createContents() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		
		
		labelCombo = new JComboBox<>();
		for(String labelColumn : getColumnsOfType(networkView.getModel(), String.class, true, false, true)) {
			labelCombo.addItem(labelColumn);
		}
		
		// Preselect the best choice for label column, with special case for EnrichmentMap
		for(int i = 0; i < labelCombo.getItemCount(); i++) {
			String item = labelCombo.getItemAt(i);
			if(item.endsWith("GS_DESCR")) { // column created by EnrichmentMap
				labelCombo.setSelectedIndex(i);
				break;
			}
			if(item.equalsIgnoreCase("name")) {
				labelCombo.setSelectedIndex(i);
				break;
			}
		}
		
		JLabel colLabel = new JLabel(" Label Column: ");
		makeSmall(labelCombo, colLabel);
		
		JLabel maxLabel = new JLabel(" Maximum number of clusters: ");
		SpinnerModel spinnerModel = new SpinnerNumberModel(10, 1, 100, 1);
		JSpinner spinner = new JSpinner(spinnerModel);
		JLabel filler = new JLabel("");
		makeSmall(maxLabel, spinner, filler);
		
		JLabel checkLabel = new JLabel(" Layout network to prevent cluster overlap: ");
		JCheckBox checkBox = new JCheckBox();
		makeSmall(checkLabel, checkBox);
		
		panel.add(colLabel,   GBCFactory.grid(0,0).anchor(EAST).fill(NONE).get());
		panel.add(labelCombo, GBCFactory.grid(1,0).gridwidth(2).get());
		panel.add(maxLabel,   GBCFactory.grid(0,1).anchor(EAST).fill(NONE).get());
		panel.add(spinner,    GBCFactory.grid(1,1).get());
		panel.add(filler,     GBCFactory.grid(2,1).weightx(1).get());
		panel.add(checkLabel, GBCFactory.grid(0,2).get());
		panel.add(checkBox,   GBCFactory.grid(1,2).gridwidth(2).get());
		
		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
	}
	
	
	@Override
	public boolean isOkButtonEnabled() {
		return parent.isClusterMakerInstalled() && parent.isWordCloudInstalled();
	}
	
	public String getLabelColumn() {
		return labelCombo.getItemAt(labelCombo.getSelectedIndex());
	}
	
	@Override
	public AnnotationSetTaskParamters createAnnotationSetTaskParameters() {
		LabelMakerFactory<?> labelMakerFactory = labelManagerProvider.get().getDefaultFactory();
		Object labelMakerContext = labelMakerFactory.getDefaultContext();
		
		AnnotationSetTaskParamters params = 
			new AnnotationSetTaskParamters.Builder(networkView)
			.setLabelColumn(getLabelColumn())
			.setUseClusterMaker(true)
			.setClusterAlgorithm(ClusterAlgorithm.MCL)
			.setClusterMakerEdgeAttribute(CreateAnnotationSetDialog.NONE)
			.setLabelMakerFactory(labelMakerFactory)
			.setLabelMakerContext(labelMakerContext)
			.setCreateGroups(false)
			.build();
		
		return params;
	}

}
