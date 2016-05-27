package org.baderlab.autoannotate.internal.ui.view;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.autoannotate.internal.AfterInjection;
import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.model.ModelManager;
import org.baderlab.autoannotate.internal.model.NetworkViewSet;
import org.baderlab.autoannotate.internal.model.io.CreationParameter;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class CreationParamsDialog extends JDialog {

	@Inject private ModelManager modelManager;
	
	
	@Inject
	public CreationParamsDialog(JFrame jFrame) {
		super(jFrame, true);
		setTitle("AutoAnnotate: Creation Parameters");
	}
	
	@AfterInjection
	public void createContents() {
		Optional<List<CreationParameter>> cpo =
			modelManager.
			getActiveNetworkViewSet()
			.flatMap(NetworkViewSet::getActiveAnnotationSet)
			.map(AnnotationSet::getCreationParameters)
			.flatMap(cp -> cp.isEmpty() ? Optional.empty() : Optional.of(cp));
			
        String text;
		if(cpo.isPresent())
			text = createText(cpo.get());
		else
			text = createEmptyText();
		
		JLabel textArea = new JLabel();
        textArea.setText(text);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        
        JPanel closePanel = new JPanel(new BorderLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        closePanel.add(closeButton, BorderLayout.EAST);
        
        setLayout(new BorderLayout());
        add(textArea, BorderLayout.CENTER);
        add(closePanel, BorderLayout.SOUTH);
        
        pack();
	}
	
	
	private String createText(Collection<CreationParameter> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		
		for(CreationParameter cp : params) {
			if(cp.isSeparator()) {
				sb.append("<br>");
			} else {
				sb.append("<b>").append(cp.getDisplayName()).append("</b>: ").append(cp.getDisplayValue()).append("<br>");
			}
		}
		
		sb.append("</html>");
		return sb.toString();
	}
	
	
	private String createEmptyText() {
		return 
			"<html>" +
			"Creation Parameters could not be found for this Annotation Set.<br><br>" +
			"This feature is only available for Annotation Sets created <br>with AutoAnnotate version 1.1 or later.<br>" +
			"</html>";
	}
	
}
