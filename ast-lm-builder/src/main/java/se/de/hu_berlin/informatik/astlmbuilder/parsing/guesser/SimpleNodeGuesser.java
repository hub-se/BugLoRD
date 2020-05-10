package se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr.Operator;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;

import java.util.EnumSet;

/**
 * A simple implementation of the node guesser (only for testing at the moment)
 */
public class SimpleNodeGuesser implements INodeGuesser {

    @Override
    public <T extends Node> T guessNode(Class<T> expectedSuperClazz, InformationWrapper info) {
        // TODO implement
        return null;
    }

    @Override
    public <T extends Node> NodeList<T> guessList(Class<T> expectedSuperClazz, int listMemberCount,
                                                  InformationWrapper info) {
        return new NodeList<>();
    }

    @Override
    public String guessMethodIdentifier(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Node> NodeList<T> guessList(Class<T> expectedSuperClazz, InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeList<BodyDeclaration<?>> guessBodyDeclarationList(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeList<TypeDeclaration<?>> guessTypeDeclarationList(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EnumSet<Modifier> guessModifiers(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean guessBoolean(InformationWrapper info) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String guessStringValue(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Primitive guessPrimitive(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Operator guessAssignOperator(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public com.github.javaparser.ast.expr.UnaryExpr.Operator guessUnaryOperator(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public com.github.javaparser.ast.expr.BinaryExpr.Operator guessBinaryOperator(InformationWrapper info) {
        // TODO Auto-generated method stub
        return null;
    }

}
