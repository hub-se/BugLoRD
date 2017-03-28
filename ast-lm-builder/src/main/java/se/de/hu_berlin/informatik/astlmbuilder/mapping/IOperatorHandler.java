package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

public interface IOperatorHandler {

	public default AssignExpr.Operator parseAssignOperatorFromToken(String token) {
		return AssignExpr.Operator.values()[Integer.valueOf(token)];
	}
	
	public default String getMappingForAssignOperator(AssignExpr.Operator operator) {
		return String.valueOf(operator.ordinal());
	}

	public default BinaryExpr.Operator parseBinaryOperatorFromToken(String token) {
		return BinaryExpr.Operator.values()[Integer.valueOf(token)];
	}

	public default String getMappingForBinaryOperator(BinaryExpr.Operator operator) {
		return String.valueOf(operator.ordinal());
	}

	public default UnaryExpr.Operator parseUnaryOperatorFromToken(String token) {
		return UnaryExpr.Operator.values()[Integer.valueOf(token)];
	}

	public default String getMappingForUnaryOperator(UnaryExpr.Operator operator) {
		return String.valueOf(operator.ordinal());
	}
	
}
