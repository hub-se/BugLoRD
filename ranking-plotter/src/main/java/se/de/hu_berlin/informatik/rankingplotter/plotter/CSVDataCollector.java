/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.changechecker.ChangeWrapper;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DiffDataTableCollection;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor;

/**
 * Takes a String of format: 'index \t ranking \t ... \t mod_id'
 * 
 * 
 * @author Simon Heiden
 */
public class CSVDataCollector implements IStringProcessor {

	
	private Map<Integer, List<Double>> unsignificantChangesMap;
	private Map<Integer, List<Double>> lowSignificanceChangesMap;
	private Map<Integer, List<Double>> mediumSignificanceChangesMap;
	private Map<Integer, List<Double>> highSignificanceChangesMap;
	private Map<Integer, List<Double>> crucialSignificanceChangesMap;
	private Map<Integer, List<Double>> allMap;
	
	/**
	 * Creates a new {@link CSVDataCollector} object.
	 */
	public CSVDataCollector() {
		unsignificantChangesMap = new HashMap<>();
		lowSignificanceChangesMap = new HashMap<>();
		mediumSignificanceChangesMap = new HashMap<>();
		highSignificanceChangesMap = new HashMap<>();
		crucialSignificanceChangesMap = new HashMap<>();
		allMap = new HashMap<>();
	}

	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.stringprocessor.IStringProcessor#process(java.lang.String)
	 */
	public boolean process(String line) {
		//format: index \t ranking \t ... \t mod_id
		int pos = line.indexOf('\t');
		int pos2 = line.indexOf('\t', pos+1);
		if (pos == -1 || pos2 == -1) {
			return false;
		}
		int index = Integer.parseInt(line.substring(0, pos));
		double ranking = Double.parseDouble(line.substring(pos+1, pos2));
		if (line.endsWith(DiffDataTableCollection.ALL_ID)) {			
			if (allMap.containsKey(index)) {
				allMap.get(index).add(ranking);
			} else {
				allMap.put(index, new ArrayList<Double>());
				allMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith(DiffDataTableCollection.UNSIGNIFICANT_ID)) {			
			if (unsignificantChangesMap.containsKey(index)) {
				unsignificantChangesMap.get(index).add(ranking);
			} else {
				unsignificantChangesMap.put(index, new ArrayList<Double>());
				unsignificantChangesMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith(DiffDataTableCollection.LOW_SIGNIFICANCE_ID)) {
			if (lowSignificanceChangesMap.containsKey(index)) {
				lowSignificanceChangesMap.get(index).add(ranking);
			} else {
				lowSignificanceChangesMap.put(index, new ArrayList<Double>());
				lowSignificanceChangesMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith(DiffDataTableCollection.MEDIUM_SIGNIFICANCE_ID)) {			
			if (mediumSignificanceChangesMap.containsKey(index)) {
				mediumSignificanceChangesMap.get(index).add(ranking);
			} else {
				mediumSignificanceChangesMap.put(index, new ArrayList<Double>());
				mediumSignificanceChangesMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith(DiffDataTableCollection.HIGH_SIGNIFICANCE_ID)) {			
			if (highSignificanceChangesMap.containsKey(index)) {
				highSignificanceChangesMap.get(index).add(ranking);
			} else {
				highSignificanceChangesMap.put(index, new ArrayList<Double>());
				highSignificanceChangesMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith(DiffDataTableCollection.CRUCIAL_SIGNIFICANCE_ID)) {			
			if (crucialSignificanceChangesMap.containsKey(index)) {
				crucialSignificanceChangesMap.get(index).add(ranking);
			} else {
				crucialSignificanceChangesMap.put(index, new ArrayList<Double>());
				crucialSignificanceChangesMap.get(index).add(ranking);
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResult()
	 */
	public Object getResult() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResultFromCollectedItems()
	 */
	@Override
	public Object getResultFromCollectedItems() {
		DiffDataTableCollection dataTableCollection = new DiffDataTableCollection();

		addData(dataTableCollection, unsignificantChangesMap, ChangeWrapper.SIGNIFICANCE_NONE);
		addData(dataTableCollection, lowSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_LOW);
		addData(dataTableCollection, mediumSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_MEDIUM);
		addData(dataTableCollection, highSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_HIGH);
		addData(dataTableCollection, crucialSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_CRUCIAL);
		addData(dataTableCollection, allMap, ChangeWrapper.SIGNIFICANCE_ALL);

		return dataTableCollection;
	}


	private void addData(DiffDataTableCollection dataTableCollection, Map<Integer, List<Double>> map, String mod_id) {
		for (Entry<Integer, List<Double>> entry : map.entrySet()) {
			if (dataTableCollection.getMaxX() < entry.getKey()) {
				dataTableCollection.setMaxX(entry.getKey());
			}
			double rankingSum = 0;
			for (double ranking : entry.getValue()) {
				rankingSum += ranking;
			}
			rankingSum /= entry.getValue().size();
			
			dataTableCollection.addData(mod_id, entry.getKey(), rankingSum);
		}
	}
}
