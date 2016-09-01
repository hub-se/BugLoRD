package se.de.hu_berlin.informatik.astlmbuilder;

import java.util.List;

import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

public class ExtendsStmt extends Statement {

	private List<ClassOrInterfaceType> extendsList;
	
	@SuppressWarnings("deprecation")
	public ExtendsStmt(List<ClassOrInterfaceType> extendsList, 
			final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
		super(beginLine, beginColumn, endLine, endColumn);
		this.extendsList = extendsList;
	}

	public List<ClassOrInterfaceType> getExtends() {
		return extendsList;
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
