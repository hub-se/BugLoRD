package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;

import java.util.List;

public interface IDetailedNodeMapper<T> extends IBasicNodeMapper<T> {

    @Override
    default public T getMappingForNode(Node aNode, Node parent, int aDepth, boolean includeParent,
                                       List<Node> nextNodes) {
        T result = null;
        // old: just to avoid some null pointer exceptions when a null object is
        // legit
        // update: should catch null in calling methods instead (since we use
        // generics)
        if (aNode == null || aNode instanceof NullNode) {
            return getMappingForNull((NullNode) aNode, parent, includeParent);
        } else if (aNode instanceof NullListNode) {
            return getMappingForNullList((NullListNode) aNode, parent, includeParent);
        } else if (aNode instanceof EmptyListNode) {
            return getMappingForEmptyList((EmptyListNode) aNode, parent, includeParent);
        } else if (aNode instanceof Expression) {
            result = getMappingForExpression((Expression) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof Type) {
            result = getMappingForType((Type) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof Statement) {
            result = getMappingForStatement((Statement) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof BodyDeclaration) {
            result = getMappingForBodyDeclaration((BodyDeclaration<?>) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof Comment) {
            // all comments
            if (aNode instanceof LineComment) {
                result = getMappingForLineComment((LineComment) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof BlockComment) {
                result = getMappingForBlockComment((BlockComment) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof JavadocComment) {
                result = getMappingForJavadocComment((JavadocComment) aNode, parent, aDepth, includeParent, nextNodes);
            }
        } else if (aNode instanceof Parameter) {
            result = getMappingForParameter((Parameter) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof PackageDeclaration) {
            result = getMappingForPackageDeclaration(
                    (PackageDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ImportDeclaration) {
            result = getMappingForImportDeclaration(
                    (ImportDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof CatchClause) {
            result = getMappingForCatchClause((CatchClause) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof VariableDeclarator) {
            result = getMappingForVariableDeclarator(
                    (VariableDeclarator) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof MemberValuePair) {
            result = getMappingForMemberValuePair((MemberValuePair) aNode, parent, aDepth, includeParent, nextNodes);
        }

        // compilation unit
        else if (aNode instanceof CompilationUnit) {
            result = getMappingForCompilationUnit((CompilationUnit) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof Name) {
            result = getMappingForName((Name) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof SimpleName) {
            result = getMappingForSimpleName((SimpleName) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ArrayCreationLevel) {
            result = getMappingForArrayCreationLevel(
                    (ArrayCreationLevel) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ModuleDeclaration) {
            result = getMappingForModuleDeclaration(
                    (ModuleDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ModuleStmt) {
            // all module statements
            if (aNode instanceof ModuleExportsStmt) {
                result = getMappingForModuleExportsStmt(
                        (ModuleExportsStmt) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof ModuleUsesStmt) {
                result = getMappingForModuleUsesStmt((ModuleUsesStmt) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof ModuleProvidesStmt) {
                result = getMappingForModuleProvidesStmt(
                        (ModuleProvidesStmt) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof ModuleRequiresStmt) {
                result = getMappingForModuleRequiresStmt(
                        (ModuleRequiresStmt) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof ModuleOpensStmt) {
                result = getMappingForModuleOpensStmt(
                        (ModuleOpensStmt) aNode, parent, aDepth, includeParent, nextNodes);
            }
        }

        // this should be removed after testing i guess
        // >> I wouldn't remove it, since it doesn't hurt and constitutes a
        // value <<
        else {
            result = getMappingForUnknownNode(aNode, parent, aDepth, includeParent, nextNodes);
        }

        return finalizeMapping(result, aNode, parent, aDepth, includeParent);
    }

    default public T finalizeMapping(T mapping, Node aNode, Node parent, int aDepth, boolean includeParent) {
        return mapping;
    }

    default public T getMappingForTypeDeclaration(TypeDeclaration<?> aNode, Node parent, int aDepth,
                                                  boolean includeParent, List<Node> nextNodes) {
        // old: just to avoid some null pointer exceptions when a null object is
        // legit
        // update: should catch null in calling methods instead (since we use
        // generics)
        if (aNode == null) {
            return getMappingForNull(null, parent, includeParent);
        }

        // all type declarations (may all have annotations)
        if (aNode instanceof AnnotationDeclaration) {
            return getMappingForAnnotationDeclaration(
                    (AnnotationDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ClassOrInterfaceDeclaration) {
            return getMappingForClassOrInterfaceDeclaration(
                    (ClassOrInterfaceDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof EnumDeclaration) {
            return getMappingForEnumDeclaration((EnumDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        }

        return getMappingForUnknownNode(aNode, parent, aDepth, includeParent, nextNodes);
    }

    default public T getMappingForBodyDeclaration(BodyDeclaration<?> aNode, Node parent, int aDepth,
                                                  boolean includeParent, List<Node> nextNodes) {
        // old: just to avoid some null pointer exceptions when a null object is
        // legit
        // update: should catch null in calling methods instead (since we use
        // generics)
        if (aNode == null) {
            return getMappingForNull(null, parent, includeParent);
        }

        // all declarations (may all have annotations)
        if (aNode instanceof InitializerDeclaration) {
            return getMappingForInitializerDeclaration(
                    (InitializerDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof FieldDeclaration) {
            return getMappingForFieldDeclaration((FieldDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof EnumConstantDeclaration) {
            return getMappingForEnumConstantDeclaration(
                    (EnumConstantDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof AnnotationMemberDeclaration) {
            return getMappingForAnnotationMemberDeclaration(
                    (AnnotationMemberDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof TypeDeclaration) {
            return getMappingForTypeDeclaration((TypeDeclaration<?>) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof CallableDeclaration) {
            if (aNode instanceof ConstructorDeclaration) {
                return getMappingForConstructorDeclaration(
                        (ConstructorDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof MethodDeclaration) {
                return getMappingForMethodDeclaration(
                        (MethodDeclaration) aNode, parent, aDepth, includeParent, nextNodes);
            }
        }

        return getMappingForUnknownNode(aNode, parent, aDepth, includeParent, nextNodes);
    }

    default public T getMappingForStatement(Statement aNode, Node parent, int aDepth, boolean includeParent,
                                            List<Node> nextNodes) {
        // old: just to avoid some null pointer exceptions when a null object is
        // legit
        // update: should catch null in calling methods instead (since we use
        // generics)
        if (aNode == null) {
            return getMappingForNull(null, parent, includeParent);
        }

        // all statements
        if (aNode instanceof AssertStmt) {
            return getMappingForAssertStmt((AssertStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof BlockStmt) {
            return getMappingForBlockStmt((BlockStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof BreakStmt) {
            return getMappingForBreakStmt((BreakStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ContinueStmt) {
            return getMappingForContinueStmt((ContinueStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof DoStmt) {
            return getMappingForDoStmt((DoStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ExplicitConstructorInvocationStmt) {
            return getMappingForExplicitConstructorInvocationStmt(
                    (ExplicitConstructorInvocationStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ExpressionStmt) {
            return getMappingForExpressionStmt((ExpressionStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ForeachStmt) {
            return getMappingForForeachStmt((ForeachStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ForStmt) {
            return getMappingForForStmt((ForStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof IfStmt) {
            return getMappingForIfStmt((IfStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof LabeledStmt) {
            return getMappingForLabeledStmt((LabeledStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ReturnStmt) {
            return getMappingForReturnStmt((ReturnStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof SwitchEntryStmt) {
            return getMappingForSwitchEntryStmt((SwitchEntryStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof SwitchStmt) {
            return getMappingForSwitchStmt((SwitchStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof SynchronizedStmt) {
            return getMappingForSynchronizedStmt((SynchronizedStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ThrowStmt) {
            return getMappingForThrowStmt((ThrowStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof TryStmt) {
            return getMappingForTryStmt((TryStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof WhileStmt) {
            return getMappingForWhileStmt((WhileStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof LocalClassDeclarationStmt) {
            return getMappingForLocalClassDeclarationStmt(
                    (LocalClassDeclarationStmt) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof EmptyStmt) {
            return getMappingForEmptyStmt((EmptyStmt) aNode, parent, includeParent, nextNodes);
        }

        return getMappingForUnknownNode(aNode, parent, aDepth, includeParent, nextNodes);
    }

    default public T getMappingForType(Type aNode, Node parent, int aDepth, boolean includeParent,
                                       List<Node> nextNodes) {
        // old: just to avoid some null pointer exceptions when a null object is
        // legit
        // update: should catch null in calling methods instead (since we use
        // generics)
        if (aNode == null) {
            return getMappingForNull(null, parent, includeParent);
        }

        // all types
        if (aNode instanceof IntersectionType) {
            return getMappingForIntersectionType((IntersectionType) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof PrimitiveType) {
            return getMappingForPrimitiveType((PrimitiveType) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ReferenceType) {
            if (aNode instanceof ClassOrInterfaceType) {
                return getMappingForClassOrInterfaceType(
                        (ClassOrInterfaceType) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof TypeParameter) {
                return getMappingForTypeParameter((TypeParameter) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof ArrayType) {
                return getMappingForArrayType((ArrayType) aNode, parent, aDepth, includeParent, nextNodes);
            }
        } else if (aNode instanceof UnionType) {
            return getMappingForUnionType((UnionType) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof UnknownType) {
            return getMappingForUnknownType((UnknownType) aNode, parent, includeParent, nextNodes);
        } else if (aNode instanceof VoidType) {
            return getMappingForVoidType((VoidType) aNode, parent, includeParent, nextNodes);
        } else if (aNode instanceof WildcardType) {
            return getMappingForWildcardType((WildcardType) aNode, parent, aDepth, includeParent, nextNodes);
        }

        return getMappingForUnknownNode(aNode, parent, aDepth, includeParent, nextNodes);
    }

    default public T getMappingForExpression(Expression aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes) {
        // old: just to avoid some null pointer exceptions when a null object is
        // legit
        // update: should catch null in calling methods instead (since we use
        // generics)
        if (aNode == null) {
            return getMappingForNull(null, parent, includeParent);
        }

        // all expressions
        if (aNode instanceof LiteralExpr) {
            if (aNode instanceof NullLiteralExpr) {
                return getMappingForNullLiteralExpr((NullLiteralExpr) aNode, parent, includeParent, nextNodes);
            } else if (aNode instanceof BooleanLiteralExpr) {
                return getMappingForBooleanLiteralExpr((BooleanLiteralExpr) aNode, parent, includeParent, nextNodes);
            } else if (aNode instanceof LiteralStringValueExpr) {
                if (aNode instanceof StringLiteralExpr) {
                    return getMappingForStringLiteralExpr((StringLiteralExpr) aNode, parent, includeParent, nextNodes);
                } else if (aNode instanceof CharLiteralExpr) {
                    return getMappingForCharLiteralExpr((CharLiteralExpr) aNode, parent, includeParent, nextNodes);
                } else if (aNode instanceof IntegerLiteralExpr) {
                    return getMappingForIntegerLiteralExpr(
                            (IntegerLiteralExpr) aNode, parent, includeParent, nextNodes);
                } else if (aNode instanceof LongLiteralExpr) {
                    return getMappingForLongLiteralExpr((LongLiteralExpr) aNode, parent, includeParent, nextNodes);
                } else if (aNode instanceof DoubleLiteralExpr) {
                    return getMappingForDoubleLiteralExpr((DoubleLiteralExpr) aNode, parent, includeParent, nextNodes);
                }
            }
        } else if (aNode instanceof ArrayAccessExpr) {
            return getMappingForArrayAccessExpr((ArrayAccessExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ArrayCreationExpr) {
            return getMappingForArrayCreationExpr((ArrayCreationExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ArrayInitializerExpr) {
            return getMappingForArrayInitializerExpr(
                    (ArrayInitializerExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof AssignExpr) {
            return getMappingForAssignExpr((AssignExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof BinaryExpr) {
            return getMappingForBinaryExpr((BinaryExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof CastExpr) {
            return getMappingForCastExpr((CastExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ClassExpr) {
            return getMappingForClassExpr((ClassExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ConditionalExpr) {
            return getMappingForConditionalExpr((ConditionalExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof FieldAccessExpr) {
            return getMappingForFieldAccessExpr((FieldAccessExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof InstanceOfExpr) {
            return getMappingForInstanceOfExpr((InstanceOfExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof LambdaExpr) {
            return getMappingForLambdaExpr((LambdaExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof MethodCallExpr) {
            return getMappingForMethodCallExpr((MethodCallExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof MethodReferenceExpr) {
            return getMappingForMethodReferenceExpr(
                    (MethodReferenceExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ThisExpr) {
            return getMappingForThisExpr((ThisExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof EnclosedExpr) {
            return getMappingForEnclosedExpr((EnclosedExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof ObjectCreationExpr) {
            return getMappingForObjectCreationExpr(
                    (ObjectCreationExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof UnaryExpr) {
            return getMappingForUnaryExpr((UnaryExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof SuperExpr) {
            return getMappingForSuperExpr((SuperExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof TypeExpr) {
            return getMappingForTypeExpr((TypeExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof VariableDeclarationExpr) {
            return getMappingForVariableDeclarationExpr(
                    (VariableDeclarationExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof NameExpr) {
            return getMappingForNameExpr((NameExpr) aNode, parent, aDepth, includeParent, nextNodes);
        } else if (aNode instanceof AnnotationExpr) {
            if (aNode instanceof MarkerAnnotationExpr) {
                return getMappingForMarkerAnnotationExpr(
                        (MarkerAnnotationExpr) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof NormalAnnotationExpr) {
                return getMappingForNormalAnnotationExpr(
                        (NormalAnnotationExpr) aNode, parent, aDepth, includeParent, nextNodes);
            } else if (aNode instanceof SingleMemberAnnotationExpr) {
                return getMappingForSingleMemberAnnotationExpr(
                        (SingleMemberAnnotationExpr) aNode, parent, aDepth, includeParent, nextNodes);
            }
        }

        return getMappingForUnknownNode(aNode, parent, aDepth, includeParent, nextNodes);
    }

    public T getMappingForNull(NullNode aNode, Node parent, boolean includeParent);

    public T getMappingForNullList(NullListNode aNode, Node parent, boolean includeParent);

    public T getMappingForEmptyList(EmptyListNode aNode, Node parent, boolean includeParent);

    public T getMappingForUnknownNode(Node aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForCompilationUnit(CompilationUnit aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForMemberValuePair(MemberValuePair aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForVariableDeclarator(VariableDeclarator aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForCatchClause(CatchClause aNode, Node parent, int aDepth, boolean includeParent,
                                      List<Node> nextNodes);

    public T getMappingForTypeParameter(TypeParameter aNode, Node parent, int aDepth, boolean includeParent,
                                        List<Node> nextNodes);

    public T getMappingForImportDeclaration(ImportDeclaration aNode, Node parent, int aDepth, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForPackageDeclaration(PackageDeclaration aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForParameter(Parameter aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForJavadocComment(JavadocComment aNode, Node parent, int aDepth, boolean includeParent,
                                         List<Node> nextNodes);

    public T getMappingForBlockComment(BlockComment aNode, Node parent, int aDepth, boolean includeParent,
                                       List<Node> nextNodes);

    public T getMappingForLineComment(LineComment aNode, Node parent, int aDepth, boolean includeParent,
                                      List<Node> nextNodes);

    public T getMappingForEnumDeclaration(EnumDeclaration aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForClassOrInterfaceDeclaration(ClassOrInterfaceDeclaration aNode, Node parent, int aDepth,
                                                      boolean includeParent, List<Node> nextNodes);

    public T getMappingForAnnotationDeclaration(AnnotationDeclaration aNode, Node parent, int aDepth,
                                                boolean includeParent, List<Node> nextNodes);

    public T getMappingForAnnotationMemberDeclaration(AnnotationMemberDeclaration aNode, Node parent, int aDepth,
                                                      boolean includeParent, List<Node> nextNodes);

    public T getMappingForEnumConstantDeclaration(EnumConstantDeclaration aNode, Node parent, int aDepth,
                                                  boolean includeParent, List<Node> nextNodes);

    public T getMappingForMethodDeclaration(MethodDeclaration aNode, Node parent, int aDepth, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForFieldDeclaration(FieldDeclaration aNode, Node parent, int aDepth, boolean includeParent,
                                           List<Node> nextNodes);

    public T getMappingForInitializerDeclaration(InitializerDeclaration aNode, Node parent, int aDepth,
                                                 boolean includeParent, List<Node> nextNodes);

    public T getMappingForConstructorDeclaration(ConstructorDeclaration aNode, Node parent, int aDepth,
                                                 boolean includeParent, List<Node> nextNodes);

    public T getMappingForWhileStmt(WhileStmt aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForTryStmt(TryStmt aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForThrowStmt(ThrowStmt aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForSynchronizedStmt(SynchronizedStmt aNode, Node parent, int aDepth, boolean includeParent,
                                           List<Node> nextNodes);

    public T getMappingForSwitchStmt(SwitchStmt aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForSwitchEntryStmt(SwitchEntryStmt aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForReturnStmt(ReturnStmt aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForLabeledStmt(LabeledStmt aNode, Node parent, int aDepth, boolean includeParent,
                                      List<Node> nextNodes);

    public T getMappingForIfStmt(IfStmt aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForForStmt(ForStmt aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForForeachStmt(ForeachStmt aNode, Node parent, int aDepth, boolean includeParent,
                                      List<Node> nextNodes);

    public T getMappingForExpressionStmt(ExpressionStmt aNode, Node parent, int aDepth, boolean includeParent,
                                         List<Node> nextNodes);

    public T getMappingForExplicitConstructorInvocationStmt(ExplicitConstructorInvocationStmt aNode, Node parent,
                                                            int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForDoStmt(DoStmt aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForContinueStmt(ContinueStmt aNode, Node parent, int aDepth, boolean includeParent,
                                       List<Node> nextNodes);

    public T getMappingForBreakStmt(BreakStmt aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForBlockStmt(BlockStmt aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForAssertStmt(AssertStmt aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForWildcardType(WildcardType aNode, Node parent, int aDepth, boolean includeParent,
                                       List<Node> nextNodes);

    public T getMappingForVoidType(VoidType aNode, Node parent, boolean includeParent, List<Node> nextNodes);

    public T getMappingForUnknownType(UnknownType aNode, Node parent, boolean includeParent, List<Node> nextNodes);

    public T getMappingForUnionType(UnionType aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForPrimitiveType(PrimitiveType aNode, Node parent, int aDepth, boolean includeParent,
                                        List<Node> nextNodes);

    public T getMappingForIntersectionType(IntersectionType aNode, Node parent, int aDepth, boolean includeParent,
                                           List<Node> nextNodes);

    public T getMappingForClassOrInterfaceType(ClassOrInterfaceType aNode, Node parent, int aDepth,
                                               boolean includeParent, List<Node> nextNodes);

    public T getMappingForSingleMemberAnnotationExpr(SingleMemberAnnotationExpr aNode, Node parent, int aDepth,
                                                     boolean includeParent, List<Node> nextNodes);

    public T getMappingForNormalAnnotationExpr(NormalAnnotationExpr aNode, Node parent, int aDepth,
                                               boolean includeParent, List<Node> nextNodes);

    public T getMappingForMarkerAnnotationExpr(MarkerAnnotationExpr aNode, Node parent, int aDepth,
                                               boolean includeParent, List<Node> nextNodes);

    public T getMappingForNameExpr(NameExpr aNode, Node parent, int aDepth, boolean includeParent,
                                   List<Node> nextNodes);

    public T getMappingForVariableDeclarationExpr(VariableDeclarationExpr aNode, Node parent, int aDepth,
                                                  boolean includeParent, List<Node> nextNodes);

    public T getMappingForTypeExpr(TypeExpr aNode, Node parent, int aDepth, boolean includeParent,
                                   List<Node> nextNodes);

    public T getMappingForSuperExpr(SuperExpr aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForUnaryExpr(UnaryExpr aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForObjectCreationExpr(ObjectCreationExpr aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForEnclosedExpr(EnclosedExpr aNode, Node parent, int aDepth, boolean includeParent,
                                       List<Node> nextNodes);

    public T getMappingForThisExpr(ThisExpr aNode, Node parent, int aDepth, boolean includeParent,
                                   List<Node> nextNodes);

    public T getMappingForMethodReferenceExpr(MethodReferenceExpr aNode, Node parent, int aDepth, boolean includeParent,
                                              List<Node> nextNodes);

    public T getMappingForMethodCallExpr(MethodCallExpr aNode, Node parent, int aDepth, boolean includeParent,
                                         List<Node> nextNodes);

    public T getMappingForLambdaExpr(LambdaExpr aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForInstanceOfExpr(InstanceOfExpr aNode, Node parent, int aDepth, boolean includeParent,
                                         List<Node> nextNodes);

    public T getMappingForFieldAccessExpr(FieldAccessExpr aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForConditionalExpr(ConditionalExpr aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForClassExpr(ClassExpr aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForCastExpr(CastExpr aNode, Node parent, int aDepth, boolean includeParent,
                                   List<Node> nextNodes);

    public T getMappingForBinaryExpr(BinaryExpr aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForAssignExpr(AssignExpr aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForArrayInitializerExpr(ArrayInitializerExpr aNode, Node parent, int aDepth,
                                               boolean includeParent, List<Node> nextNodes);

    public T getMappingForArrayCreationExpr(ArrayCreationExpr aNode, Node parent, int aDepth, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForArrayAccessExpr(ArrayAccessExpr aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForStringLiteralExpr(StringLiteralExpr aNode, Node parent, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForDoubleLiteralExpr(DoubleLiteralExpr aNode, Node parent, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForLongLiteralExpr(LongLiteralExpr aNode, Node parent, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForIntegerLiteralExpr(IntegerLiteralExpr aNode, Node parent, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForCharLiteralExpr(CharLiteralExpr aNode, Node parent, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForBooleanLiteralExpr(BooleanLiteralExpr aNode, Node parent, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForNullLiteralExpr(NullLiteralExpr aNode, Node parent, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForName(Name aNode, Node parent, int aDepth, boolean includeParent, List<Node> nextNodes);

    public T getMappingForSimpleName(SimpleName aNode, Node parent, int aDepth, boolean includeParent,
                                     List<Node> nextNodes);

    public T getMappingForLocalClassDeclarationStmt(LocalClassDeclarationStmt aNode, Node parent, int aDepth,
                                                    boolean includeParent, List<Node> nextNodes);

    public T getMappingForArrayType(ArrayType aNode, Node parent, int aDepth, boolean includeParent,
                                    List<Node> nextNodes);

    public T getMappingForArrayCreationLevel(ArrayCreationLevel aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForModuleDeclaration(ModuleDeclaration aNode, Node parent, int aDepth, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForModuleExportsStmt(ModuleExportsStmt aNode, Node parent, int aDepth, boolean includeParent,
                                            List<Node> nextNodes);

    public T getMappingForModuleOpensStmt(ModuleOpensStmt aNode, Node parent, int aDepth, boolean includeParent,
                                          List<Node> nextNodes);

    public T getMappingForModuleProvidesStmt(ModuleProvidesStmt aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForModuleRequiresStmt(ModuleRequiresStmt aNode, Node parent, int aDepth, boolean includeParent,
                                             List<Node> nextNodes);

    public T getMappingForModuleUsesStmt(ModuleUsesStmt aNode, Node parent, int aDepth, boolean includeParent,
                                         List<Node> nextNodes);

    public T getMappingForEmptyStmt(EmptyStmt aNode, Node parent, boolean includeParent, List<Node> nextNodes);

}
