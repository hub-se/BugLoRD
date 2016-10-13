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
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DataTableCollection;
import se.de.hu_berlin.informatik.rankingplotter.plotter.datatables.DiffDataTableCollection;
import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.StringProcessor;

/**
 * Takes a String of format: 'index \t ranking \t ... \t mod_id'
 * 
 * 
 * @author Simon Heiden
 */
public class CSVDataCollector implements StringProcessor<DataTableCollection> {

	
	private Map<Integer, List<Double>> unsignificantChangesMap;
	private Map<Integer, List<Double>> lowSignificanceChangesMap;
	private Map<Integer, List<Double>> mediumSignificanceChangesMap;
	private Map<Integer, List<Double>> highSignificanceChangesMap;
	private Map<Integer, List<Double>> crucialSignificanceChangesMap;
	private Map<Integer, List<Double>> allMap;
	
	private Map<Integer, List<Double>> changesMap;
	private Map<Integer, List<Double>> deletesMap;
	private Map<Integer, List<Double>> insertsMap;
	private Map<Integer, List<Double>> unknownsMap;
	
	private Map<Integer, List<Double>> hitAt1Map;
	private Map<Integer, List<Double>> hitAt5Map;
	private Map<Integer, List<Double>> hitAt10Map;
	private Map<Integer, List<Double>> hitAt20Map;
	private Map<Integer, List<Double>> hitAt30Map;
	private Map<Integer, List<Double>> hitAt50Map;
	private Map<Integer, List<Double>> hitAt100Map;
	private Map<Integer, List<Double>> hitAtInfMap;
	
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
		
		changesMap = new HashMap<>();
		deletesMap = new HashMap<>();
		insertsMap = new HashMap<>();
		unknownsMap = new HashMap<>();
		
		hitAt1Map = new HashMap<>();
		hitAt5Map = new HashMap<>();
		hitAt10Map = new HashMap<>();
		hitAt20Map = new HashMap<>();
		hitAt30Map = new HashMap<>();
		hitAt50Map = new HashMap<>();
		hitAt100Map = new HashMap<>();
		hitAtInfMap = new HashMap<>();
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
			return updateMap(allMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.UNSIGNIFICANT_ID)) {
			return updateMap(unsignificantChangesMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.LOW_SIGNIFICANCE_ID)) {
			return updateMap(lowSignificanceChangesMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.MEDIUM_SIGNIFICANCE_ID)) {
			return updateMap(mediumSignificanceChangesMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIGH_SIGNIFICANCE_ID)) {
			return updateMap(highSignificanceChangesMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.CRUCIAL_SIGNIFICANCE_ID)) {	
			return updateMap(crucialSignificanceChangesMap, index, ranking);
		}
		
		if (line.endsWith(DiffDataTableCollection.CHANGE_ID)) {	
			return updateMap(changesMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.DELETE_ID)) {	
			return updateMap(deletesMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.INSERT_ID)) {	
			return updateMap(insertsMap, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.UNKNOWN_ID)) {	
			return updateMap(unknownsMap, index, ranking);
		}
		
		if (line.endsWith(DiffDataTableCollection.HIT_AT_1)) {	
			return updateMap(hitAt1Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_5)) {	
			return updateMap(hitAt5Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_10)) {	
			return updateMap(hitAt10Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_20)) {	
			return updateMap(hitAt20Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_30)) {	
			return updateMap(hitAt30Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_50)) {	
			return updateMap(hitAt50Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_100)) {	
			return updateMap(hitAt100Map, index, ranking);
		}
		if (line.endsWith(DiffDataTableCollection.HIT_AT_INF)) {	
			return updateMap(hitAtInfMap, index, ranking);
		}
		return false;
	}
	
	private boolean updateMap(Map<Integer,List<Double>> map, int index, double ranking) {
		if (map.containsKey(index)) {
			map.get(index).add(ranking);
		} else {
			map.put(index, new ArrayList<Double>());
			map.get(index).add(ranking);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResult()
	 */
	public DataTableCollection getResult() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor#getResultFromCollectedItems()
	 */
	@Override
	public DataTableCollection getResultFromCollectedItems() {
		DiffDataTableCollection dataTableCollection = new DiffDataTableCollection();

		addData(dataTableCollection, unsignificantChangesMap, ChangeWrapper.SIGNIFICANCE_NONE);
		addData(dataTableCollection, lowSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_LOW);
		addData(dataTableCollection, mediumSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_MEDIUM);
		addData(dataTableCollection, highSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_HIGH);
		addData(dataTableCollection, crucialSignificanceChangesMap, ChangeWrapper.SIGNIFICANCE_CRUCIAL);
		addData(dataTableCollection, allMap, ChangeWrapper.SIGNIFICANCE_ALL);
		
		addData(dataTableCollection, changesMap, DiffDataTableCollection.CHANGE_ID);
		addData(dataTableCollection, deletesMap, DiffDataTableCollection.CHANGE_ID);
		addData(dataTableCollection, insertsMap, DiffDataTableCollection.CHANGE_ID);
		addData(dataTableCollection, unknownsMap, DiffDataTableCollection.CHANGE_ID);
		
		addData(dataTableCollection, hitAt1Map, DiffDataTableCollection.HIT_AT_1);
		addData(dataTableCollection, hitAt5Map, DiffDataTableCollection.HIT_AT_5);
		addData(dataTableCollection, hitAt10Map, DiffDataTableCollection.HIT_AT_10);
		addData(dataTableCollection, hitAt20Map, DiffDataTableCollection.HIT_AT_20);
		addData(dataTableCollection, hitAt30Map, DiffDataTableCollection.HIT_AT_30);
		addData(dataTableCollection, hitAt50Map, DiffDataTableCollection.HIT_AT_50);
		addData(dataTableCollection, hitAt100Map, DiffDataTableCollection.HIT_AT_100);
		addData(dataTableCollection, hitAtInfMap, DiffDataTableCollection.HIT_AT_INF);

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
