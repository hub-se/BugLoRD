package se.de.hu_berlin.informatik.astlmbuilder.mapping.hrkw;

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
			return AssignExpr.Operator.assign;
		}

		if (aSerializedOperator.equals(AO_PLUS)) {
			// +=
			return AssignExpr.Operator.plus;
		}

		if (aSerializedOperator.equals(AO_MINUS)) {
			// -=
			return AssignExpr.Operator.minus;
		}

		if (aSerializedOperator.equals(AO_STAR)) {
			// *=
			return AssignExpr.Operator.star;
		}

		if (aSerializedOperator.equals(AO_SLASH)) {
			// /=
			return AssignExpr.Operator.slash;
		}

		if (aSerializedOperator.equals(AO_AND)) {
			// &=
			return AssignExpr.Operator.and;
		}

		if (aSerializedOperator.equals(AO_OR)) {
			// |=
			return AssignExpr.Operator.or;
		}

		if (aSerializedOperator.equals(AO_XOR)) {
			// ^=
			return AssignExpr.Operator.xor;
		}

		if (aSerializedOperator.equals(AO_REM)) {
			// %=
			return AssignExpr.Operator.rem;
		}

		if (aSerializedOperator.equals(AO_LSHIFT)) {
			// <<=
			return AssignExpr.Operator.lShift;
		}

		if (aSerializedOperator.equals(AO_RSSHIFT)) {
			// >>=
			return AssignExpr.Operator.rSignedShift;
		}

		if (aSerializedOperator.equals(AO_RUSHIFT)) {
			// >>>=
			return AssignExpr.Operator.rUnsignedShift;
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
		case assign:         return AO_ASSIGN;
		case plus:           return AO_PLUS;
		case minus:          return AO_MINUS;
		case star:           return AO_STAR;
		case slash:          return AO_SLASH;
		case and:            return AO_AND;
		case or:             return AO_OR;
		case xor:            return AO_XOR;
		case rem:            return AO_REM;
		case lShift:         return AO_LSHIFT;
		case rSignedShift:   return AO_RSSHIFT;
		case rUnsignedShift: return AO_RUSHIFT;
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
			return BinaryExpr.Operator.or;
		}

		if (aSerializedOperator.equals(BO_AND)) {
			// &&
			return BinaryExpr.Operator.and;
		}

		if (aSerializedOperator.equals(BO_BINOR)) {
			// |
			return BinaryExpr.Operator.binOr;
		}

		if (aSerializedOperator.equals(BO_BINAND)) {
			// &
			return BinaryExpr.Operator.binAnd;
		}

		if (aSerializedOperator.equals(BO_XOR)) {
			// ^
			return BinaryExpr.Operator.xor;
		}

		if (aSerializedOperator.equals(BO_EQUALS)) {
			// ==
			return BinaryExpr.Operator.equals;
		}

		if (aSerializedOperator.equals(BO_NOTEQUALS)) {
			// !=
			return BinaryExpr.Operator.notEquals;
		}

		if (aSerializedOperator.equals(BO_LESS)) {
			// <
			return BinaryExpr.Operator.less;
		}

		if (aSerializedOperator.equals(BO_GREATER)) {
			// >
			return BinaryExpr.Operator.greater;
		}

		if (aSerializedOperator.equals(BO_LESSEQUALS)) {
			// <=
			return BinaryExpr.Operator.lessEquals;
		}

		if (aSerializedOperator.equals(BO_GREATEREQUALS)) {
			// >=
			return BinaryExpr.Operator.greaterEquals;
		}

		if (aSerializedOperator.equals(BO_LSHIFT)) {
			// <<
			return BinaryExpr.Operator.lShift;
		}

		if (aSerializedOperator.equals(BO_RSIGNEDSHIFT)) {
			// >>
			return BinaryExpr.Operator.rSignedShift;
		}

		if (aSerializedOperator.equals(BO_RUNSIGNEDSHIFT)) {
			// >>>
			return BinaryExpr.Operator.rUnsignedShift;
		}

		if (aSerializedOperator.equals(BO_PLUS)) {
			// +
			return BinaryExpr.Operator.plus;
		}

		if (aSerializedOperator.equals(BO_MINUS)) {
			// -
			return BinaryExpr.Operator.minus;
		}

		if (aSerializedOperator.equals(BO_TIMES)) {
			// *
			return BinaryExpr.Operator.times;
		}

		if (aSerializedOperator.equals(BO_DIVIDE)) {
			// /
			return BinaryExpr.Operator.divide;
		}

		if (aSerializedOperator.equals(BO_REMAINDER)) {
			// %
			return BinaryExpr.Operator.remainder;
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
		case or:             return BO_OR;
		case and:            return BO_AND;
		case binOr:          return BO_BINOR;
		case binAnd:         return BO_BINAND;
		case xor:            return BO_XOR;
		case equals:         return BO_EQUALS;
		case notEquals:      return BO_NOTEQUALS;
		case less:           return BO_LESS;
		case greater:        return BO_GREATER;
		case lessEquals:     return BO_LESSEQUALS;
		case greaterEquals:  return BO_GREATEREQUALS;
		case lShift:         return BO_LSHIFT;
		case rSignedShift:   return BO_RSIGNEDSHIFT;
		case rUnsignedShift: return BO_RUNSIGNEDSHIFT;
		case plus:           return BO_PLUS;
		case minus:          return BO_MINUS;
		case times:          return BO_TIMES;
		case divide:         return BO_DIVIDE;
		case remainder:      return BO_REMAINDER;
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
			// ||
			return UnaryExpr.Operator.positive;
		}

		if (aSerializedOperator.equals(UO_NEGATIVE)) {
			// &&
			return UnaryExpr.Operator.negative;
		}

		if (aSerializedOperator.equals(UO_PREINCREMENT)) {
			// |
			return UnaryExpr.Operator.preIncrement;
		}

		if (aSerializedOperator.equals(UO_PREDECREMENT)) {
			// &
			return UnaryExpr.Operator.preDecrement;
		}

		if (aSerializedOperator.equals(UO_NOT)) {
			// ^
			return UnaryExpr.Operator.not;
		}

		if (aSerializedOperator.equals(UO_INVERSE)) {
			// ==
			return UnaryExpr.Operator.inverse;
		}

		if (aSerializedOperator.equals(UO_POSINCREMENT)) {
			// !=
			return UnaryExpr.Operator.posIncrement;
		}

		if (aSerializedOperator.equals(UO_POSDECREMENT)) {
			// <
			return UnaryExpr.Operator.posDecrement;
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
		case positive:     return UO_POSITIVE;
		case negative:     return UO_NEGATIVE;
		case preIncrement: return UO_PREINCREMENT;
		case preDecrement: return UO_PREDECREMENT;
		case not:          return UO_NOT;
		case inverse:      return UO_INVERSE;
		case posIncrement: return UO_POSINCREMENT;
		case posDecrement: return UO_POSDECREMENT;
		default:           return null;
		}
	}

}
