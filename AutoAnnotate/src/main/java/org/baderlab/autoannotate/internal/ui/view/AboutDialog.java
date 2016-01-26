package org.baderlab.autoannotate.internal.ui.view;

import java.awt.Insets;
import java.net.URL;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.baderlab.autoannotate.internal.BuildProperties;
import org.cytoscape.util.swing.OpenBrowser;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
    
    @Inject Provider<OpenBrowser> openBrowserProvider;
   
    @Inject
    public AboutDialog(JFrame jframe) {
        super(jframe, "About " + BuildProperties.APP_NAME, false);
        setResizable(false);

        //main panel for dialog box
        JEditorPane editorPane = new JEditorPane();
        editorPane.setMargin(new Insets(10,10,10,10));
        editorPane.setEditable(false);
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));
        
        URL logoURL = this.getClass().getResource("enrichmentmap_logo.png");

        editorPane.setText(
                "<html><body>"+
                "<table border='0'><tr>" +
                "<td width='125'></td>"+
                "<td width='200'>"+
                "<p align=center><b>" + BuildProperties.APP_NAME + " v" + BuildProperties.APP_VERSION + "</b><BR>" + 
                "A Cytoscape App<BR>" +
                "<BR></p>" +
                "</td>"+
                "<td width='125'><div align='right'><img height='77' width='125' src=\""+ /*logoURL.toString() +*/ "\" ></div></td>"+
                "</tr></table>" +
                "<p align=center>Finds clusters and visually annotates them with labels and groups..<BR>" +
                "<BR>" +
                "by Mike Kucera, Ruth Isserlin and Arkady Arkhangorodsky<BR>" +
                "(<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>" +
                "<BR>" +
                "App Homepage:<BR>" +
                "<a href='" + BuildProperties.APP_URL + "'>" + BuildProperties.APP_URL + "</a><BR>" +
                "<BR>" +
                "<font size='-1'>" + BuildProperties.BUILD_ID + "</font>" +
                "</p></body></html>"
            );
        
        setContentPane(editorPane);
    }

    private class HyperlinkAction implements HyperlinkListener {
        @SuppressWarnings("unused")
        JEditorPane pane;

        public HyperlinkAction(JEditorPane pane) {
            this.pane = pane;
        }

        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            	openBrowserProvider.get().openURL(event.getURL().toString());
            }
        }
    }	
}
