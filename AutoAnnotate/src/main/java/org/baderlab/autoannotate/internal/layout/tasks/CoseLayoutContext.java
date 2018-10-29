package org.baderlab.autoannotate.internal.layout.tasks;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cose.CoSEConstants;

public class CoseLayoutContext implements TunableValidator {
	
	public enum LayoutQuality {
		PROOF(LayoutConstants.PROOF_QUALITY, "Proof"),
		DEFAULT(LayoutConstants.DEFAULT_QUALITY, "Default"),
		DRAFT(LayoutConstants.DRAFT_QUALITY, "Draft");

		private int value;
		private String name;

		private LayoutQuality(final int value, final String name) {
			this.value = value;
			this.name = name;
		}
		
		public int getValue() {
			return value;
		}
		
		@Override
		public String toString() { 
			return name;
		}
	}
	
	public LayoutQuality layoutQuality = LayoutQuality.DEFAULT;

	@Tunable(description = "Layout quality:", gravity = 1.0, context="both", longDescription="Layout quality; allowed values are ```Proof```, ```Default``` and ```Draft```", exampleStringValue="Default")
	public ListSingleSelection<LayoutQuality> getLayoutQuality() {
		ListSingleSelection<LayoutQuality> list = new ListSingleSelection<>(LayoutQuality.PROOF, LayoutQuality.DEFAULT,
				LayoutQuality.DRAFT);
		list.setSelectedValue(layoutQuality);

		return list;
	}

	public void setLayoutQuality(final ListSingleSelection<LayoutQuality> list) {
		layoutQuality = list.getSelectedValue();
	}
	
	@Tunable(description = "Incremental:", gravity = 1.1, context="both", longDescription="Incremental; whether the algorithm will be applied incrementally; boolean values only, ```true``` or ```false```; defaults to ```false```", exampleStringValue="false")
	public boolean incremental = LayoutConstants.DEFAULT_INCREMENTAL;
	
	@Tunable(description = "Ideal edge length:", tooltip = "Any positive integer", gravity = 2.0, context="both", longDescription="Ideal edge length, any positive integer", exampleStringValue="50")
	public int idealEdgeLength = CoSEConstants.DEFAULT_EDGE_LENGTH;
	@Tunable(description = "Spring strength (0-100):", gravity = 2.1, context="both", longDescription="Spring strength (0-100)", exampleStringValue="50")
	public int springStrength = 50;
	@Tunable(description = "Repulsion strength (0-100):", gravity = 2.2, context="both", longDescription="Repulsion strength (0-100)", exampleStringValue="50")
	public int repulsionStrength = 50;
	@Tunable(description = "Gravity strength (0-100):", gravity = 2.3, context="both", longDescription="Gravity strength (0-100)", exampleStringValue="50")
	public int gravityStrength = 50;
	@Tunable(description = "Compound gravity strength (0-100):", gravity = 2.4, context="both", longDescription="Compound gravity strength (0-100)", exampleStringValue="50")
	public int compoundGravityStrength = 50;
	@Tunable(description = "Gravity range (0-100):", gravity = 2.5, context="both", longDescription="Gravity range (0-100)", exampleStringValue="50")
	public int gravityRange = 53;
	@Tunable(description = "Compound gravity range (0-100):", gravity = 2.6, context="both", longDescription="Compound gravity range (0-100)", exampleStringValue="50")
	public int compoundGravityRange = 50;
	
	@Tunable(description = "Use smart edge length calculation:", gravity = 3.0, context="both", longDescription="Use smart edge length calculation; boolean values only, ```true``` or ```false```; defaults to ```true```", exampleStringValue="true")
	public boolean smartEdgeLengthCalc = CoSEConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION;
	@Tunable(description = "Use smart repulsion range calculation:", gravity = 3.1, context="both", longDescription="Use smart repulsion range calculation; boolean values only, ```true``` or ```false```; defaults to ```true```", exampleStringValue="true")
	public boolean smartRepulsionRangeCalc = CoSEConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION;
	
	// Extra tunables
	@Tunable(description = "Experimental: tree all non-clustered nodes as being in a catch-all cluster", gravity = 3.2, context="both", exampleStringValue="true")
	public boolean useCatchallCluster = false;
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}

	@Override
	public String toString() {
		return "CoseLayoutContext [layoutQuality=" + layoutQuality + ", incremental=" + incremental
				+ ", idealEdgeLength=" + idealEdgeLength + ", springStrength=" + springStrength + ", repulsionStrength="
				+ repulsionStrength + ", gravityStrength=" + gravityStrength + ", compoundGravityStrength="
				+ compoundGravityStrength + ", gravityRange=" + gravityRange + ", compoundGravityRange="
				+ compoundGravityRange + ", smartEdgeLengthCalc=" + smartEdgeLengthCalc + ", smartRepulsionRangeCalc="
				+ smartRepulsionRangeCalc + ", useCatchallCluster=" + useCatchallCluster + "]";
	}

}
