/**
 * 
 */
package se.de.hu_berlin.informatik.rankingplotter.plotter.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Simon Heiden
 */
public class StatisticsCollection<T extends Enum<T>> {

	private final Map<T, List<Double[]>> statisticsMap;
	
	private final String identifier;
	
	public StatisticsCollection(String identifier) {
		super();
		this.identifier = identifier;
		Objects.requireNonNull(this.identifier);
		statisticsMap = new HashMap<>();
	}
	
	public StatisticsCollection() {
		this("");
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public boolean addValuePair(T tableIdentifier, Double... values) {
		return statisticsMap.computeIfAbsent(tableIdentifier, k -> new ArrayList<>()).add(values);
	}
	
	public void setValueList(T tableIdentifier, List<Double[]> values) {
		statisticsMap.put(tableIdentifier, values);
	}
	
	public List<Double[]> getStatistics(T tableIdentifier) {
		return statisticsMap.get(tableIdentifier);
	}
	
	public Map<T, List<Double[]>> getStatisticsmap() {
		return statisticsMap;
	}

}
