package org.baderlab.autoannotate.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;

@SuppressWarnings("serial")
public final class ColorPaletteButton extends JButton {

	public static enum Mode {
		SINGLE_COLOR, PALETTE, SIGNIFICANT
	}
	
	private final CyServiceRegistrar registrar;
	
	private Color color;
	private Color borderColor;
	private Palette palette;
	private Mode mode = Mode.SINGLE_COLOR;
	

	public ColorPaletteButton(CyServiceRegistrar registrar, Color color, Palette palette) {
		super(" ");
		this.registrar = registrar;
		
		putClientProperty("JButton.buttonType", "gradient"); // Aqua LAF only
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.CENTER);
		borderColor = getContrastingColor(getBackground());
		setIcon(new ColorIcon());
		
		setColor(color);
		setPalette(palette);
		
		addActionListener(e -> {
			if(mode == Mode.PALETTE)
				pickColorPalette();
			else if(mode == Mode.SIGNIFICANT)
				pickSignificant();
			else
				pickSingleColor();
		});
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
		repaint();
	}
	
	private void pickSingleColor() {
		JColorChooser chooser = new JColorChooser();
		chooser.setColor(ColorPaletteButton.this.color);

		JDialog dialog = JColorChooser.createDialog(ColorPaletteButton.this, "Colors", true, chooser, null, null);
		dialog.setVisible(true);
		Color c = chooser.getColor();
		setColor(c);
	}
	
	private void pickColorPalette() {
		if(palette == null)
			return;
		
		var chooserFactory = registrar.getService(CyColorPaletteChooserFactory.class);
		var chooser = chooserFactory.getColorPaletteChooser(BrewerType.ANY, true);
		
		int size = 8;
		var p = chooser.showDialog(this, "Palettes", palette, size);
		setPalette(p);
	}

	private void pickSignificant() {
		firePropertyChange("significance", null, null);
	}
	
	/**
	 * Sets a new color and fires a {@link java.beans.PropertyChangeEvent} for the property "color".
	 * @param color
	 */
	public void setColor(final Color color) {
		final Color oldColor = this.color;
		this.color = color;
		repaint();
		firePropertyChange("color", oldColor, color);
	}
	
	public void setPalette(Palette palette) {
		var oldPalette = this.palette;
		this.palette = palette;
		repaint();
		firePropertyChange("palette", oldPalette, palette);
	}
	
	/**
	 * @return The currently selected color.
	 */
	public Color getColor() {
		return color;
	}
	
	public Palette getPalette() {
		return palette;
	}
	
	private static Color getContrastingColor(final Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		final double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	private class ColorIcon implements Icon {

		@Override
		public int getIconHeight() {
			return 16;
		}

		@Override
		public int getIconWidth() {
			return 44;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int w = getIconWidth();
			int h = getIconHeight();
			
			if(mode == Mode.PALETTE) {
				g.setColor(borderColor);
				g.fillRect(x, y, w, h);
				int n = 6;
				var colors = palette.getColors(n);
				int segW = w / n;
				for(int i = 0; i < n; i++) {
					g.setColor(colors[i]);
					g.fillRect(x + 1 + segW * i, y, segW, h);
				}
			} else if(mode == Mode.SIGNIFICANT) {
				g.setColor(Color.GRAY);
				g.fillRect(x, y, w, h);
			} else {
				g.setColor(color);
				g.fillRect(x, y, w, h);
			}
			
			g.setColor(borderColor);
			g.drawRect(x, y, w, h);
		}
	}
}
