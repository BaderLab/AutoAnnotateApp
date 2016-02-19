package org.baderlab.autoannotate.internal;

public class Setting<T> {
	
	// Note: WarnDialog settings are in WarnDialogModule
	
	public final static Setting<Integer> DEFAULT_MAX_WORDS = new Setting<Integer>("defaultMaxWords", Integer.class, 4);
	public final static Setting<Boolean> OVERRIDE_GROUP_LABELS = new Setting<Boolean>("overrideGroupLabels", Boolean.class, true);
		
	private final String key;
	private final Class<T> type;
	private final T defaultValue;
	
	private Setting(String key, Class<T> type, T defaultValue) {
		this.key = key;
		this.type = type;
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public Class<T> getType() {
		return type;
	}

	public T getDefaultValue() {
		return defaultValue;
	}


}
