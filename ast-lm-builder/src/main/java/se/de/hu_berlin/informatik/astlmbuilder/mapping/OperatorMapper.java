package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

public class OperatorMapper {

	// Assign Expressions
	public static final String AO_ASSIGN = "assign";
	public static final String AO_PLUS = "plus";
	public static final String AO_MINUS = "minus";
	public static final String AO_STAR = "star";
	public static final String AO_SLASH = "slash";
	public static final String AO_AND = "and";
	public static final String AO_OR = "or";
	public static final String AO_XOR = "xor";
	public static final String AO_REM = "rem";
	public static final String AO_LSHIFT = "lShift";
	public static final String AO_RSSHIFT = "rSignedShift";
	public static final String AO_RUSHIFT = "rUnsignedShift";

	// Binary expressions
	public static final String BO_OR = "or";
	public static final String BO_AND = "and";
	public static final String BO_BINOR = "binOr";
	public static final String BO_BINAND = "binAnd";
	public static final String BO_XOR = "xor";
	public static final String BO_EQUALS = "equals";
	public static final String BO_NOTEQUALS = "notEquals";
	public static final String BO_LESS = "less";
	public static final String BO_GREATER = "greater";
	public static final String BO_LESSEQUALS = "lessEquals";
	public static final String BO_GREATEREQUALS = "greaterEquals";
	public static final String BO_LSHIFT = "lShift";
	public static final String BO_RSIGNEDSHIFT = "rSignedShift";
	public static final String BO_RUNSIGNEDSHIFT = "rUnsignedShift";
	public static final String BO_PLUS = "plus";
	public static final String BO_MINUS = "minus";
	public static final String BO_TIMES = "times";
	public static final String BO_DIVIDE = "divide";
	public static final String BO_REMAINDER = "remainder";

	// Unary expressions
	public static final String UO_POSITIVE = "positive";
	public static final String UO_NEGATIVE = "negative";
	public static final String UO_PREINCREMENT = "preIncrement";
	public static final String UO_PREDECREMENT = "preDecrement";
	public static final String UO_NOT = "not";
	public static final String UO_INVERSE = "inverse";
	public static final String UO_POSINCREMENT = "posIncrement";
	public static final String UO_POSDECREMENT = "posDecrement";

	public static AssignExpr.Operator getAssignOperatorFromMapping(String aSerializedOperator) {
		if (aSerializedOperator == null || aSerializedOperator.length() == 0) {
			return null;
		}

		// TODO to make it more robust a check for brackets could be added here
		// in case somewhere some characters got lost
		// very low priority

		if (aSerializedOperator.equals(AO_ASSIGN)) {
			// =
			return AssignExpr.Operator.ASSIGN;
		}

		if (aSerializedOperator.equals(AO_PLUS)) {
			// +=
			return AssignExpr.Operator.PLUS;
		}

		if (aSerializedOperator.equals(AO_MINUS)) {
			// -=
			return AssignExpr.Operator.MINUS;
		}

		if (aSerializedOperator.equals(AO_STAR)) {
			// *=
			return AssignExpr.Operator.MULTIPLY;
		}

		if (aSerializedOperator.equals(AO_SLASH)) {
			// /=
			return AssignExpr.Operator.DIVIDE;
		}

		if (aSerializedOperator.equals(AO_AND)) {
			// &=
			return AssignExpr.Operator.AND;
		}

		if (aSerializedOperator.equals(AO_OR)) {
			// |=
			return AssignExpr.Operator.OR;
		}

		if (aSerializedOperator.equals(AO_XOR)) {
			// ^=
			return AssignExpr.Operator.XOR;
		}

		if (aSerializedOperator.equals(AO_REM)) {
			// %=
			return AssignExpr.Operator.REMAINDER;
		}

		if (aSerializedOperator.equals(AO_LSHIFT)) {
			// <<=
			return AssignExpr.Operator.LEFT_SHIFT;
		}

		if (aSerializedOperator.equals(AO_RSSHIFT)) {
			// >>=
			return AssignExpr.Operator.SIGNED_RIGHT_SHIFT;
		}

		if (aSerializedOperator.equals(AO_RUSHIFT)) {
			// >>>=
			return AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT;
		}

		return null;
	}
	
	/**
	 * Returns the keyword for the given binary operator
	 * @param aAO The binary operator that needs mapping
	 * @return the keyword for the creation of a token or null if the mapping failed
	 */
	public static String getAssignOperatorMapping(AssignExpr.Operator aAO) {

		if (aAO == null) {
			return null;
		}

		switch (aAO) {
		case ASSIGN:         return AO_ASSIGN;
		case PLUS:           return AO_PLUS;
		case MINUS:          return AO_MINUS;
		case MULTIPLY:           return AO_STAR;
		case DIVIDE:          return AO_SLASH;
		case AND:            return AO_AND;
		case OR:             return AO_OR;
		case XOR:            return AO_XOR;
		case REMAINDER:            return AO_REM;
		case LEFT_SHIFT:         return AO_LSHIFT;
		case SIGNED_RIGHT_SHIFT:   return AO_RSSHIFT;
		case UNSIGNED_RIGHT_SHIFT: return AO_RUSHIFT;
		default:             return null;
		}
		
	}

	public static BinaryExpr.Operator getBinaryOperatorFromMapping(String aSerializedOperator) {

		if (aSerializedOperator == null || aSerializedOperator.length() == 0) {
			return null;
		}

		// TODO to make it more robust a check for brackets could be added here
		// in case somewhere some characters got lost
		// very low priority

		if (aSerializedOperator.equals(BO_OR)) {
			// ||
			return BinaryExpr.Operator.OR;
		}

		if (aSerializedOperator.equals(BO_AND)) {
			// &&
			return BinaryExpr.Operator.AND;
		}

		if (aSerializedOperator.equals(BO_BINOR)) {
			// |
			return BinaryExpr.Operator.BINARY_OR;
		}

		if (aSerializedOperator.equals(BO_BINAND)) {
			// &
			return BinaryExpr.Operator.BINARY_AND;
		}

		if (aSerializedOperator.equals(BO_XOR)) {
			// ^
			return BinaryExpr.Operator.XOR;
		}

		if (aSerializedOperator.equals(BO_EQUALS)) {
			// ==
			return BinaryExpr.Operator.EQUALS;
		}

		if (aSerializedOperator.equals(BO_NOTEQUALS)) {
			// !=
			return BinaryExpr.Operator.NOT_EQUALS;
		}

		if (aSerializedOperator.equals(BO_LESS)) {
			// <
			return BinaryExpr.Operator.LESS;
		}

		if (aSerializedOperator.equals(BO_GREATER)) {
			// >
			return BinaryExpr.Operator.GREATER;
		}

		if (aSerializedOperator.equals(BO_LESSEQUALS)) {
			// <=
			return BinaryExpr.Operator.LESS_EQUALS;
		}

		if (aSerializedOperator.equals(BO_GREATEREQUALS)) {
			// >=
			return BinaryExpr.Operator.GREATER_EQUALS;
		}

		if (aSerializedOperator.equals(BO_LSHIFT)) {
			// <<
			return BinaryExpr.Operator.LEFT_SHIFT;
		}

		if (aSerializedOperator.equals(BO_RSIGNEDSHIFT)) {
			// >>
			return BinaryExpr.Operator.SIGNED_RIGHT_SHIFT;
		}

		if (aSerializedOperator.equals(BO_RUNSIGNEDSHIFT)) {
			// >>>
			return BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT;
		}

		if (aSerializedOperator.equals(BO_PLUS)) {
			// +
			return BinaryExpr.Operator.PLUS;
		}

		if (aSerializedOperator.equals(BO_MINUS)) {
			// -
			return BinaryExpr.Operator.MINUS;
		}

		if (aSerializedOperator.equals(BO_TIMES)) {
			// *
			return BinaryExpr.Operator.MULTIPLY;
		}

		if (aSerializedOperator.equals(BO_DIVIDE)) {
			// /
			return BinaryExpr.Operator.DIVIDE;
		}

		if (aSerializedOperator.equals(BO_REMAINDER)) {
			// %
			return BinaryExpr.Operator.REMAINDER;
		}

		return null;
	}
	
	/**
	 * Returns the keyword for the given binary operator
	 * @param aBO The binary operator that needs mapping
	 * @return the keyword for the creation of a token or null if the mapping failed
	 */
	public static String getBinaryOperatorMapping(BinaryExpr.Operator aBO) {

		if (aBO == null) {
			return null;
		}

		switch (aBO) {
		case OR:             return BO_OR;
		case AND:            return BO_AND;
		case BINARY_OR:          return BO_BINOR;
		case BINARY_AND:         return BO_BINAND;
		case XOR:            return BO_XOR;
		case EQUALS:         return BO_EQUALS;
		case NOT_EQUALS:      return BO_NOTEQUALS;
		case LESS:           return BO_LESS;
		case GREATER:        return BO_GREATER;
		case LESS_EQUALS:     return BO_LESSEQUALS;
		case GREATER_EQUALS:  return BO_GREATEREQUALS;
		case LEFT_SHIFT:         return BO_LSHIFT;
		case SIGNED_RIGHT_SHIFT:   return BO_RSIGNEDSHIFT;
		case UNSIGNED_RIGHT_SHIFT: return BO_RUNSIGNEDSHIFT;
		case PLUS:           return BO_PLUS;
		case MINUS:          return BO_MINUS;
		case MULTIPLY:          return BO_TIMES;
		case DIVIDE:         return BO_DIVIDE;
		case REMAINDER:      return BO_REMAINDER;
		default:             return null;
		}
	}

	public static UnaryExpr.Operator getUnaryOperatorFromMapping(String aSerializedOperator) {

		if (aSerializedOperator == null || aSerializedOperator.length() == 0) {
			return null;
		}

		// TODO to make it more robust a check for brackets could be added here
		// in case somewhere some characters got lost
		// very low priority

		if (aSerializedOperator.equals(UO_POSITIVE)) {
			// +
			return UnaryExpr.Operator.PLUS;
		}

		if (aSerializedOperator.equals(UO_NEGATIVE)) {
			// -
			return UnaryExpr.Operator.MINUS;
		}

		if (aSerializedOperator.equals(UO_PREINCREMENT)) {
			// ++
			return UnaryExpr.Operator.PREFIX_INCREMENT;
		}

		if (aSerializedOperator.equals(UO_PREDECREMENT)) {
			// --
			return UnaryExpr.Operator.PREFIX_DECREMENT;
		}

		if (aSerializedOperator.equals(UO_NOT)) {
			// !
			return UnaryExpr.Operator.LOGICAL_COMPLEMENT;
		}

		if (aSerializedOperator.equals(UO_INVERSE)) {
			// ~
			return UnaryExpr.Operator.BITWISE_COMPLEMENT;
		}

		if (aSerializedOperator.equals(UO_POSINCREMENT)) {
			// ++
			return UnaryExpr.Operator.POSTFIX_INCREMENT;
		}

		if (aSerializedOperator.equals(UO_POSDECREMENT)) {
			// --
			return UnaryExpr.Operator.POSTFIX_DECREMENT;
		}

		return null;
	}

	/**
	 * Returns the keyword for the given unary operator
	 * @param aUO The unary operator that needs mapping
	 * @return the keyword for the creation of a token or null if the mapping failed
	 */
	public static String getUnaryOperatorMapping(UnaryExpr.Operator aUO) {

		if (aUO == null) {
			return null;
		}

		switch (aUO) {
		case PLUS:     return UO_POSITIVE;
		case MINUS:     return UO_NEGATIVE;
		case PREFIX_INCREMENT: return UO_PREINCREMENT;
		case PREFIX_DECREMENT: return UO_PREDECREMENT;
		case LOGICAL_COMPLEMENT:          return UO_NOT;
		case BITWISE_COMPLEMENT:      return UO_INVERSE;
		case POSTFIX_INCREMENT: return UO_POSINCREMENT;
		case POSTFIX_DECREMENT: return UO_POSDECREMENT;
		default:           return null;
		}
	}

}
