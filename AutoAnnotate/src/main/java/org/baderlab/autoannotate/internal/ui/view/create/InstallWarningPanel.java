package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.OpenBrowser;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class InstallWarningPanel extends JPanel {
	
	private static final String WARN_CARD = "warning";
	private static final String NORMAL_CARD = "contents";

	@Inject private IconManager iconManager;
	@Inject private Provider<OpenBrowser> browserProvider;
	
	private final String message;
	private final String appName;
	private final String appUrl;
	private final JPanel contents;
	
	private CardLayout cardLayout;
	private Runnable onClickHandler;
	
	public static interface Factory {
		InstallWarningPanel create(
			JPanel contents, 
			@Assisted("message") String message, 
			@Assisted("appName") String appName, 
			@Assisted("appUrl") String appUrl);
	}

	@AssistedInject
	public InstallWarningPanel(
			@Assisted JPanel contents, 
			@Assisted("message") String message,
			@Assisted("appName") String appName, 
			@Assisted("appUrl") String appUrl) 
	{
		this.message = message;
		this.appName = appName;
		this.appUrl = appUrl;
		this.contents = contents;
	}
	
	@AfterInjection
	private void createContents() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		
		JPanel warningPanel = createMessagePanel();
		
		add(contents, NORMAL_CARD);
		add(warningPanel, WARN_CARD);
		setOpaque(false);
	}
	
	public void setOnClickHandler(Runnable handler) {
		this.onClickHandler = handler;
	}
	
	
	private JPanel createMessagePanel() {
		final boolean error = true; // temporary
		
		JPanel panel = new JPanel(new BorderLayout());
		
		JLabel icon = new JLabel(error ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_CIRCLE);
		icon.setFont(iconManager.getIconFont(16));
		icon.setForeground(error ? Color.RED.darker() : Color.YELLOW.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		
		JLabel messageLabel = new JLabel(message + "  ");
		
		JLabel link = new JLabel("<HTML><FONT color=\"#000099\"><U>install " + appName + "</U></FONT></HTML>");
		link.setCursor(new Cursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				browserProvider.get().openURL(appUrl);
				
				if(onClickHandler != null) {
					onClickHandler.run();
				}
			}
		});
		
		panel.add(icon, BorderLayout.WEST);
		panel.add(messageLabel, BorderLayout.CENTER);
		panel.add(link, BorderLayout.EAST);
		panel.setOpaque(false);
		return panel;
	}


	public void showWarning(boolean warning) {
		cardLayout.show(this, warning ? WARN_CARD : NORMAL_CARD);
	}
}
