package org.baderlab.autoannotate.internal.labels;

import static org.baderlab.autoannotate.internal.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.util.GBCFactory;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

@SuppressWarnings("serial")
public class LabelQuestionButton extends JLabel {
	
	@Inject private Provider<IconManager> iconManagerProvider;
	
	private final LabelMakerFactory<?> factory;
	
	public interface Factory {
		LabelQuestionButton create(LabelMakerFactory<?> factory);
	}
	
	@AssistedInject
	public LabelQuestionButton(@Assisted LabelMakerFactory<?> factory) {
		this.factory = factory;
	}

	@AfterInjection
	private void createContents() {
		makeSmall(this);
		setFont(iconManagerProvider.get().getIconFont(14));
		setText("  " + IconManager.ICON_QUESTION_CIRCLE);
		
		addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				String[] description = factory.getDescription();
				if(description == null || description.length == 0) {
					description = new String[] {"(no description)"};
				}
				
				JPanel panel = new JPanel(new GridBagLayout());
				int y = 0;
				for(String s : description) {
					JLabel lab = new JLabel(" " + s);
					makeSmall(lab);
					panel.add(lab, GBCFactory.grid(0,y++).weightx(1.0).get());
				}
				
				JPopupMenu popup = new JPopupMenu();
				popup.setLayout(new BorderLayout());
				popup.add(panel);
				var button = LabelQuestionButton.this;
				popup.show(button, 10, button.getHeight());
			}
		});
	}
	
}
