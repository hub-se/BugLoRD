package se.de.hu_berlin.informatik.astlmbuilder.nodes;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class UnknownNode extends Node {

	public UnknownNode(Range range) {
		super(range);
	}
	
	public UnknownNode() {
		super(null);
	}

	@Override
	public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> void accept(VoidVisitor<A> v, A arg) {
		// TODO Auto-generated method stub
		
	}


}
