package org.baderlab.autoannotate.internal.layout;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.swing.Action;

import org.baderlab.autoannotate.internal.model.AnnotationSet;
import org.baderlab.autoannotate.internal.ui.view.action.LayoutClustersAction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ClusterLayoutManager {

	public static final String COMMAND_ARG = "autoannotate.command.arg";
	
	@Inject private Provider<CoseLayoutAlgorithm> coseLayoutProvider;
	@Inject private Provider<GridLayoutAlgorithm> gridLayoutProvider;
	@Inject private LayoutClustersAction.Factory layoutClustersActionFactory;
	
	public static enum Algorithm {
		COSE       ("cose",       "Layout Clusters to Minimize Overlap"),
		COSE_GROUP ("cose_group", "Layout Clusters to Minimize Overlap (Group single nodes together)"),
		GRID       ("grid",       "Grid Layout");
		
		private final String commandArg;
		private final String menuName;
		
		private Algorithm(String commandArg, String menuName) {
			this.commandArg = commandArg;
			this.menuName = menuName;
		}
		public String getCommandArg() {
			return commandArg;
		}
		public String getMenuName() {
			return menuName;
		}
	}
	
	
	public Action getAction(Algorithm alg) {
		return getAction(alg, null);
	}
	
	public Action getAction(Algorithm alg, @Nullable AnnotationSet as) {
		CoseLayoutAlgorithm coseLayout = coseLayoutProvider.get();
		Action action;
		switch(alg) {
			case COSE:
				CoseLayoutContext ctx1 = coseLayout.createLayoutContext();
				action = layoutClustersActionFactory.create(coseLayout, ctx1, as);
				break;
			case COSE_GROUP:
				CoseLayoutContext ctx2 = coseLayout.createLayoutContext();
				ctx2.useCatchallCluster = true;
				action = layoutClustersActionFactory.create(coseLayout, ctx2, as);
				break;
			case GRID:
				GridLayoutAlgorithm gridLayout = gridLayoutProvider.get();
				action = layoutClustersActionFactory.create(gridLayout, gridLayout.createLayoutContext(), as);
				break;
			default:
				return null;
		}
		action.putValue(Action.NAME, alg.getMenuName());
		action.putValue(COMMAND_ARG, alg.getCommandArg());
		return action;
	}
	
	public Algorithm getAlgorithmForCommand(String command) {
		for(Algorithm a : Algorithm.values()) {
			if(command.equalsIgnoreCase(a.getCommandArg())) {
				return a;
			}
		}
		return null;
	}
	
	public List<Action> getActions() {
		return Arrays.stream(Algorithm.values()).map(this::getAction).collect(Collectors.toList());
	}
	
	public List<Algorithm> getAlgorithms() {
		return Arrays.asList(Algorithm.values());
	}
	
	public List<String> getCommandArgs() {
		return Arrays.stream(Algorithm.values()).map(Algorithm::getCommandArg).collect(Collectors.toList());
	}
	
}
