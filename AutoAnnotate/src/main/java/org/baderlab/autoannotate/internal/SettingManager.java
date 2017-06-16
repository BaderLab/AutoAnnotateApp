package org.baderlab.autoannotate.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import org.cytoscape.property.CyProperty;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingManager {
	
	@Inject private CyProperty<Properties> cyProperties;
	
	
	public <T> void setValue(Setting<T> setting, T value) {
		cyProperties.getProperties().setProperty(setting.getKey(), String.valueOf(value));
	}
	
	public <T> T getValue(Setting<T> setting) {
		String propertyValue = cyProperties.getProperties().getProperty(setting.getKey());
		if(propertyValue == null)
			return setting.getDefaultValue();
		
		try {
			Class<T> type = setting.getType();
			Method valueOf = type.getMethod("valueOf", String.class);
			if(valueOf != null && Modifier.isStatic(valueOf.getModifiers()))
				return type.cast(valueOf.invoke(null, propertyValue));
			else
				throw new IllegalArgumentException("Can't find valueOf() method in type: " + type); // this shouldn't happen
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return setting.getDefaultValue();
		}
	}
	
}
