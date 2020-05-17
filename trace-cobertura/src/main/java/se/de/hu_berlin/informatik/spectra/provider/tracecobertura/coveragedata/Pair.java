package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.coveragedata;

import java.io.Serializable;

import se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data.CoverageIgnore;

@CoverageIgnore
public class Pair<A,B> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5514962526390628213L;
	
	private B second;
	private A first;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public B getSecond() {
		return second;
	}

	public void setSecond(B second) {
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public void setFirst(A first) {
		this.first = first;
	}
	
	
}
