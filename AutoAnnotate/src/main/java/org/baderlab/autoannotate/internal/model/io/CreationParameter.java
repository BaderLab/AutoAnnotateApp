package org.baderlab.autoannotate.internal.model.io;

/**
 * Initial creation parameters for the Annotation Sets are saved
 * so the user may view them later.
 * 
 * @author mkucera
 *
 */
public class CreationParameter {

	private String displayName;
	private String displayValue;
	private boolean separator = false;
	
	public CreationParameter() {
	}
	
	
	/**
	 * It probably makes more sense to have groups of creation parameters, 
	 * or use the composite pattern, but this is just easier.
	 */
	public static CreationParameter separator() {
		CreationParameter cp = new CreationParameter(null, null);
		cp.separator = true;
		return cp;
	}
	
	public CreationParameter(String displayName, String displayValue) {
		this.displayName = displayName;
		this.displayValue = displayValue;
	}


	public String getDisplayName() {
		return displayName;
	}


	public String getDisplayValue() {
		return displayValue;
	}


	public boolean isSeparator() {
		return separator;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		CreationParameter other = (CreationParameter) obj;
		if(displayName == null) {
			if(other.displayName != null)
				return false;
		} else if(!displayName.equals(other.displayName))
			return false;
		return true;
	}
	
	
	
}
