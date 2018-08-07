package org.baderlab.autoannotate.internal.ui.view.copy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.table.AbstractTableModel;

import org.baderlab.autoannotate.internal.model.AnnotationSet;

@SuppressWarnings("serial")
public class AnnotationSetTableModel extends AbstractTableModel {
	
	static final Boolean DEFAULT_SELECTION_STATE = true;
	
	private List<AnnotationSet> annotationSets;
	private Map<AnnotationSet,Boolean> selected = new HashMap<>();
	
	public AnnotationSetTableModel(List<AnnotationSet> annotationSets) {
		this.annotationSets = annotationSets == null ? Collections.emptyList() : annotationSets;
	}

	@Override
	public int getRowCount() {
		return annotationSets.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 0;
	}

	@Override
	public Class<?> getColumnClass(int col) {
		if(col == 0)
			return Boolean.class;
		if(col == 1)
			return String.class;
		return null;
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		if(col == 0)
			return isSelected(annotationSets.get(row));
		if(col == 1)
			return annotationSets.get(row).getName();
		return null;
	}
	
	public boolean isSelected(AnnotationSet as) {
		return selected.getOrDefault(as, DEFAULT_SELECTION_STATE);
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		AnnotationSet as = annotationSets.get(row);
		selected.put(as, (Boolean)value);
		fireTableCellUpdated(row, col);
	}
	
	public void selectAll() {
		for(AnnotationSet as : annotationSets)
			selected.put(as, true);
		fireTableDataChanged();
	}
	
	public void selectNone() {
		for(AnnotationSet as : annotationSets)
			selected.put(as, false);
		fireTableDataChanged();
	}
	
	public List<AnnotationSet> getSelectedAnnotationSets() {
		return annotationSets.stream().filter(this::isSelected).collect(Collectors.toList());
	}
}