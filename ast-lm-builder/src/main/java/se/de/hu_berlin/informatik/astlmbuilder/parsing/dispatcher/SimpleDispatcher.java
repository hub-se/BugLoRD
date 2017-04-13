package se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher;

import se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser.INodeGuesser;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.ITokenParser;

public class SimpleDispatcher implements IKeyWordDispatcher {

	final private ITokenParser parser;
	final private INodeGuesser guesser;
	
	public SimpleDispatcher(ITokenParser parser, INodeGuesser guesser) {
		this.parser = parser;
		this.guesser = guesser;
	}
	
	@Override
	public ITokenParser getParser() {
		return parser;
	}

	@Override
	public INodeGuesser getGuesser() {
		return guesser;
	}

}
