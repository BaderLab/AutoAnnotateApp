package org.baderlab.autoannotate.internal.ui.view.layout;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.layout.ClusterLayoutAlgorithm;
import org.baderlab.autoannotate.internal.layout.ClusterLayoutAlgorithmUI;
import org.baderlab.autoannotate.internal.util.ComboItem;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;


/**
 * MKTODO Save the algorithm properties in the session? CyProperty?
 */
@SuppressWarnings("serial")
@Singleton
public class LayoutClustersDialog extends JDialog {
	
	@Inject private Provider<Set<ClusterLayoutAlgorithm<?>>> algorithmProvider;
	@Inject private @Named("default") String defaultAlgID;

	private JComboBox<ComboItem<ClusterLayoutAlgorithm<?>>> algCombo;
	
	
	@Inject
	public LayoutClustersDialog(JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Layout Clusters");
	}
	 
	
	@AfterInjection
	private void createContents() {
		Set<ClusterLayoutAlgorithm<?>> algorithms = algorithmProvider.get();
		
		JLabel algLabel = new JLabel("Layout Algorithm:");
		algCombo = createAlgorithmCombo(algorithms);
		JPanel cardPanel = createCardPanel(algorithms);
		SwingUtil.makeSmall(algLabel, algCombo);
		
		algCombo.addActionListener(e -> {
			String id = getAlgorithm().getID();
			((CardLayout)cardPanel.getLayout()).show(cardPanel, id);
		});
		
		JButton applyLayoutButton = new JButton("Apply Layout");
		applyLayoutButton.addActionListener(e -> applyLayout());
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> setVisible(false));
		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(applyLayoutButton, closeButton);
		
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.add(algLabel, GBCFactory.grid(0,0).insets(2,2,2,2).get());
		topPanel.add(algCombo, GBCFactory.grid(0,1).insets(2,2,2,2).weightx(1.0).get());

		getRootPane().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);
		add(cardPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		pack();
	}
	
	
	private ClusterLayoutAlgorithm<?> getAlgorithm() {
		return algCombo.getItemAt(algCombo.getSelectedIndex()).getValue();
	}
	
	private JComboBox<ComboItem<ClusterLayoutAlgorithm<?>>> createAlgorithmCombo(Set<ClusterLayoutAlgorithm<?>> algorithms) {
		JComboBox<ComboItem<ClusterLayoutAlgorithm<?>>> algCombo = new JComboBox<>();
		int selected = 0;
		int i = 0;
		for(ClusterLayoutAlgorithm<?> alg : algorithms) {
			algCombo.addItem(new ComboItem<>(alg, alg.getDisplayName()));
			if(alg.getID().equals(defaultAlgID)) {
				selected = i;
			}
			i++;
		}
		algCombo.setSelectedIndex(selected);
		return algCombo;
	}
	
	
	private  JPanel createCardPanel(Set<ClusterLayoutAlgorithm<?>> algorithms) {
		CardLayout cardLayout = new CardLayout();
		JPanel cardPanel = new JPanel(cardLayout);
		cardPanel.setBorder(LookAndFeelUtil.createPanelBorder());
		
		for(ClusterLayoutAlgorithm alg : algorithms) {
			Object context = alg.createLayoutContext();
			ClusterLayoutAlgorithmUI ui = alg.createUI(context);
			JPanel panel = ui == null ? new JPanel() : ui.getPanel();
			panel.setOpaque(false);
			cardPanel.add(panel, alg.getID());
		}
		cardLayout.show(cardPanel, defaultAlgID);
		return cardPanel;
	}
	
	private void applyLayout() {
		ClusterLayoutAlgorithm<?> algorithm = getAlgorithm();  
	}
	
}
