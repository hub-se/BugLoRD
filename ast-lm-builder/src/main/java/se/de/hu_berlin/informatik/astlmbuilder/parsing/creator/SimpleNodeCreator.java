package se.de.hu_berlin.informatik.astlmbuilder.parsing.creator;

import se.de.hu_berlin.informatik.astlmbuilder.parsing.parser.ITokenParser;

/**
 * A simple implementation of the node creator (only for testing at the moment)
 */
public class SimpleNodeCreator implements INodeCreator {

    final private ITokenParser parser;

    public SimpleNodeCreator(ITokenParser parser) {
        this.parser = parser;
    }

    @Override
    public ITokenParser getParser() {
        return parser;
    }

}
