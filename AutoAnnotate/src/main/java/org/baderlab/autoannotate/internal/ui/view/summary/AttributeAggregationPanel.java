package org.baderlab.autoannotate.internal.ui.view.summary;

import static org.baderlab.autoannotate.internal.util.GBCFactory.grid;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorOperator;
import org.baderlab.autoannotate.internal.data.aggregators.AggregatorSet;
import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;


@SuppressWarnings("serial")
public class AttributeAggregationPanel extends JPanel {
	
	@Inject private Provider<CyColumnPresentationManager> presentationManagerProvider;
	
	private final String title;
	private final AggregatorSet aggregators;
	
	
	public static interface Factory {
		AttributeAggregationPanel create(String title, AggregatorSet aggregators);
	}
	
	@Inject
	public AttributeAggregationPanel(@Assisted String title, @Assisted AggregatorSet aggregators) {
		this.title = title;
		this.aggregators = aggregators;
	}
	
	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title));
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		panel.setBackground(this.getBackground().brighter());
		
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, BorderLayout.CENTER);
		
		int row = 0;
		var columns = aggregators.getTable().getColumns();
		columns = sortAndFilter(columns);
		
		for(var col : columns) {
			var nameLabel = createAttrLabel(col);
			var typeLabel = createTypeLabel(col);
			var aggCombo  = createAggregateCombo(col);
			
			panel.add(nameLabel, grid(0,row).weightx(1.0).get());
			panel.add(typeLabel, grid(1,row).get());
			panel.add(aggCombo,  grid(2,row).get());
			row++;
		}
	}
	
	
	private static Collection<CyColumn> sortAndFilter(Collection<CyColumn> columns) {
		Set<CyColumn> sorted = new LinkedHashSet<>(); // Maintains insertion order
		
		// EM columns first
		for(var col : columns) {
			if("EnrichmentMap".equals(col.getNamespace())) {
				sorted.add(col);
			}
		}
		
		for(var col : columns) {
			if(CyNetwork.SUID.equals(col.getName()))
				continue;
			sorted.add(col); // duplicates ignored
		}
		
		return sorted;
	}
	
	
	private JLabel createAttrLabel(CyColumn attr) {
		var fullName = attr.getName();
		var shortName = SwingUtil.abbreviate(fullName, 60);
		
		JLabel label = new JLabel();
		presentationManagerProvider.get().setLabel(shortName, label);
		label.setToolTipText(fullName);
		
		return label;
	}
	
	private JLabel createTypeLabel(CyColumn attr) {
		return new JLabel(attr.getType().getSimpleName());
	}
	
	private JComboBox<AggregatorOperator> createAggregateCombo(CyColumn attr) {
		var name = attr.getName();
		var handlers = aggregators.getAggregatorOperators(name);
		var selected = aggregators.getOperator(name);
		
		var combo = new JComboBox<>(handlers);
		combo.setSelectedItem(selected);
		
		combo.addActionListener(e -> {
			var handler = combo.getItemAt(combo.getSelectedIndex());
			aggregators.setOperator(name, handler);
		});
		
		return combo;
	}

}
