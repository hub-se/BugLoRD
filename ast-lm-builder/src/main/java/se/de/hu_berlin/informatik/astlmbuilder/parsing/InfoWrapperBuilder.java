package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.CharLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.type.Type;

import se.de.hu_berlin.informatik.astlmbuilder.parsing.VariableInfoWrapper.VariableScope;

/**
 * Class to build the info wrapper objects that store something similar to a
 * symbol table for nodes
 *
 */
public class InfoWrapperBuilder {

	// just for convenience
	private String defStrValue = VariableInfoWrapper.UNKNOWN_STR_VALUE;
	
	/**
	 * Collects all relevant data like variable names, scopes and types from a
	 * node and its parents and stores them into an information wrapper object.
	 * 
	 * @param aNode
	 * The node of interest
	 * @return A information wrapper containing data
	 */
	public InformationWrapper buildInfoWrapperForNode(Node aNode) {
		List<Class<? extends Node>> classHistory = getClassHistory(aNode);
		List<VariableInfoWrapper> symbolTable = new ArrayList<VariableInfoWrapper>();
		List<Optional<Node>> nodeHistory = getNodeHistory(aNode, symbolTable);

		InformationWrapper result = new InformationWrapper(nodeHistory, classHistory, symbolTable);
		return result;
	}

	private List<Optional<Node>> getNodeHistory(Node aNode, List<VariableInfoWrapper> aSymbolTable) {
		
		if( aNode == null ) {
			return null;
		}
		
		List<Optional<Node>> result = new ArrayList<Optional<Node>>();
		addAllParentsToHistory( aNode, result, aSymbolTable );
		
		return result;
	}
	
	/**
	 * Given the type of a node the rest of the info wrapper can be build
	 * @param aNode
	 * The node with a variable declaration of some type
	 * @param aType
	 * The type of the variable that is declared or passed as argument
	 * @param aLastKnownValue
	 * The first value is also the last known when dealing with declarations
	 * @return
	 * A variable information wrapper object
	 */
	private VariableInfoWrapper buildVarInfoWrapper( Node aNode, String aType, String aName, String aLastKnownValue ) {
		boolean primitive = false;
		VariableScope scope = VariableScope.UNKNOWN;

		primitive = hasPrimitiveType( aType );
		scope = getScope( aNode );
		
		return new VariableInfoWrapper( aType, aName, aLastKnownValue, primitive, scope, aNode );
	}
	
	/**
	 * Those are usually declarations of global variables
	 * @param aNode
	 * @return
	 */
	private VariableInfoWrapper buildVarInfoWrapperFromFieldDeclaration( FieldDeclaration aNode ) {
		
		String name = defStrValue;
		String type = defStrValue;
		String lastKnownValue = defStrValue;
		
		VariableDeclarator vd = aNode.getVariable(0);
		if( vd != null ) {
			Type t = vd.getType();
			name = vd.getNameAsString();
			if( t != null) {
				type = t.toString().trim().toLowerCase(); // to bad there is no getName from types
			}
			
			// this only makes sense for objects that can easily be converted to strings and reconstructed
			// for everything else we need to access the node laster on
			if( hasPrimitiveValue( vd.getInitializer().isPresent() ? vd.getInitializer().get() : null ) ) {
				lastKnownValue = vd.getInitializer().get().toString();
			}
		}
		
		return buildVarInfoWrapper( aNode, type, name, lastKnownValue );
	}
	
	/**
	 * Those are usually declarations of arguments
	 * @param aNode
	 * @return
	 */
	private VariableInfoWrapper buildVarInfoWrapperFromParameter( Parameter aNode ) {
		String name = defStrValue;
		String type = defStrValue;
		String lastKnownValue = defStrValue;	
		
		type = aNode.getType().toString().trim().toLowerCase();
		name = aNode.getNameAsString();
		
		return buildVarInfoWrapper( aNode, type, name, lastKnownValue );
	}
	
	/**
	 * Those are usually declarations of local variables
	 * @param aNode
	 * @return
	 */
	private VariableInfoWrapper buildVarInfoWrapperFromExpressionStmt( ExpressionStmt aNode ) {
		String name = defStrValue;
		String type = defStrValue;
		String lastKnownValue = defStrValue;

		Expression expr = aNode.getExpression();
		if( expr instanceof VariableDeclarationExpr ) {
			VariableDeclarationExpr varDecExp = (VariableDeclarationExpr) expr;
			
			VariableDeclarator vd = varDecExp.getVariable(0);
			if( vd != null ) {
				name = vd.getNameAsString();
				Type t = vd.getType();
				if( t != null) {
					type = t.toString().trim().toLowerCase(); // to bad there is no getName from types
				}
				
				// this only makes sense for objects that can easily be converted to strings and reconstructed
				// for everything else we need to access the node laster on
				if( hasPrimitiveValue( vd.getInitializer().isPresent() ? vd.getInitializer().get() : null ) ) {
					lastKnownValue = vd.getInitializer().get().toString();
				}
			}
			
		} else {
			// we are not interested in all the other expressions
			return null;
		}
		
		return buildVarInfoWrapper( aNode, type, name, lastKnownValue );
	}
	
	/**
	 * Has a look at the parent of the node to determine what kind of scope the variable
	 * belongs to.
	 * @param aNode
	 * @return The scope
	 */
	private VariableScope getScope( Node aNode ) {
		VariableScope result = VariableScope.UNKNOWN;
		
		Node parentNode = null;
		if( aNode.getParentNode().isPresent() ) {
			parentNode = aNode.getParentNode().get();
		} else {
			// if this node has no parent its scope is at least global
			return VariableScope.GLOBAL;
		}
		
		// if the parent is the compilation unit the variable is considered a global variable
		if( parentNode instanceof ClassOrInterfaceDeclaration ) {
			return VariableScope.GLOBAL;
		}
		
		// if the parent is a block declaration the variable is considered a local variable
		if( parentNode instanceof BlockStmt ) {
			return VariableScope.LOCAL;
		}
		
		// arguments are variables from the signature of a method declaration
		if( parentNode instanceof MethodDeclaration ) {
			return VariableScope.ARGUMENT;
		}
		
		return result;
	}
	
	/**
	 * Recursive approach because the optionals are weird
	 * @param aNode The node that may has one or multiple parents
	 * @param aList A list of all parents that were found
	 */
	private void addAllParentsToHistory(Node aNode, List<Optional<Node>> aList, List<VariableInfoWrapper> aSymbolTable ) {
		Optional<Node> parentOpt = aNode.getParentNode();
		if( parentOpt.isPresent() ) {	
			addAllParentsToHistory( parentOpt.get(), aList, aSymbolTable );
			aList.add( parentOpt );
			
			// add all children that are before the
			for( Node child : parentOpt.get().getChildNodes() ) {
				VariableInfoWrapper viw = checkAndBuildVariableInfoWrapper( child );
				if( viw != null ) {
					aSymbolTable.add( viw );
				}
				
				// we do not want to have children in the symbol table that come
				// below the actual node
				// this ignores global variables that come after the node of importance which is acceptable
				if ( child == aNode ) {
					// lets assume that a parent can be a variable declaration as well
					// if we do no want the parent to be part of the symbol table
					// move this break to the start
					break;
				}
			}
		} 
	}

	/**
	 * Checks if the given node is a statement that declares a variable or assignes a new value
	 * to an already existing variable. The latter still needs thinking
	 * @param aNode
	 * The node that should be checked
	 * @return
	 * An info object with all desired data or null if the node is not of interest
	 */
	private VariableInfoWrapper checkAndBuildVariableInfoWrapper( Node aNode ) {

		if( aNode instanceof FieldDeclaration ) {
			return buildVarInfoWrapperFromFieldDeclaration( (FieldDeclaration) aNode );
		}
		
		if( aNode instanceof Parameter ) {
			return buildVarInfoWrapperFromParameter( (Parameter) aNode );
		}
		
		if( aNode instanceof ExpressionStmt ) {
			return buildVarInfoWrapperFromExpressionStmt( (ExpressionStmt) aNode );
		}
		
		// TODO maybe add assignments to variables as well to find the last known value of a variable
		
		// if none of the checked types matched null is returned and the object will not be added to the
		// symbol table list
		return null;
	}
	
	private List<Class<? extends Node>> getClassHistory(Node aNode) {
		List<Class<? extends Node>> result = new ArrayList<Class<? extends Node>>();

		// TODO implement if this makes any sense

		return result;
	}
	
	/**
	 * @param aNode
	 * The node with an assignment of a value to a variable
	 * @return True if the node is an assignment of a primitve value which can be easily converted to
	 * a string and reconstructed. For complex objects use the node directly
	 */
	private boolean hasPrimitiveValue( Node aNode ) {
		
		if( aNode instanceof BooleanLiteralExpr ) {
			return true;
		}
		
		if( aNode instanceof CharLiteralExpr ) {
			return true;
		}
		
		if( aNode instanceof DoubleLiteralExpr ) {
			return true;
		}
		
		if( aNode instanceof IntegerLiteralExpr ) {
			return true;
		}
		
		if( aNode instanceof LongLiteralExpr ) {
			return true;
		}
		
		if( aNode instanceof NullLiteralExpr ) {
			return true;
		}
		
		if( aNode instanceof StringLiteralExpr ) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param aType
	 * The type of a variable declaration that was converted to a string beforehand
	 * @return True if the type of the declared variable is primitive
	 */
	private boolean hasPrimitiveType( String aType ) {
		
		// no breaks needed
		switch ( aType ) {
			case "int":;
			case "integer":;
			case "bool":;
			case "boolean":;
			case "double":;
			case "long":;
			case "char":;
			case "character":;
			case "string": return true;
			default : return false;
		}
		
	}

}
