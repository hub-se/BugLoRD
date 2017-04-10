package se.de.hu_berlin.informatik.astlmbuilder.nodes;

import java.util.List;

import com.github.javaparser.Range;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

@Deprecated
public class ExtendsStmt extends Statement {

	private List<ClassOrInterfaceType> extendsList;
	
	public ExtendsStmt(List<ClassOrInterfaceType> extendsList, final Range range) {
		super(range);
		this.extendsList = extendsList;
	}
	
	// a simple constructor
	public ExtendsStmt() {
		super(null);
	}

	public List<ClassOrInterfaceType> getExtends() {
		return extendsList;
	}
	
	public void setExtends( List<ClassOrInterfaceType> aExtendsList ) {
		extendsList = aExtendsList;
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
