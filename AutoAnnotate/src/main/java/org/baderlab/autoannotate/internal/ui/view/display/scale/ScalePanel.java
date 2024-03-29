package org.baderlab.autoannotate.internal.ui.view.display.scale;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.makeSmall;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.layout.LayoutEdit;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.UndoSupport;

import com.google.inject.Inject;

/*
 * #%L
 * Cytoscape Manual Layout Impl (manual-layout-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
/**
 * GUI for scale of manualLayout
 */
@SuppressWarnings("serial")
public class ScalePanel extends JPanel implements ChangeListener {
	
	private JLabel label;
	private JCheckBox checkBox;
	private JSlider slider;
	private JCheckBox alongXAxis;
	private JCheckBox alongYAxis;
	private JButton resetButton;
	
	private int prevValue; 

	private boolean startAdjusting = true;

	private final CyApplicationManager appMgr;
	private final UndoSupport undoSupport;
	private LayoutEdit layoutEdit = null;

	@Inject
	public ScalePanel(CyApplicationManager appMgr, IconManager iconMgr, UndoSupport undoSupport) {
		this.appMgr = appMgr;
		this.undoSupport = undoSupport;

		prevValue = getSlider().getValue();

		label = new JLabel("Scale");
		
		checkBox = new JCheckBox("Selected Only", /* selected = */true);
//		new CheckBoxTracker(checkBox);

		alongXAxis = new JCheckBox("Width");
		alongYAxis = new JCheckBox("Height");
		alongXAxis.setSelected(true);
		alongYAxis.setSelected(true);

		resetButton = createButton(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSlider(0);
			}
		}, "Reset Scale");
		resetButton.setText(IconManager.ICON_REFRESH);
		resetButton.setFont(iconMgr.getIconFont(16.0f));
		
		makeSmall(label, checkBox, getSlider(), alongXAxis, alongYAxis);
		
//		new SliderStateTracker(this);

		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addGroup(layout.createSequentialGroup()
						.addComponent(label)
						.addGap(10,  10, Short.MAX_VALUE)
						.addComponent(alongXAxis)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(alongYAxis)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(checkBox)
				)
				.addGroup(layout.createSequentialGroup()
						.addComponent(getSlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(resetButton)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(alongXAxis, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(alongYAxis, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(checkBox, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(getSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(resetButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		
		if (isAquaLAF())
			setOpaque(false);
	} 

	public static JButton createButton(Action a, String tt) {
		JButton b = new JButton(a);
		b.setToolTipText(tt);
		b.setPreferredSize(new Dimension(32, 24));
		b.setMaximumSize(new Dimension(32, 24));
		b.setMinimumSize(new Dimension(32, 24));
		b.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		b.setBorderPainted(false);
		b.setOpaque(false);
		b.setContentAreaFilled(false);

		return b;
	}
	
	
	public void updateSlider(int x) {
		prevValue = x;
		getSlider().setValue(x);
	}

	public int getSliderValue() {
		return getSlider().getValue();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() != getSlider())
			return;

		CyNetworkView currentView = appMgr.getCurrentNetworkView();
		
		if (currentView == null)
			return;


		// TODO support undo events
		// only create the edit when we're beginning to adjust
		if ( startAdjusting ) { 
			layoutEdit = new LayoutEdit("Scale", currentView);
			startAdjusting = false;
		}

		// do the scaling
		MutablePolyEdgeGraphLayout nativeGraph = GraphConverter2.getGraphReference(128.0d, true, checkBox.isSelected(),currentView);
		ScaleLayouter scale = new ScaleLayouter(nativeGraph);

		double prevAbsoluteScaleFactor = Math.pow(2, ((double) prevValue) / 100.0d);

		double currentAbsoluteScaleFactor = Math.pow(2, ((double) getSlider().getValue()) / 100.0d);

		double neededIncrementalScaleFactor = currentAbsoluteScaleFactor / prevAbsoluteScaleFactor;

		ScaleLayouter.Direction direction = ScaleLayouter.Direction.BOTH_AXES;
		if (alongXAxis.isSelected() && alongYAxis.isSelected())
			direction = ScaleLayouter.Direction.BOTH_AXES;
		else if (alongXAxis.isSelected())
			direction = ScaleLayouter.Direction.X_AXIS_ONLY;
		else if (alongYAxis.isSelected())
			direction = ScaleLayouter.Direction.Y_AXIS_ONLY;
		
		scale.scaleGraph(neededIncrementalScaleFactor, direction);
		currentView.updateView();
		prevValue = getSlider().getValue();

		// TODO support undo
		// only post the edit when we're finished adjusting 
		if (!getSlider().getValueIsAdjusting()) { 
			if (undoSupport != null && layoutEdit != null)
				 undoSupport.postEdit(layoutEdit);
			startAdjusting = true;
		} 
	}

	public void setHeightCheckBoxVisible(boolean visible) {
		alongYAxis.setVisible(visible);
	}
	
	public void setWidthCheckBoxVisible(boolean visible) {
		alongXAxis.setVisible(visible);
	}
	
	public void setSelectedCheckBoxVisible(boolean visible) {
		checkBox.setVisible(visible);
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		label.setEnabled(enabled);
		checkBox.setEnabled(enabled);
		getSlider().setEnabled(enabled);
		alongXAxis.setEnabled(enabled);
		alongYAxis.setEnabled(enabled);
		resetButton.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}
	
	public JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider();
			slider.setMajorTickSpacing(100);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			slider.setMaximum(300);
			slider.setValue(0);
			slider.setMinimum(-300);
			slider.addChangeListener(this);
			
			var labels = getSliderTickLabels();
			slider.setLabelTable(labels);
		}
		
		return slider;
	}
	
	private Hashtable<Integer,JLabel> getSliderTickLabels() {
		Hashtable<Integer,JLabel> labels = new Hashtable<>();
		labels.put(-300, new JLabel("closer"));
		labels.put(300,  new JLabel("farther"));
		return labels;
	}
	
//	private Hashtable<Integer,JLabel> getSliderTickLabels() {
//		Hashtable<Integer,JLabel> labels = new Hashtable<>();
//		labels.put(-300, new JLabel("1/8"));
//		labels.put(-200, new JLabel("1/4"));
//		labels.put(-100, new JLabel("1/2"));
//		labels.put(0,    new JLabel("1"));
//		labels.put(100,  new JLabel("2"));
//		labels.put(200,  new JLabel("4"));
//		labels.put(300,  new JLabel("8"));
//		return labels;
//	}
}
