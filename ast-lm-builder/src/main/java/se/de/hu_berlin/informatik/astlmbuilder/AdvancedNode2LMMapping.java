package se.de.hu_berlin.informatik.astlmbuilder;

import java.util.List;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;

public class AdvancedNode2LMMapping extends SimpleNode2LMMapping {
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * This variant handles method and constructor declarations differently and inspects
	 * method calls more closely.
	 * @param aNode The node that should be mapped
	 * @return the string representation enclosed in a wrapper
	 */
	public MappingWrapper<String> getMappingForNode( Node aNode ) {
		
		// all declarations
		if ( aNode instanceof ConstructorDeclaration ){
			return buildCDec( (ConstructorDeclaration) aNode );
		} else if ( aNode instanceof MethodDeclaration ){
			return buildMDec( (MethodDeclaration) aNode );
		} 
		
		// all expressions
		else if ( aNode instanceof MethodCallExpr ){
			return buildMCall( (MethodCallExpr) aNode );
		} 
		
		return super.getMappingForNode(aNode);
	}
	
	/**
	 * Builds a token for the method call including some information about the
	 * method name, number of arguments and their types
	 * @param aMCall
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildMCall( MethodCallExpr aMCall ) {
		String result1 = METHOD_CALL_EXPRESSION;
		String result2 = "(";
		
//		String name = null;
//		try {
//			name = Class.forName(aMCall.getName()).getCanonicalName();
//		} catch (ClassNotFoundException e) {
//			name = aMCall.getName();
//		}
		// first argument is the name
		result2 += aMCall.getName();
		
		if( aMCall.getArgs() != null ) {
			// add some information regarding the arguments
			List<Expression> args = aMCall.getArgs();
			// first the number of parameters
			result2 += "," + args.size();
			// afterwards the simple type of them
			for( Expression singleArg : args ) {
//				result += "," + mapObjectTypes( singlePar.getType() );
				result2 += "," + getMappingForNode(singleArg);
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}
	
	/**
	 * Builds a token for the method declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aMDec
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildMDec( MethodDeclaration aMDec ) {
		String result1 = METHOD_DECLARATION;
		String result2 = "(";
		
		// first argument is always the return type
		result2 += aMDec.getType();
		
		if( aMDec.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aMDec.getParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
//				result += "," + mapObjectTypes( singlePar.getType() );
				result2 += "," + singlePar.getType();
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}
	
	/**
	 * Builds a token for the constructor declaration including some information about the
	 * return value, number of parameters and modifiers
	 * @param aCDec
	 * @return an advanced token string
	 */
	private MappingWrapper<String> buildCDec( ConstructorDeclaration aCDec ) {
		String result1 = CONSTRUCTOR_DECLARATION;
		String result2 = "(";
		
		if( aCDec.getParameters() != null ) {
			// add some information regarding the parameters
			List<Parameter> pars = aCDec.getParameters();
			// first the number of parameters
			result2 += "," + pars.size();
			// afterwards the simple type of them
			for( Parameter singlePar : pars ) {
//				result += "," + mapObjectTypes( singlePar.getType() );
				result2 += "," + singlePar.getType();
			}
		}
		
		result2 += ")";
		
		return new MappingWrapper<>(result1, result2);
	}

	
//	/**
//	 * Maps the types of objects to a standard format
//	 * @param aType
//	 * @return
//	 */
//	private String mapObjectTypes( Type aType ) {
//		// TODO remove 
//		
//		if( aType instanceof PrimitiveType ) {
//			// those are already ready to use
//			return aType.toString();
//		} else if( aType instanceof ReferenceType ){
//			return TYPE_REFERENCE;
//		} else if( aType instanceof IntersectionType ){
//			return TYPE_INTERSECTION;
//		} else if( aType instanceof VoidType ){
//			return TYPE_VOID;
//		} else if( aType instanceof WildcardType ){
//			return TYPE_WILDCARD;
//		} else if( aType instanceof UnknownType ){
//			return "UKN_T";
//		}
//		
//		// well whatever
//		return aType.toString();
//	}
	
}
