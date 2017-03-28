package org.baderlab.autoannotate.internal.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

@SuppressWarnings("serial")
public final class ColorButton extends JButton {

	private Color color;
	private Color borderColor;

	public ColorButton(final Color color) {
		super(" ");
		putClientProperty("JButton.buttonType", "gradient"); // Aqua LAF only
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.CENTER);
		borderColor = getContrastingColor(getBackground());
		setIcon(new ColorIcon());
		setColor(color);
		
		addActionListener(e -> {
			JColorChooser chooser = new JColorChooser();
			chooser.setColor(ColorButton.this.color);

			JDialog dialog = JColorChooser.createDialog(ColorButton.this, "Colors", true, chooser, null, null);
			dialog.setVisible(true);
			Color c = chooser.getColor();
			setColor(c);
		});
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
	
	/**
	 * @return The currently selected color.
	 */
	public Color getColor() {
		return color;
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
			
			g.setColor(color);
			g.fillRect(x, y, w, h);
			g.setColor(borderColor);
			g.drawRect(x, y, w, h);
		}
	}
}
