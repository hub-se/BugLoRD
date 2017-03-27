package se.de.hu_berlin.informatik.astlmbuilder.parser;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

public interface OperatorParser {

	// Assign Expressions
	public static final String AO_ASSIGN = AssignExpr.Operator.ASSIGN.asString();
	public static final String AO_PLUS = AssignExpr.Operator.PLUS.asString();
	public static final String AO_MINUS = AssignExpr.Operator.MINUS.asString();
	public static final String AO_STAR = AssignExpr.Operator.MULTIPLY.asString();
	public static final String AO_SLASH = AssignExpr.Operator.DIVIDE.asString();
	public static final String AO_AND = AssignExpr.Operator.AND.asString();
	public static final String AO_OR = AssignExpr.Operator.OR.asString();
	public static final String AO_XOR = AssignExpr.Operator.XOR.asString();
	public static final String AO_REM = AssignExpr.Operator.REMAINDER.asString();
	public static final String AO_LSHIFT = AssignExpr.Operator.LEFT_SHIFT.asString();
	public static final String AO_RSSHIFT = AssignExpr.Operator.SIGNED_RIGHT_SHIFT.asString();
	public static final String AO_RUSHIFT = AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT.asString();

	// Binary expressions
	public static final String BO_OR = BinaryExpr.Operator.OR.asString();
	public static final String BO_AND = BinaryExpr.Operator.AND.asString();
	public static final String BO_BINOR = BinaryExpr.Operator.BINARY_OR.asString();
	public static final String BO_BINAND = BinaryExpr.Operator.BINARY_AND.asString();
	public static final String BO_XOR = BinaryExpr.Operator.XOR.asString();
	public static final String BO_EQUALS = BinaryExpr.Operator.EQUALS.asString();
	public static final String BO_NOTEQUALS = BinaryExpr.Operator.NOT_EQUALS.asString();
	public static final String BO_LESS = BinaryExpr.Operator.LESS.asString();
	public static final String BO_GREATER = BinaryExpr.Operator.GREATER.asString();
	public static final String BO_LESSEQUALS = BinaryExpr.Operator.LESS_EQUALS.asString();
	public static final String BO_GREATEREQUALS = BinaryExpr.Operator.GREATER_EQUALS.asString();
	public static final String BO_LSHIFT = BinaryExpr.Operator.LEFT_SHIFT.asString();
	public static final String BO_RSIGNEDSHIFT = BinaryExpr.Operator.SIGNED_RIGHT_SHIFT.asString();
	public static final String BO_RUNSIGNEDSHIFT = BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT.asString();
	public static final String BO_PLUS = BinaryExpr.Operator.PLUS.asString();
	public static final String BO_MINUS = BinaryExpr.Operator.MINUS.asString();
	public static final String BO_TIMES = BinaryExpr.Operator.MULTIPLY.asString();
	public static final String BO_DIVIDE = BinaryExpr.Operator.DIVIDE.asString();
	public static final String BO_REMAINDER = BinaryExpr.Operator.REMAINDER.asString();

	// Unary expressions
	public static final String UO_POSITIVE = UnaryExpr.Operator.PLUS.asString();
	public static final String UO_NEGATIVE = UnaryExpr.Operator.MINUS.asString();
	public static final String UO_PREINCREMENT = UnaryExpr.Operator.PREFIX_INCREMENT.asString();
	public static final String UO_PREDECREMENT = UnaryExpr.Operator.PREFIX_DECREMENT.asString();
	public static final String UO_NOT = UnaryExpr.Operator.LOGICAL_COMPLEMENT.asString();
	public static final String UO_INVERSE = UnaryExpr.Operator.BITWISE_COMPLEMENT.asString();
	public static final String UO_POSINCREMENT = UnaryExpr.Operator.POSTFIX_INCREMENT.asString();
	public static final String UO_POSDECREMENT = UnaryExpr.Operator.POSTFIX_DECREMENT.asString();

	public static AssignExpr.Operator getAssignOperatorFromMapping(String token) {
		if (token == null || token.length() == 0) {
			return null;
		}

		if (token.equals(AO_ASSIGN)) {
			// =
			return AssignExpr.Operator.ASSIGN;
		}

		if (token.equals(AO_PLUS)) {
			// +=
			return AssignExpr.Operator.PLUS;
		}

		if (token.equals(AO_MINUS)) {
			// -=
			return AssignExpr.Operator.MINUS;
		}

		if (token.equals(AO_STAR)) {
			// *=
			return AssignExpr.Operator.MULTIPLY;
		}

		if (token.equals(AO_SLASH)) {
			// /=
			return AssignExpr.Operator.DIVIDE;
		}

		if (token.equals(AO_AND)) {
			// &=
			return AssignExpr.Operator.AND;
		}

		if (token.equals(AO_OR)) {
			// |=
			return AssignExpr.Operator.OR;
		}

		if (token.equals(AO_XOR)) {
			// ^=
			return AssignExpr.Operator.XOR;
		}

		if (token.equals(AO_REM)) {
			// %=
			return AssignExpr.Operator.REMAINDER;
		}

		if (token.equals(AO_LSHIFT)) {
			// <<=
			return AssignExpr.Operator.LEFT_SHIFT;
		}

		if (token.equals(AO_RSSHIFT)) {
			// >>=
			return AssignExpr.Operator.SIGNED_RIGHT_SHIFT;
		}

		if (token.equals(AO_RUSHIFT)) {
			// >>>=
			return AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT;
		}

		return null;
	}
	
//	/**
//	 * Returns the keyword for the given binary operator
//	 * @param aAO The binary operator that needs mapping
//	 * @return the keyword for the creation of a token or null if the mapping failed
//	 */
//	public static String getAssignOperatorMapping(AssignExpr.Operator aAO) {
//
//		if (aAO == null) {
//			return null;
//		}
//
//		switch (aAO) {
//		case ASSIGN:         return AO_ASSIGN;
//		case PLUS:           return AO_PLUS;
//		case MINUS:          return AO_MINUS;
//		case MULTIPLY:           return AO_STAR;
//		case DIVIDE:          return AO_SLASH;
//		case AND:            return AO_AND;
//		case OR:             return AO_OR;
//		case XOR:            return AO_XOR;
//		case REMAINDER:            return AO_REM;
//		case LEFT_SHIFT:         return AO_LSHIFT;
//		case SIGNED_RIGHT_SHIFT:   return AO_RSSHIFT;
//		case UNSIGNED_RIGHT_SHIFT: return AO_RUSHIFT;
//		default:             return null;
//		}
//		
//	}

	public static BinaryExpr.Operator getBinaryOperatorFromMapping(String token) {

		if (token == null || token.length() == 0) {
			return null;
		}

		if (token.equals(BO_OR)) {
			// ||
			return BinaryExpr.Operator.OR;
		}

		if (token.equals(BO_AND)) {
			// &&
			return BinaryExpr.Operator.AND;
		}

		if (token.equals(BO_BINOR)) {
			// |
			return BinaryExpr.Operator.BINARY_OR;
		}

		if (token.equals(BO_BINAND)) {
			// &
			return BinaryExpr.Operator.BINARY_AND;
		}

		if (token.equals(BO_XOR)) {
			// ^
			return BinaryExpr.Operator.XOR;
		}

		if (token.equals(BO_EQUALS)) {
			// ==
			return BinaryExpr.Operator.EQUALS;
		}

		if (token.equals(BO_NOTEQUALS)) {
			// !=
			return BinaryExpr.Operator.NOT_EQUALS;
		}

		if (token.equals(BO_LESS)) {
			// <
			return BinaryExpr.Operator.LESS;
		}

		if (token.equals(BO_GREATER)) {
			// >
			return BinaryExpr.Operator.GREATER;
		}

		if (token.equals(BO_LESSEQUALS)) {
			// <=
			return BinaryExpr.Operator.LESS_EQUALS;
		}

		if (token.equals(BO_GREATEREQUALS)) {
			// >=
			return BinaryExpr.Operator.GREATER_EQUALS;
		}

		if (token.equals(BO_LSHIFT)) {
			// <<
			return BinaryExpr.Operator.LEFT_SHIFT;
		}

		if (token.equals(BO_RSIGNEDSHIFT)) {
			// >>
			return BinaryExpr.Operator.SIGNED_RIGHT_SHIFT;
		}

		if (token.equals(BO_RUNSIGNEDSHIFT)) {
			// >>>
			return BinaryExpr.Operator.UNSIGNED_RIGHT_SHIFT;
		}

		if (token.equals(BO_PLUS)) {
			// +
			return BinaryExpr.Operator.PLUS;
		}

		if (token.equals(BO_MINUS)) {
			// -
			return BinaryExpr.Operator.MINUS;
		}

		if (token.equals(BO_TIMES)) {
			// *
			return BinaryExpr.Operator.MULTIPLY;
		}

		if (token.equals(BO_DIVIDE)) {
			// /
			return BinaryExpr.Operator.DIVIDE;
		}

		if (token.equals(BO_REMAINDER)) {
			// %
			return BinaryExpr.Operator.REMAINDER;
		}

		return null;
	}
	
//	/**
//	 * Returns the keyword for the given binary operator
//	 * @param aBO The binary operator that needs mapping
//	 * @return the keyword for the creation of a token or null if the mapping failed
//	 */
//	public static String getBinaryOperatorMapping(BinaryExpr.Operator aBO) {
//
//		if (aBO == null) {
//			return null;
//		}
//
//		switch (aBO) {
//		case OR:             return BO_OR;
//		case AND:            return BO_AND;
//		case BINARY_OR:          return BO_BINOR;
//		case BINARY_AND:         return BO_BINAND;
//		case XOR:            return BO_XOR;
//		case EQUALS:         return BO_EQUALS;
//		case NOT_EQUALS:      return BO_NOTEQUALS;
//		case LESS:           return BO_LESS;
//		case GREATER:        return BO_GREATER;
//		case LESS_EQUALS:     return BO_LESSEQUALS;
//		case GREATER_EQUALS:  return BO_GREATEREQUALS;
//		case LEFT_SHIFT:         return BO_LSHIFT;
//		case SIGNED_RIGHT_SHIFT:   return BO_RSIGNEDSHIFT;
//		case UNSIGNED_RIGHT_SHIFT: return BO_RUNSIGNEDSHIFT;
//		case PLUS:           return BO_PLUS;
//		case MINUS:          return BO_MINUS;
//		case MULTIPLY:          return BO_TIMES;
//		case DIVIDE:         return BO_DIVIDE;
//		case REMAINDER:      return BO_REMAINDER;
//		default:             return null;
//		}
//	}

	public static UnaryExpr.Operator getUnaryOperatorFromMapping(String token) {

		if (token == null || token.length() == 0) {
			return null;
		}

		if (token.equals(UO_POSITIVE)) {
			// +
			return UnaryExpr.Operator.PLUS;
		}

		if (token.equals(UO_NEGATIVE)) {
			// -
			return UnaryExpr.Operator.MINUS;
		}

		if (token.equals(UO_PREINCREMENT)) {
			// ++
			return UnaryExpr.Operator.PREFIX_INCREMENT;
		}

		if (token.equals(UO_PREDECREMENT)) {
			// --
			return UnaryExpr.Operator.PREFIX_DECREMENT;
		}

		if (token.equals(UO_NOT)) {
			// !
			return UnaryExpr.Operator.LOGICAL_COMPLEMENT;
		}

		if (token.equals(UO_INVERSE)) {
			// ~
			return UnaryExpr.Operator.BITWISE_COMPLEMENT;
		}

		if (token.equals(UO_POSINCREMENT)) {
			// ++
			return UnaryExpr.Operator.POSTFIX_INCREMENT;
		}

		if (token.equals(UO_POSDECREMENT)) {
			// --
			return UnaryExpr.Operator.POSTFIX_DECREMENT;
		}

		return null;
	}

//	/**
//	 * Returns the keyword for the given unary operator
//	 * @param aUO The unary operator that needs mapping
//	 * @return the keyword for the creation of a token or null if the mapping failed
//	 */
//	public static String getUnaryOperatorMapping(UnaryExpr.Operator aUO) {
//
//		if (aUO == null) {
//			return null;
//		}
//
//		switch (aUO) {
//		case PLUS:     return UO_POSITIVE;
//		case MINUS:     return UO_NEGATIVE;
//		case PREFIX_INCREMENT: return UO_PREINCREMENT;
//		case PREFIX_DECREMENT: return UO_PREDECREMENT;
//		case LOGICAL_COMPLEMENT:          return UO_NOT;
//		case BITWISE_COMPLEMENT:      return UO_INVERSE;
//		case POSTFIX_INCREMENT: return UO_POSINCREMENT;
//		case POSTFIX_DECREMENT: return UO_POSDECREMENT;
//		default:           return null;
//		}
//	}

}
