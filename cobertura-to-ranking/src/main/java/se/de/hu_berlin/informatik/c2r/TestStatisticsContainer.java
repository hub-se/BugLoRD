package se.de.hu_berlin.informatik.c2r;

import java.util.ArrayList;
import java.util.List;

public class TestStatisticsContainer {

	private List<TestStatistics> statisticsList = new ArrayList<>();
	
	public void addStatistics(TestStatistics statistics) {
		statisticsList.add(statistics);
	}
	
}
