package se.de.hu_berlin.informatik.astlmbuilder.parsing.parser;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.IKeyWordDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.SimpleDispatcher;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser.SimpleNodeGuesser;

/**
 * A simple implementation of the token parser
 */
public class SimpleTokenParser implements ITokenParser {

	final private IKeyWordProvider<String> kwp;
	final private IKeyWordDispatcher dispatcher;
	
	public SimpleTokenParser(IKeyWordProvider<String> kwp) {
		this.kwp = kwp;
		this.dispatcher = new SimpleDispatcher(this, new SimpleNodeGuesser());
	}
	
	@Override
	public IKeyWordProvider<String> getKeyWordProvider() {
		return kwp;
	}
	
	@Override
	public IKeyWordDispatcher getDispatcher() {
		return dispatcher;
	}

}
