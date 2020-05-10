package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.Type;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.VariableInfoWrapper.VariableScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to build the info wrapper objects that store something similar to a
 * symbol table for nodes
 */
public class InfoWrapperBuilder {

    // just for convenience
    private static final String defStrValue = VariableInfoWrapper.UNKNOWN_STR_VALUE;

    /**
     * Collects all relevant data like variable names, scopes and types from a
     * node and its parents and stores them into an information wrapper object.
     *
     * @param aNode The node of interest
     * @return A information wrapper containing data
     */
    public static InformationWrapper buildInfoWrapperForNode(Node aNode) {
        List<Class<? extends Node>> classHistory = getClassHistory(aNode);
        List<VariableInfoWrapper> symbolTableTmp = new ArrayList<>();
        List<Optional<Node>> nodeHistory = getNodeHistory(aNode, symbolTableTmp);
        SymbolTable realSymbolTable = new SymbolTable(symbolTableTmp);

        return new InformationWrapper(nodeHistory, classHistory, realSymbolTable);
    }

    private static List<Optional<Node>> getNodeHistory(Node aNode, List<VariableInfoWrapper> aSymbolTable) {

        if (aNode == null) {
            return null;
        }

        List<Optional<Node>> result = new ArrayList<>();
        addAllParentsToHistory(aNode, result, aSymbolTable);

        return result;
    }

    /**
     * Given the type of a node the rest of the info wrapper can be build
     *
     * @param aNode           The node with a variable declaration of some type
     * @param aType           The type of the variable that is declared or passed as argument
     * @param aLastKnownValue The first value is also the last known when dealing with declarations
     * @return A variable information wrapper object
     */
    private static VariableInfoWrapper buildVarInfoWrapper(Node aNode, String aType, String aName, String aLastKnownValue) {

        boolean primitive = hasPrimitiveType(aType);
        VariableScope scope = getScope(aNode);

        return new VariableInfoWrapper(aType, aName, aLastKnownValue, primitive, scope, aNode);
    }

    /**
     * Those can be anywhere in the code. For example in the initialization of a loop
     *
     * @param aVD A declaration of a variable
     * @return An info object for this variable declaration
     */
    private static VariableInfoWrapper buildVarInfoWrapperFromVarDec(VariableDeclarator aVD) {
        String name = defStrValue;
        String type = defStrValue;
        String lastKnownValue = defStrValue;

        if (aVD != null) {
            Type t = aVD.getType();
            name = aVD.getNameAsString();
            if (t != null) {
                type = t.toString().trim().toLowerCase(); // to bad there is no getName from types
            }

            // this only makes sense for objects that can easily be converted to strings and reconstructed
            // for everything else we need to access the node laster on
            if (hasPrimitiveValue(aVD.getInitializer().isPresent() ? aVD.getInitializer().get() : null)) {
                lastKnownValue = aVD.getInitializer().get().toString();
            }
        }

        return buildVarInfoWrapper(aVD, type, name, lastKnownValue);
    }

    /**
     * Those are usually declarations of global variables
     *
     * @param aNode bla
     * @return An info object for this variable declaration
     */
    private static VariableInfoWrapper buildVarInfoWrapperFromFieldDeclaration(FieldDeclaration aNode) {
        VariableDeclarator vd = aNode.getVariable(0);
        return buildVarInfoWrapperFromVarDec(vd);
    }

    /**
     * Those are usually declarations of parameters
     *
     * @param aNode A parameter of a method
     * @return An info object for this variable declaration
     */
    private static VariableInfoWrapper buildVarInfoWrapperFromParameter(Parameter aNode) {
        String name = defStrValue;
        String type = defStrValue;
        String lastKnownValue = defStrValue;

        type = aNode.getType().toString().trim().toLowerCase();
        name = aNode.getNameAsString();

        return buildVarInfoWrapper(aNode, type, name, lastKnownValue);
    }

    /**
     * Those are usually declarations of local variables
     *
     * @param aNode A declaration of one or multiple local variables
     */
    private static void buildVarInfoWrapperFromExpressionStmt(ExpressionStmt aNode, List<VariableInfoWrapper> aSymbolTable) {
        Expression expr = aNode.getExpression();
        if (expr instanceof VariableDeclarationExpr) {
            VariableDeclarationExpr vde = (VariableDeclarationExpr) expr;
            for (VariableDeclarator vd : vde.getVariables()) {
                aSymbolTable.add(buildVarInfoWrapperFromVarDec(vd));
            }
        }
    }

    /**
     * Has a look at the parent of the node to determine what kind of scope the variable
     * belongs to.
     *
     * @param aNode bla
     * @return The scope
     */
    private static VariableScope getScope(Node aNode) {
        VariableScope result = VariableScope.UNKNOWN;

        Node parentNode = findFirstMeaningfulParent(aNode);
        if (parentNode == null) {
            // if this node has no parent its scope is at least global
            return VariableScope.GLOBAL;
        }

        // if the parent is the compilation unit the variable is considered a global variable
        if (parentNode instanceof ClassOrInterfaceDeclaration) {
            return VariableScope.GLOBAL;
        }

        // if the parent is a block declaration the variable is considered a local variable
        // adding loop statements to this check is better than ignoring them when searching for
        // the first meaningful parent
        if (parentNode instanceof BlockStmt ||
                parentNode instanceof ForStmt ||
                parentNode instanceof ForeachStmt ||
                parentNode instanceof WhileStmt) {
            return VariableScope.LOCAL;
        }

        // parameters are variables from the signature of a method declaration
        if (parentNode instanceof MethodDeclaration) {
            return VariableScope.PARAMETER;
        }

        return result;
    }

    /**
     * Because of nesting of expressions and declarations it is not always easy to get
     * the right parent node to determine the scope of a variable.
     *
     * @param aChildNode The node that needs a scope
     * @return The first parent that is not some kind of wrapper
     */
    private static Node findFirstMeaningfulParent(Node aChildNode) {
        if (aChildNode == null) {
            return null;
        }

        if (!aChildNode.getParentNode().isPresent()) {
            // no parent means the child is the best choice
            return aChildNode;
        }

        Node parent = aChildNode.getParentNode().get();

        // those are three wrapper that are not of interest and should be skipped
        if (parent instanceof VariableDeclarationExpr ||
                parent instanceof ExpressionStmt ||
                parent instanceof FieldDeclaration) {
            return findFirstMeaningfulParent(parent);
        }

        return parent;
    }

    /**
     * Recursive approach because the optionals are weird
     *
     * @param aNode The node that may has one or multiple parents
     * @param aList A list of all parents that were found
     */
    private static void addAllParentsToHistory(Node aNode, List<Optional<Node>> aList, List<VariableInfoWrapper> aSymbolTable) {
        Optional<Node> parentOpt = aNode.getParentNode();
        if (parentOpt.isPresent()) {
            addAllParentsToHistory(parentOpt.get(), aList, aSymbolTable);
            aList.add(parentOpt);

            // check if the parent is something special like a for statement
            checkSpecialCasesForVarDecs(parentOpt.get(), aSymbolTable);

            // add all children that are before the
            for (Node child : parentOpt.get().getChildNodes()) {
                checkAndBuildVariableInfoWrapper(child, aSymbolTable);

                // we do not want to have children in the symbol table that come
                // below the actual node
                // this does not ignores global variables that come after the node of importance
                if (child == aNode && !(parentOpt.get() instanceof ClassOrInterfaceDeclaration)) {
                    // lets assume that a parent can be a variable declaration as well
                    // if we do no want the parent to be part of the symbol table
                    // move this break to the start
                    break;
                }
            }
        }
    }

    /**
     * Some variable declarations are not done in the regular fashion but inside special fields like
     * the initialization of a for loop.
     *
     * @param aNode        A special node like a for loop
     * @param aSymbolTable The list of variable info wrappers so far
     */
    private static void checkSpecialCasesForVarDecs(Node aNode, List<VariableInfoWrapper> aSymbolTable) {

        if (aNode instanceof ForStmt) {
            ForStmt node = (ForStmt) aNode;
            for (Expression expr : node.getInitialization()) {

                if (expr instanceof VariableDeclarationExpr) {
                    VariableDeclarationExpr vde = (VariableDeclarationExpr) expr;
                    for (VariableDeclarator vd : vde.getVariables()) {
                        aSymbolTable.add(buildVarInfoWrapperFromVarDec(vd));
                    }
                } else {
                    checkAndBuildVariableInfoWrapper(expr, aSymbolTable);
                }
            }
        } else if (aNode instanceof ForeachStmt) {
            ForeachStmt foreach = (ForeachStmt) aNode;
            VariableDeclarationExpr vde = foreach.getVariable();

            for (VariableDeclarator vd : vde.getVariables()) {
                aSymbolTable.add(buildVarInfoWrapperFromVarDec(vd));
            }
        }

    }

    /**
     * Checks if the given node is a statement that declares a variable or assignes a new value
     * to an already existing variable. The latter still needs thinking
     *
     * @param aNode The node that should be checked
     */
    private static void checkAndBuildVariableInfoWrapper(Node aNode, List<VariableInfoWrapper> aSymbolTable) {

        if (aNode instanceof FieldDeclaration) {
            aSymbolTable.add(buildVarInfoWrapperFromFieldDeclaration((FieldDeclaration) aNode));
        }

        if (aNode instanceof Parameter) {
            aSymbolTable.add(buildVarInfoWrapperFromParameter((Parameter) aNode));
        }

        if (aNode instanceof ExpressionStmt) {
            buildVarInfoWrapperFromExpressionStmt((ExpressionStmt) aNode, aSymbolTable);
        }

        // TODO maybe add assignments to variables as well to find the last known value of a variable
    }

    private static List<Class<? extends Node>> getClassHistory(Node aNode) {
        List<Class<? extends Node>> result = new ArrayList<>();

        // TODO implement if this makes any sense

        return result;
    }

    /**
     * @param aNode The node with an assignment of a value to a variable
     * @return True if the node is an assignment of a primitve value which can be easily converted to
     * a string and reconstructed. For complex objects use the node directly
     */
    private static boolean hasPrimitiveValue(Node aNode) {

        if (aNode instanceof BooleanLiteralExpr) {
            return true;
        }

        if (aNode instanceof CharLiteralExpr) {
            return true;
        }

        if (aNode instanceof DoubleLiteralExpr) {
            return true;
        }

        if (aNode instanceof IntegerLiteralExpr) {
            return true;
        }

        if (aNode instanceof LongLiteralExpr) {
            return true;
        }

        if (aNode instanceof NullLiteralExpr) {
            return true;
        }

        if (aNode instanceof StringLiteralExpr) {
            return true;
        }

        return false;
    }

    /**
     * @param aType The type of a variable declaration that was converted to a string beforehand
     * @return True if the type of the declared variable is primitive
     */
    private static boolean hasPrimitiveType(String aType) {

        // no breaks needed
        switch (aType) {
            case "int":
            case "integer":
            case "bool":
            case "boolean":
            case "double":
            case "long":
            case "char":
            case "character":
            case "string":
                return true;
            default:
                return false;
        }

    }

}
