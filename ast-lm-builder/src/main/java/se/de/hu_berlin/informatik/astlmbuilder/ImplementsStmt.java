package se.de.hu_berlin.informatik.astlmbuilder;

import java.util.List;

import com.github.javaparser.Range;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class ImplementsStmt extends Statement {

	private List<ClassOrInterfaceType> implementsList;
	
	public ImplementsStmt(List<ClassOrInterfaceType> implementsList, final Range range) {
		super(range);
		this.implementsList = implementsList;
	}

	// simple constructor for the deserialization
	public ImplementsStmt() {
		super();
	}
	
	public List<ClassOrInterfaceType> getImplements() {
		return implementsList;
	}
	
	public void setImplements( List<ClassOrInterfaceType> aImplementsList ) {
		implementsList = aImplementsList;
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
