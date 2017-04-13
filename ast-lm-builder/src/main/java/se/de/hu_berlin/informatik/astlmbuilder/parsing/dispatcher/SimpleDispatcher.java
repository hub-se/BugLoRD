package se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher;

import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.ITokenParser;

public class SimpleDispatcher implements IKeyWordDispatcher {

	final private ITokenParser parser;
	
	public SimpleDispatcher(ITokenParser parser) {
		this.parser = parser;
	}
	
	@Override
	public ITokenParser getParser() {
		return parser;
	}

}
