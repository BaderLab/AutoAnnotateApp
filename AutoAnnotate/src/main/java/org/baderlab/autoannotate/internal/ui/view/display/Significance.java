package org.baderlab.autoannotate.internal.ui.view.display;

import java.util.Comparator;

/**
 * Determines how the most significant nodes in a cluster are sorted.
 */
public enum Significance {
	MINIMUM, 
	MAXIMUM, 
	GREATEST_MAGNITUDE;
	
	public String getDisplayName() {
		switch(this) {
		default:
		case MINIMUM: return "Minimum value";
		case MAXIMUM: return "Maximum value";
		case GREATEST_MAGNITUDE: return "Largest absolute value";
		}
	}
	
	
	public Comparator<Number> comparator() {
		switch(this) {
		default:
		case MINIMUM: 
			return Comparator.nullsLast(Comparator.comparingDouble(Number::doubleValue));
		case MAXIMUM: 
			return Comparator.nullsLast(Comparator.comparingDouble(Number::doubleValue).reversed());
		case GREATEST_MAGNITUDE:
			return Comparator.nullsLast(Comparator.comparingDouble((Number n) -> Math.abs(n.doubleValue())).reversed());
		}
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
	
	public static Significance getDefault() {
		return MINIMUM;
	}
}
