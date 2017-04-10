package se.de.hu_berlin.informatik.astlmbuilder.nodes;

import com.github.javaparser.Range;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

@Deprecated
public class BodyStmt extends Statement {

	public BodyStmt(final Range range) {
		super(range);
	}
	
	// simple constructor for the deserialization
	public BodyStmt() {
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
