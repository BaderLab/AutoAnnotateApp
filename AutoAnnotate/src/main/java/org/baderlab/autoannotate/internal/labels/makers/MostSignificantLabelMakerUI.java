package org.baderlab.autoannotate.internal.labels.makers;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.labels.LabelMakerUI;
import org.baderlab.autoannotate.internal.labels.makers.MostSignificantOptions.Significance;
import org.baderlab.autoannotate.internal.ui.view.create.CreateViewUtil;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyColumnComboBox;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class MostSignificantLabelMakerUI implements LabelMakerUI<MostSignificantOptions> {

	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	@Inject private Provider<CyApplicationManager> appManagerProvider;
	
	
	private final MostSignificantLabelMakerFactory factory;
	
	private MostSignificantOptionsPanel panel;
	
	
	public interface Factory {
		MostSignificantLabelMakerUI create(MostSignificantLabelMakerFactory factory);
	}
	
	@AssistedInject
	public MostSignificantLabelMakerUI(@Assisted MostSignificantLabelMakerFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public MostSignificantLabelMakerFactory getFactory() {
		return factory;
	}
	
	@Override
	public JPanel getPanel() {
		if(panel == null) {
			panel = new MostSignificantOptionsPanel();
			reset(null);
		}
		return panel;
	}

	@Override
	public MostSignificantOptions getContext() {
		return new MostSignificantOptions(panel.getSignificanceColumn(), panel.getSignificance());
	}

	@Override
	public void reset(Object options) {
		var currentNetwork = appManagerProvider.get().getCurrentNetwork();
		panel.reset((MostSignificantOptions)options, currentNetwork);
	}
	
	
	@SuppressWarnings("serial")
	private class MostSignificantOptionsPanel extends JPanel {
		
		private CyColumnComboBox columnCombo;
		private JComboBox<ComboItem<Significance>> sigCombo;
		
		public MostSignificantOptionsPanel() {
			setLayout(new GridBagLayout());
			
			JLabel label1 = new JLabel("    Signficance Column :");
			columnCombo = new CyColumnComboBox(presentationManagerProvider.get(), List.of()); // Initially empty, need reset() to be called
			
			JLabel label2 = new JLabel("    Most Significant: ");
			sigCombo = CreateViewUtil.createComboBox(Arrays.asList(Significance.values()), Significance::toString);
			
			add(makeSmall(label1), GBCFactory.grid(0,0).get());
			add(makeSmall(columnCombo), GBCFactory.grid(1,0).weightx(1.0).get());
			add(makeSmall(label2), GBCFactory.grid(0,1).get());
			add(makeSmall(sigCombo), GBCFactory.grid(1,1).weightx(1.0).get());
			
		}

		public String getSignificanceColumn() {
			return columnCombo.getSelectedItem().getName();
		}
		
		public Significance getSignificance() {
			return sigCombo.getItemAt(sigCombo.getSelectedIndex()).getValue();
		}
		
		
		public void reset(MostSignificantOptions options, CyNetwork network) {
			var columns = CreateViewUtil.getNumericColumns(network);
			CreateViewUtil.updateColumnCombo(columnCombo, columns);
			CreateViewUtil.setSignificanceColumnDefault(columnCombo);
			sigCombo.setSelectedItem(ComboItem.of(Significance.getDefault()));
		}
		
	}
}
