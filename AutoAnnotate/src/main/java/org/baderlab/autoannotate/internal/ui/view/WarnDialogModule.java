package org.baderlab.autoannotate.internal.ui.view;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.cytoscape.property.CyProperty;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

/**
 * Guice module to configure the warning dialogs used in the App.
 */
public class WarnDialogModule extends AbstractModule {
	
	public static final String 
		CY_PROPERTY_WARN_CREATE    = "warnDialog.dontShowAgain.create",
		CY_PROPERTY_WARN_COLLAPSE  = "warnDialog.dontShowAgain.collapse",
		CY_PROPERTY_WARN_LAYOUT    = "warnDialog.dontShowAgain.layout",
		CY_PROPERTY_WARN_LABEL     = "warnDialog.dontShowAgain.label",
		CY_PROPERTY_WARN_MAX_WORDS = "warnDialog.dontShowAgain.maxWords";

	@BindingAnnotation @Retention(RUNTIME) public @interface Create {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Collapse {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Layout {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Label {}
	@BindingAnnotation @Retention(RUNTIME) public @interface MaxWords {}
	
	@Override
	protected void configure() { }

	public static List<String> getPropertyKeys() {
		return Arrays.asList(
			CY_PROPERTY_WARN_COLLAPSE, 
			CY_PROPERTY_WARN_CREATE, 
			CY_PROPERTY_WARN_LABEL, 
			CY_PROPERTY_WARN_LAYOUT,
			CY_PROPERTY_WARN_MAX_WORDS
		); 
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
			"Before collapsing clusters please go to the menu 'Edit > Preferences > Group Preferences...' and "
			+ "select 'Enable attribute aggregation'."
		);
	}
	
	@Provides @Layout
	public WarnDialog warnLayout(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_LAYOUT, 
			"Layout clusters cannot be undone."
		);
	}
	
	@Provides @Label
	public WarnDialog warnLabel(CyProperty<Properties> cyProperty) {
		return new WarnDialog(cyProperty, CY_PROPERTY_WARN_LABEL, 
			"Recalculating labels for selected clusters cannot be undone."
		);
	}
	
	@Provides @MaxWords
	public WarnDialog warnMaxWords(CyProperty<Properties> cyProperty) {
		WarnDialog warnDialog = new WarnDialog(cyProperty, CY_PROPERTY_WARN_MAX_WORDS, 
			"For setting to take effect please select \"Recalculate Labels\" from the AutoAnnotate panel menu."
		);
		warnDialog.setAskToContinue(false);
		return warnDialog;
	}
	
}
