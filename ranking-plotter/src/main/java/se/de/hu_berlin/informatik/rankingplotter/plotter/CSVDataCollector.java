/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import se.de.hu_berlin.informatik.utils.tm.modules.stringprocessor.IStringProcessor;

/**
 * Takes a String of format: 'index \t ranking \t ... \t mod_id'
 * 
 * 
 * @author Simon Heiden
 */
public class CSVDataCollector implements IStringProcessor {

	
	private Map<Integer, List<Double>> appendsMap;
	private Map<Integer, List<Double>> changesMap;
	private Map<Integer, List<Double>> deletesMap;
	private Map<Integer, List<Double>> neighborsMap;
	private Map<Integer, List<Double>> allMap;
	
	/**
	 * Creates a new {@link CSVDataCollector} object.
	 */
	public CSVDataCollector() {
		appendsMap = new HashMap<>();
		changesMap = new HashMap<>();
		deletesMap = new HashMap<>();
		neighborsMap = new HashMap<>();
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
		if (line.endsWith("x")) {			
			if (allMap.containsKey(index)) {
				allMap.get(index).add(ranking);
			} else {
				allMap.put(index, new ArrayList<Double>());
				allMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith("a")) {			
			if (appendsMap.containsKey(index)) {
				appendsMap.get(index).add(ranking);
			} else {
				appendsMap.put(index, new ArrayList<Double>());
				appendsMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith("c")) {
			if (changesMap.containsKey(index)) {
				changesMap.get(index).add(ranking);
			} else {
				changesMap.put(index, new ArrayList<Double>());
				changesMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith("d")) {			
			if (deletesMap.containsKey(index)) {
				deletesMap.get(index).add(ranking);
			} else {
				deletesMap.put(index, new ArrayList<Double>());
				deletesMap.get(index).add(ranking);
			}
			return true;
		}
		if (line.endsWith("n")) {			
			if (neighborsMap.containsKey(index)) {
				neighborsMap.get(index).add(ranking);
			} else {
				neighborsMap.put(index, new ArrayList<Double>());
				neighborsMap.get(index).add(ranking);
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

		addData(dataTableCollection, appendsMap, "a");
		addData(dataTableCollection, changesMap, "c");
		addData(dataTableCollection, deletesMap, "d");
		addData(dataTableCollection, neighborsMap, "n");
		addData(dataTableCollection, allMap, "x");

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
			
			dataTableCollection.addData(mod_id, entry.getKey(), rankingSum, true);
		}
	}
}
