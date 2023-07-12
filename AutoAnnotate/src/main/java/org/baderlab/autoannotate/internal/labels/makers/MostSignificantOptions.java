package org.baderlab.autoannotate.internal.labels.makers;

public class MostSignificantOptions {

	
	public static enum Significance {
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
		
		public Boolean isMoreSignificant(Number arg1, Number arg2) {
			var val1 = arg1.doubleValue();
			var val2 = arg2.doubleValue();
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
	
	
	private final String significanceColumn;
	private final Significance significance;
	
	
	public MostSignificantOptions(String significanceColumn, Significance significance) {
		this.significanceColumn = significanceColumn;
		this.significance = significance;
	}
	
	public MostSignificantOptions() {
		this(null, Significance.getDefault());
	}
	
	public String getSignificanceColumn() {
		return significanceColumn;
	}
	
	public Significance getSignificance() {
		return significance;
	}

	
	@Override
	public String toString() {
		return "MostSignificantOptions[significanceColumn=" + significanceColumn + ", significance=" + significance + "]";
	}
	
}
