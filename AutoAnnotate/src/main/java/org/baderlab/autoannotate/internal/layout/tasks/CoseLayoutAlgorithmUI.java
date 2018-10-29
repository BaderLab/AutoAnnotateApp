package org.baderlab.autoannotate.internal.layout.tasks;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.autoannotate.internal.layout.ClusterLayoutAlgorithmUI;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;


@SuppressWarnings("serial")
public class CoseLayoutAlgorithmUI implements ClusterLayoutAlgorithmUI<CoseLayoutContext> {

	private CoseLayoutAlgorithmOptionsPanel panel;
	private CoseLayoutContext context;
	
	
	public CoseLayoutAlgorithmUI(CoseLayoutContext context) {
		this.context = context;
		this.panel = new CoseLayoutAlgorithmOptionsPanel(context);
	}
	
	
	private static class CoseLayoutAlgorithmOptionsPanel extends JPanel {
		
		private SpacingSlider slider;
		private JCheckBox checkbox;
		
		public CoseLayoutAlgorithmOptionsPanel(CoseLayoutContext context) {
			JLabel label = new JLabel("Space between clusters:");
			
			int gravity = Math.max(50, Math.min(60, context.gravityRange));
			context.gravityRange = gravity;
			
			slider = new SpacingSlider(50, 60, gravity);
			checkbox = new JCheckBox("Group unclustered nodes");
			SwingUtil.makeSmall(label, checkbox);
			
			setLayout(new GridBagLayout());
			add(label,    GBCFactory.grid(0,0).get());
			add(slider,   GBCFactory.grid(0,1).get());
			add(checkbox, GBCFactory.grid(0,2).get());
		}
		
		public boolean getGroupUnclusteredNodes() {
			return checkbox.isSelected();
		}
		
		public int getGravityRange() {
			return slider.getGravityValue();
		}
	}
	
	private static class SpacingSlider extends JPanel {

		private JSlider slider;
		
		public SpacingSlider(int min, int max, int defaultTick) {
			slider = new JSlider(min, max, defaultTick);
			slider.setMajorTickSpacing(1);
			slider.setSnapToTicks(true);
			slider.setPaintTicks(true);
			slider.setOpaque(false);
			
			JLabel lessLabel = new JLabel("less");
			JLabel moreLabel = new JLabel("more");
			SwingUtil.makeSmall(lessLabel, moreLabel);
			
			Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
			labelTable.put(min, lessLabel);
			labelTable.put(max, moreLabel);
			
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);
			
			setLayout(new BorderLayout());
			add(slider, BorderLayout.SOUTH);
			setOpaque(false);
		}
		
		public int getGravityValue() {
			return slider.getValue();
		}
	}
	
	
	@Override
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public CoseLayoutContext getContext() {
		context.gravityRange = panel.getGravityRange();
		context.useCatchallCluster = panel.getGroupUnclusteredNodes();
		return context;
	}

	@Override
	public void reset(CoseLayoutContext context) {
	}

}
