package org.baderlab.autoannotate.internal.ui.view.display;


/**
 * Determines how the most significant nodes in a cluster are sorted.
 */
public enum Significance {
	MINIMUM, 
	MAXIMUM, 
	GREATEST_MAGNITUDE;
	
	public String getDisplayName() {
		switch(this) {
			case MINIMUM: return "Minimum value";
			case MAXIMUM: return "Maximum value";
			case GREATEST_MAGNITUDE: return "Largest absolute value";
			default: return null;
		}
	}
	
	public boolean isMoreSignificant(Number arg1, Number arg2) {
		var val1 = arg1 == null ? Double.NaN : arg1.doubleValue();
		var val2 = arg2 == null ? Double.NaN : arg2.doubleValue();
		
		if(Double.isNaN(val1) && Double.isNaN(val2)) {
			return false;
		} else if(Double.isNaN(val1)) {
			return false;
		} else if(Double.isNaN(val2)) {
			return true;
		}
		
		switch(this) {
			case MINIMUM: return val1 < val2;
			case MAXIMUM: return val1 > val2;
			case GREATEST_MAGNITUDE: return Math.abs(val1) > Math.abs(val2);
			default: return false;
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
