package org.baderlab.autoannotate.internal.ui.view;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.baderlab.autoannotate.internal.ui.view.action.SummaryNetworkAction;
import org.cytoscape.property.CyProperty;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

/**
 * Guice module to configure the warning dialogs used in the App.
 */
public class WarnDialogModule extends AbstractModule {
	
	public static final String 
		CY_PROPERTY_WARN_HIDDEN    = "warnDialog.dontShowAgain.hidden",
		CY_PROPERTY_WARN_CREATE    = "warnDialog.dontShowAgain.create",
		CY_PROPERTY_WARN_COLLAPSE  = "warnDialog.dontShowAgain.collapse",
		CY_PROPERTY_WARN_LAYOUT    = "warnDialog.dontShowAgain.layout",
		CY_PROPERTY_WARN_LABEL     = "warnDialog.dontShowAgain.label",
		CY_PROPERTY_WARN_SUMMARY   = "warnDialog.dontShowAgain.summary",
		CY_PROPERTY_WARN_EM        = "warnDialog.dontShowAgain.em";

	@BindingAnnotation @Retention(RUNTIME) public @interface Hidden {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Create {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Collapse {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Layout {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Label {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Summary {}
	@BindingAnnotation @Retention(RUNTIME) public @interface EM {}
	
	@Override
	protected void configure() { }

	public static List<String> getPropertyKeys() {
		return Arrays.asList(
			CY_PROPERTY_WARN_HIDDEN,
			CY_PROPERTY_WARN_COLLAPSE, 
			CY_PROPERTY_WARN_CREATE, 
			CY_PROPERTY_WARN_LABEL, 
			CY_PROPERTY_WARN_LAYOUT,
			CY_PROPERTY_WARN_SUMMARY,
			CY_PROPERTY_WARN_EM
		); 
	}
	
	
	@Provides @EM 
	public WarnDialog warnEMInit(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_EM,
			"The AutoAnnotate app was used to find clusters in the network and highlight the most significant node in each cluster.",
			"Use the AutoAnnotate panel to change how clusters are shown, and to create/remove sets of clusters."
		);
	}
	
	
	@Provides @Hidden 
	public WarnDialog warnHidden(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_HIDDEN,
			"There are hidden nodes and/or edges in the current network view.",
			"Hidden nodes will not be included in clusters."
		).setAskToContinue(true);
	}
	
	@Provides @Create 
	public WarnDialog warnCreate(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_CREATE,
			"AutoAnnotate will manage all groups in this network view.",
			"To manually create groups you may duplicate the network view at any time."
		);
	}
	
	@Provides @Collapse
	public WarnDialog warnCollapse(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_COLLAPSE,
			"Warning: Collapsing or expanding clusters can be slow for large networks. "
			+ "Please try using the '" + SummaryNetworkAction.TITLE + "' command instead.",
			"Before collapsing clusters please go to the menu 'Edit > Preferences > Group Preferences...' and "
			+ "select 'Enable attribute aggregation'."
		).setAskToContinue(true);
	}
	
	@Provides @Layout
	public WarnDialog warnLayout(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_LAYOUT, 
			"Layout clusters cannot be undone."
		).setAskToContinue(true);
	}
	
	@Provides @Label
	public WarnDialog warnLabel(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_LABEL, 
			"Recalculating labels for selected clusters cannot be undone."
		).setAskToContinue(true);
	}
	
	@Provides @Summary
	public WarnDialog warnSummary(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_SUMMARY, 
			"Column values in summary network are aggregated using the Group aggregation settings.",
		    "To Edit the Group aggregation settings go to the menu 'Edit > Preferences > Group Preferences...'"
		);
	}
	
}
