package org.baderlab.autoannotate.internal.ui.view.action;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.autoannotate.internal.ui.view.summary.SummaryNetworkDialogSettings;

import com.google.inject.Singleton;

@Singleton
public class SummaryNetworkActionSettings {

	private final Map<Long,SummaryNetworkDialogSettings> dialogSettingsMap = new HashMap<>();
	
	public SummaryNetworkDialogSettings get(Long suid) {
		return dialogSettingsMap.get(suid);
	}
	
	public void put(Long suid, SummaryNetworkDialogSettings settings) {
		dialogSettingsMap.put(suid, settings);
	}
}
