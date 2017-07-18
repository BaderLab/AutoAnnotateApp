package org.baderlab.autoannotate.internal.ui.view.display;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.baderlab.autoannotate.internal.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class FontComboPanel extends JPanel {

	private List<BiConsumer<String,Integer>> listenerList = new CopyOnWriteArrayList<>();
	
	private JComboBox<Font> fontCombo;
	private JToggleButton boldButton;
	private JToggleButton italicButton;
	
	@Inject
	public FontComboPanel(IconManager iconManager) {
		Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
		fontCombo = new JComboBox<>(allFonts);
		fontCombo.setRenderer(new FontRenderer());
		SwingUtil.makeSmall(fontCombo);
		fontCombo.setPreferredSize(new Dimension(130, fontCombo.getPreferredSize().height));
		
		boldButton   = SwingUtil.createIconToggleButton(iconManager, IconManager.ICON_BOLD, "Bold");
		italicButton = SwingUtil.createIconToggleButton(iconManager, IconManager.ICON_ITALIC, "Italic");
		
		fontCombo.addActionListener(this::fireChanged);
		boldButton.addActionListener(this::fireChanged);
		italicButton.addActionListener(this::fireChanged);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(boldButton);
		buttonPanel.add(italicButton);
		buttonPanel.setOpaque(false);
		
		setLayout(new BorderLayout());
		add(fontCombo, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);
		setOpaque(false);
	}
	
	public void init(String family, int style) {
		for(int i = 0; i < fontCombo.getItemCount(); i++) {
			if(fontCombo.getItemAt(i).getFontName().equals(family)) {
				fontCombo.setSelectedIndex(i);
				break;
			}
		}
		boldButton.setSelected((style & Font.BOLD) != 0);
		italicButton.setSelected((style & Font.ITALIC) != 0);
	}
	
	public void addFontListener(BiConsumer<String,Integer> listener) {
		listenerList.add(listener);
	}
	
	public void removeFontListener(BiConsumer<String,Integer> listener) {
		listenerList.remove(listener);
	}
	
	private void fireChanged(ActionEvent e) {
		String family = getFontFamily();
		int style = getFontStyle();
		for(BiConsumer<String,Integer> listener : listenerList) {
			listener.accept(family, style);
		}
	}
	
	private static class FontRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Font font = (Font)value;
			String name = font.getFontName();
			Component component = super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
			setFont(font.deriveFont(13f));
			return component;
		}
	}
	
	public String getFontFamily() {
		return fontCombo.getItemAt(fontCombo.getSelectedIndex()).getFontName();
	}
	
	public int getFontStyle() {
		int style = 0;
		if(boldButton.isSelected())
			style |= Font.BOLD;
		if(italicButton.isSelected())
			style |= Font.ITALIC;
		return style;
	}
}
