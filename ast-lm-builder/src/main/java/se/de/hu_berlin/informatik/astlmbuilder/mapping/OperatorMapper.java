package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

public class OperatorMapper {

	public static AssignExpr.Operator getAssignOperatorFromMapping( String aSerializedOperator ) {
		if( aSerializedOperator == null || aSerializedOperator.length() == 0 ) {
			return null;
		}		
		
		// TODO to make it more robust a check for brackets could be added here in case somewhere some characters got lost
		// very low priority
		
		if( aSerializedOperator.equals( "assign" ) ) {
			// =
			return AssignExpr.Operator.assign;
		}
		
		if( aSerializedOperator.equals( "plus" ) ) {
			// +=
			return AssignExpr.Operator.plus;
		}
		
		if( aSerializedOperator.equals( "minus" ) ) {
			// -=
			return AssignExpr.Operator.minus;
		}
		
		if( aSerializedOperator.equals( "star" ) ) {
			// *=
			return AssignExpr.Operator.star;
		}
		
		if( aSerializedOperator.equals( "slash" ) ) {
			// /=
			return AssignExpr.Operator.slash;
		}
		
		if( aSerializedOperator.equals( "and" ) ) {
			// &=
			return AssignExpr.Operator.and;
		}
		
		if( aSerializedOperator.equals( "or" ) ) {
			// |=
			return AssignExpr.Operator.or;
		}
		
		if( aSerializedOperator.equals( "xor" ) ) {
			// ^=
			return AssignExpr.Operator.xor;
		}
		
		if( aSerializedOperator.equals( "rem" ) ) {
			// %=
			return AssignExpr.Operator.rem;
		}
		
		if( aSerializedOperator.equals( "lShift" ) ) {
			// <<=
			return AssignExpr.Operator.lShift;
		}
		
		if( aSerializedOperator.equals( "rSignedShift" ) ) {
			// >>=
			return AssignExpr.Operator.rSignedShift;
		}
		
		if( aSerializedOperator.equals( "rUnsignedShift" ) ) {
			// >>>=
			return AssignExpr.Operator.rUnsignedShift;
		}
		
		return null;
	}
	
	public static BinaryExpr.Operator getBinaryOperatorFromMapping( String aSerializedOperator ) {

		if( aSerializedOperator == null || aSerializedOperator.length() == 0 ) {
			return null;
		}		
		
		// TODO to make it more robust a check for brackets could be added here in case somewhere some characters got lost
		// very low priority
		
		if( aSerializedOperator.equals( "or" ) ) {
			// ||
			return BinaryExpr.Operator.or;
		}
		
		if( aSerializedOperator.equals( "and" ) ) {
			// &&
			return BinaryExpr.Operator.and;
		}
		
		if( aSerializedOperator.equals( "binOr" ) ) {
			// |
			return BinaryExpr.Operator.binOr;
		}
		
		if( aSerializedOperator.equals( "binAnd" ) ) {
			// &
			return BinaryExpr.Operator.binAnd;
		}
		
		if( aSerializedOperator.equals( "xor" ) ) {
			// ^
			return BinaryExpr.Operator.xor;
		}
		
		if( aSerializedOperator.equals( "equals" ) ) {
			// ==
			return BinaryExpr.Operator.equals;
		}
		
		if( aSerializedOperator.equals( "notEquals" ) ) {
			// !=
			return BinaryExpr.Operator.notEquals;
		}
		
		if( aSerializedOperator.equals( "less" ) ) {
			// <
			return BinaryExpr.Operator.less;
		}
		
		if( aSerializedOperator.equals( "greater" ) ) {
			// >
			return BinaryExpr.Operator.greater;
		}
		
		if( aSerializedOperator.equals( "lessEquals" ) ) {
			// <=
			return BinaryExpr.Operator.lessEquals;
		}
		
		if( aSerializedOperator.equals( "greaterEquals" ) ) {
			// >=
			return BinaryExpr.Operator.greaterEquals;
		}
		
		if( aSerializedOperator.equals( "lShift" ) ) {
			// <<
			return BinaryExpr.Operator.lShift;
		}
		
		if( aSerializedOperator.equals( "rSignedShift" ) ) {
			// >>
			return BinaryExpr.Operator.rSignedShift;
		}
		
		if( aSerializedOperator.equals( "rUnsignedShift" ) ) {
			// >>>
			return BinaryExpr.Operator.rUnsignedShift;
		}
		
		if( aSerializedOperator.equals( "plus" ) ) {
			// +
			return BinaryExpr.Operator.plus;
		}
		
		if( aSerializedOperator.equals( "minus" ) ) {
			// -
			return BinaryExpr.Operator.minus;
		}
		
		if( aSerializedOperator.equals( "times" ) ) {
			// *
			return BinaryExpr.Operator.times;
		}
		
		if( aSerializedOperator.equals( "divide" ) ) {
			// /
			return BinaryExpr.Operator.divide;
		}
		
		if( aSerializedOperator.equals( "remainder" ) ) {
			// %
			return BinaryExpr.Operator.remainder;
		}
		
		return null;
	}
	
	public static UnaryExpr.Operator getUnaryOperatorFromMapping( String aSerializedOperator ) {

		if( aSerializedOperator == null || aSerializedOperator.length() == 0 ) {
			return null;
		}		
		
		// TODO to make it more robust a check for brackets could be added here in case somewhere some characters got lost
		// very low priority
		
		if( aSerializedOperator.equals( "positive" ) ) {
			// ||
			return UnaryExpr.Operator.positive;
		}
		
		if( aSerializedOperator.equals( "negative" ) ) {
			// &&
			return UnaryExpr.Operator.negative;
		}
		
		if( aSerializedOperator.equals( "preIncrement" ) ) {
			// |
			return UnaryExpr.Operator.preIncrement;
		}
		
		if( aSerializedOperator.equals( "preDecrement" ) ) {
			// &
			return UnaryExpr.Operator.preDecrement;
		}
		
		if( aSerializedOperator.equals( "not" ) ) {
			// ^
			return UnaryExpr.Operator.not;
		}
		
		if( aSerializedOperator.equals( "inverse" ) ) {
			// ==
			return UnaryExpr.Operator.inverse;
		}
		
		if( aSerializedOperator.equals( "posIncrement" ) ) {
			// !=
			return UnaryExpr.Operator.posIncrement;
		}
		
		if( aSerializedOperator.equals( "posDecrement" ) ) {
			// <
			return UnaryExpr.Operator.posDecrement;
		}
		
		return null;
	}
	
}
