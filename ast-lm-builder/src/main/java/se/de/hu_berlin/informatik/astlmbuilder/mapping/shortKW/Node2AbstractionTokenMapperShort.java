package se.de.hu_berlin.informatik.astlmbuilder.mapping.shortKW;

import java.util.Collection;

import com.github.javaparser.ast.expr.MethodCallExpr;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.IAbsTokenMapper;

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> {@code ($NODE_IDENTIFIER) ($NODE_IDENTIFIER;[list,with,information],information) ($NODE_IDENTIFIER;more,information) ...}
 * 
 * @author Simon
 */
public class Node2AbstractionTokenMapperShort extends KeyWordConstantsShort implements IAbsTokenMapper {

	// a collection of blacklisted private method names
	// the simple mapper makes no use of this
	public Collection<String> privMethodBL = null;
	
	@Override
	public void setPrivMethodBlackList(Collection<String> aBL) {
		privMethodBL = aBL;
	}

	@Override
	public void clearPrivMethodBlackList() {
		privMethodBL = null;
	}

	// this is redefined because of the usage of the method name list
	@Override
	public String getMappingForMethodCallExpr(MethodCallExpr aNode, int aAbsDepth) {

		if (aAbsDepth == 0) { // maximum abstraction
			return combineData2String(getMethodCallExpression());
		} else { // still at a higher level of abstraction (either negative or
					// greater than 0)
			--aAbsDepth;
		}
		String method = "";
		if (privMethodBL.contains(aNode.getName())) {
			method += getPrivateMethodCallExpression();
		} else {
			if (aNode.getScope() != null) {
				method += getMappingForExpression(aNode.getScope(), aAbsDepth);
			}
		}

		String name = aNode.getName();
		String exprList = getMappingForExpressionList(aNode.getArgs(), aAbsDepth);
		String typeList = getMappingForTypeList(aNode.getTypeArgs(), aAbsDepth);

		return combineData2String(getMethodCallExpression(), method, name, exprList, typeList);
	}
	
}
