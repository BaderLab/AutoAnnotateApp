package org.baderlab.autoannotate.internal.ui.view.create;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.baderlab.autoannotate.internal.util.SwingUtil;
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
	
	private final AppInfo appInfo;
	private final JPanel contents;
	private boolean isShowingWarning;
	
	private CardLayout cardLayout;
	private Runnable onClickHandler;
	
	
	public static class AppInfo {
		final String message;
		final String appName; 
		final String appUrl;
		
		public AppInfo(String appName, String appUrl, String message) {
			this.message = message;
			this.appName = appName;
			this.appUrl = appUrl;
		}
	}
	
	
	public static interface Factory {
		InstallWarningPanel create(JPanel contents, AppInfo appInfo);
	}

	@AssistedInject
	public InstallWarningPanel(@Assisted JPanel contents, @Assisted AppInfo appInfo) {
		this.contents = contents;
		this.appInfo = appInfo;
	}
	
	@AfterInjection
	private void createContents() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		
		JPanel warningPanel = createMessagePanel();
		
		add(contents, NORMAL_CARD);
		add(warningPanel, WARN_CARD);
		
		cardLayout.show(this, NORMAL_CARD);
		isShowingWarning = false;
		
		setOpaque(false);
	}
	
	public void setOnClickHandler(Runnable handler) {
		this.onClickHandler = handler;
	}
	
	
	private JPanel createMessagePanel() {
		final boolean error = true; // temporary
		
		JLabel icon = new JLabel(error ? IconManager.ICON_TIMES_CIRCLE : IconManager.ICON_EXCLAMATION_CIRCLE);
		icon.setFont(iconManager.getIconFont(16));
		icon.setForeground(error ? Color.RED.darker() : Color.YELLOW.darker());
		icon.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		
		JLabel warnLabel = new JLabel(appInfo.appName + " is not installed  ");
		
		JLabel messageLabel = new JLabel(appInfo.message);
		SwingUtil.makeSmall(messageLabel);
		messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 0));
		
		JLabel link = createLink();
		
		JPanel panel = new JPanel(new GridBagLayout());
		
		panel.add(icon, GBCFactory.grid(0,0).get());
		panel.add(warnLabel, GBCFactory.grid(1,0).weightx(1.0).get());
		panel.add(link, GBCFactory.grid(2,0).get());
		panel.add(messageLabel, GBCFactory.grid(0,1).gridwidth(3).get());
		
		panel.setOpaque(false);
		return panel;
	}
	
	
	private JLabel createLink() {
		JLabel link = new JLabel("<HTML><FONT color=\"#000099\"><U>install " + appInfo.appName + "</U></FONT></HTML>");
		link.setCursor(new Cursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				browserProvider.get().openURL(appInfo.appUrl);
				if(onClickHandler != null) {
					onClickHandler.run();
				}
			}
		});
		return link;
	}


	public void showWarning(boolean warning) {
		cardLayout.show(this, warning ? WARN_CARD : NORMAL_CARD);
		isShowingWarning = warning;
	}
	
	public boolean isShowingWarning() {
		return isShowingWarning;
	}
}
