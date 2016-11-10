package se.de.hu_berlin.informatik.benchmark.ranking;

import java.util.Collection;

public interface MarkedRanking<T,K> extends Ranking<T> {

    public boolean markElementWith(T element, K marker);
    
    public Collection<T> getMarkedElements();
    
    public K getMarker(T element);
    
    public boolean isMarked(T element);
	
}
