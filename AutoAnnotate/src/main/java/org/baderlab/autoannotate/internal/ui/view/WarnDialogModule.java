package org.baderlab.autoannotate.internal.ui.view;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.Properties;

import org.cytoscape.property.CyProperty;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Provides;

/**
 * Guice module to configure the warning dialogs used in the App.
 */
public class WarnDialogModule extends AbstractModule {
	
	public static final String CY_PROPERTY_WARN_CREATE   = "warnDialog.dontShowAgain.create";
	public static final String CY_PROPERTY_WARN_COLLAPSE = "warnDialog.dontShowAgain.collapse";
	public static final String CY_PROPERTY_WARN_LAYOUT   = "warnDialog.dontShowAgain.layout";
	public static final String CY_PROPERTY_WARN_LABEL    = "warnDialog.dontShowAgain.label";

	@BindingAnnotation @Retention(RUNTIME) public @interface Create {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Collapse {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Layout {}
	@BindingAnnotation @Retention(RUNTIME) public @interface Label {}
	
	@Override
	protected void configure() { }

	
	@Provides @Create
	public WarnDialog warnCreate(CyProperty<Properties> cyProperty) {
		WarnDialog warnDialog = new WarnDialog(cyProperty);
		warnDialog.setPropertyName(CY_PROPERTY_WARN_CREATE);
		warnDialog.setMessages(
			"AutoAnnotate will manage all annotations and groups in this network view.",
			"Any annotations or groups not created by AutoAnnotate will be removed.",
			"To manually create annotations and groups you may duplicate the network view at any time."
		);
		return warnDialog;
	}
	
	
	@Provides @Collapse
	public WarnDialog warnCollapse(CyProperty<Properties> cyProperty) {
		WarnDialog warnDialog = new WarnDialog(cyProperty);
		warnDialog.setPropertyName(CY_PROPERTY_WARN_COLLAPSE);
		warnDialog.setMessages(
			"Before collapsing clusters please go to the menu 'Edit > Preferences > Group Preferences...' and "
			+ "select 'Enable attribute aggregation'."
		);
		return warnDialog;
	}
	
	
	@Provides @Layout
	public WarnDialog warnLayout(CyProperty<Properties> cyProperty) {
		WarnDialog warnDialog = new WarnDialog(cyProperty);
		warnDialog.setPropertyName(CY_PROPERTY_WARN_LAYOUT);
		warnDialog.setMessages(
			"Layout clusters cannot be undone."
		);
		return warnDialog;
	}
	
	
	@Provides @Label
	public WarnDialog warnLabel(CyProperty<Properties> cyProperty) {
		WarnDialog warnDialog = new WarnDialog(cyProperty);
		warnDialog.setPropertyName(CY_PROPERTY_WARN_LAYOUT);
		warnDialog.setMessages(
			"Recalculating labels for selected clusters cannot be undone."
		);
		return warnDialog;
	}
}
