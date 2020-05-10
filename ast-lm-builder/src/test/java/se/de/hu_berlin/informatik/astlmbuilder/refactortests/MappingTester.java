package se.de.hu_berlin.informatik.astlmbuilder.refactortests;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnknownType;
import org.junit.Assert;
import org.junit.Test;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider.KeyWords;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.SimpleMapper;

public class MappingTester {

    @Test
    public void testAbstractionMapping1() {
        IMapper<String> mapper = new Node2AbstractionMapper.Builder(new KeyWordConstants()).usesCharAbstraction()
                .usesStringAbstraction().usesBooleanAbstraction().usesNumberAbstraction().build();
        IKeyWordProvider<String> kwc = new KeyWordConstants();
        testWithMapperAndProvider(mapper, kwc);
    }

    @Test
    public void testAbstractionMapping2() {
        IMapper<String> mapper = new Node2AbstractionMapper.Builder(new KeyWordConstantsShort()).usesCharAbstraction()
                .usesStringAbstraction().usesBooleanAbstraction().usesNumberAbstraction().build();
        IKeyWordProvider<String> kwc = new KeyWordConstantsShort();
        testWithMapperAndProvider(mapper, kwc);
    }

    @Test
    public void testSimpleMapping1() {
        IMapper<String> mapper = new SimpleMapper<>(new KeyWordConstants());
        IKeyWordProvider<String> kwc = new KeyWordConstants();
        testWithMapperAndProvider(mapper, kwc);
    }

    @Test
    public void testSimpleMapping2() {
        IMapper<String> mapper = new SimpleMapper<>(new KeyWordConstantsShort());
        IKeyWordProvider<String> kwc = new KeyWordConstantsShort();
        testWithMapperAndProvider(mapper, kwc);
    }

    public void testWithMapperAndProvider(IMapper<String> mapper, IKeyWordProvider<String> kwc) {
        CompilationUnit node1 = new CompilationUnit();
        Assert.assertEquals(mapper.getMappingForNode(node1, null, 0, false, null), kwc.getKeyWord(KeyWords.COMPILATION_UNIT));

        AnnotationDeclaration node2 = new AnnotationDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node2, null, 0, false, null), kwc.getKeyWord(KeyWords.ANNOTATION_DECLARATION));

        AnnotationMemberDeclaration node3 = new AnnotationMemberDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node3, null, 0, false, null), kwc.getKeyWord(KeyWords.ANNOTATION_MEMBER_DECLARATION));

        ClassOrInterfaceDeclaration node4 = new ClassOrInterfaceDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node4, null, 0, false, null), kwc.getKeyWord(KeyWords.CLASS_OR_INTERFACE_DECLARATION));

        ConstructorDeclaration node5 = new ConstructorDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node5, null, 0, false, null), kwc.getKeyWord(KeyWords.CONSTRUCTOR_DECLARATION));

        EnumConstantDeclaration node8 = new EnumConstantDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node8, null, 0, false, null), kwc.getKeyWord(KeyWords.ENUM_CONSTANT_DECLARATION));

        EnumDeclaration node9 = new EnumDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node9, null, 0, false, null), kwc.getKeyWord(KeyWords.ENUM_DECLARATION));

        FieldDeclaration node10 = new FieldDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node10, null, 0, false, null), kwc.getKeyWord(KeyWords.FIELD_DECLARATION));

        InitializerDeclaration node11 = new InitializerDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node11, null, 0, false, null), kwc.getKeyWord(KeyWords.INITIALIZER_DECLARATION));

        MethodDeclaration node12 = new MethodDeclaration();
        Assert.assertEquals(mapper.getMappingForNode(node12, null, 0, false, null), kwc.getKeyWord(KeyWords.METHOD_DECLARATION));

        Parameter node14 = new Parameter();
        Assert.assertEquals(mapper.getMappingForNode(node14, null, 0, false, null), kwc.getKeyWord(KeyWords.PARAMETER));

        VariableDeclarator node15 = new VariableDeclarator(new UnknownType(), "name");
        Assert.assertEquals(mapper.getMappingForNode(node15, null, 0, false, null), kwc.getKeyWord(KeyWords.VARIABLE_DECLARATOR));

        AssignExpr node17 = new AssignExpr();
        Assert.assertEquals(mapper.getMappingForNode(node17, null, 0, false, null), kwc.getKeyWord(KeyWords.ASSIGN_EXPRESSION));

        BinaryExpr node18 = new BinaryExpr();
        Assert.assertEquals(mapper.getMappingForNode(node18, null, 0, false, null), kwc.getKeyWord(KeyWords.BINARY_EXPRESSION));

        BooleanLiteralExpr node19 = new BooleanLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node19, null, 0, false, null), kwc.getKeyWord(KeyWords.BOOLEAN_LITERAL_EXPRESSION));

        CastExpr node20 = new CastExpr();
        Assert.assertEquals(mapper.getMappingForNode(node20, null, 0, false, null), kwc.getKeyWord(KeyWords.CAST_EXPRESSION));

        CharLiteralExpr node21 = new CharLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node21, null, 0, false, null), kwc.getKeyWord(KeyWords.CHAR_LITERAL_EXPRESSION));

        ClassExpr node22 = new ClassExpr();
        Assert.assertEquals(mapper.getMappingForNode(node22, null, 0, false, null), kwc.getKeyWord(KeyWords.CLASS_EXPRESSION));

        ConditionalExpr node23 = new ConditionalExpr();
        Assert.assertEquals(mapper.getMappingForNode(node23, null, 0, false, null), kwc.getKeyWord(KeyWords.CONDITIONAL_EXPRESSION));

        DoubleLiteralExpr node24 = new DoubleLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node24, null, 0, false, null), kwc.getKeyWord(KeyWords.DOUBLE_LITERAL_EXPRESSION));

        EnclosedExpr node25 = new EnclosedExpr();
        Assert.assertEquals(mapper.getMappingForNode(node25, null, 0, false, null), kwc.getKeyWord(KeyWords.ENCLOSED_EXPRESSION));

        FieldAccessExpr node26 = new FieldAccessExpr();
        Assert.assertEquals(mapper.getMappingForNode(node26, null, 0, false, null), kwc.getKeyWord(KeyWords.FIELD_ACCESS_EXPRESSION));

        InstanceOfExpr node27 = new InstanceOfExpr();
        Assert.assertEquals(mapper.getMappingForNode(node27, null, 0, false, null), kwc.getKeyWord(KeyWords.INSTANCEOF_EXPRESSION));

        IntegerLiteralExpr node28 = new IntegerLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node28, null, 0, false, null), kwc.getKeyWord(KeyWords.INTEGER_LITERAL_EXPRESSION));

        LambdaExpr node30 = new LambdaExpr();
        Assert.assertEquals(mapper.getMappingForNode(node30, null, 0, false, null), kwc.getKeyWord(KeyWords.LAMBDA_EXPRESSION));

        LongLiteralExpr node31 = new LongLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node31, null, 0, false, null), kwc.getKeyWord(KeyWords.LONG_LITERAL_EXPRESSION));

        MarkerAnnotationExpr node33 = new MarkerAnnotationExpr();
        Assert.assertEquals(mapper.getMappingForNode(node33, null, 0, false, null), kwc.getKeyWord(KeyWords.MARKER_ANNOTATION_EXPRESSION));

        MemberValuePair node34 = new MemberValuePair();
        Assert.assertEquals(mapper.getMappingForNode(node34, null, 0, false, null), kwc.getKeyWord(KeyWords.MEMBER_VALUE_PAIR));

        MethodCallExpr node35 = new MethodCallExpr();
        Assert.assertEquals(mapper.getMappingForNode(node35, null, 0, false, null), kwc.getKeyWord(KeyWords.METHOD_CALL_EXPRESSION));

        MethodReferenceExpr node36 = new MethodReferenceExpr();
        Assert.assertEquals(mapper.getMappingForNode(node36, null, 0, false, null), kwc.getKeyWord(KeyWords.METHOD_REFERENCE_EXPRESSION));

        NameExpr node37 = new NameExpr();
        Assert.assertEquals(mapper.getMappingForNode(node37, null, 0, false, null), kwc.getKeyWord(KeyWords.NAME_EXPRESSION));

        NormalAnnotationExpr node38 = new NormalAnnotationExpr();
        Assert.assertEquals(mapper.getMappingForNode(node38, null, 0, false, null), kwc.getKeyWord(KeyWords.NORMAL_ANNOTATION_EXPRESSION));

        NullLiteralExpr node39 = new NullLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node39, null, 0, false, null), kwc.getKeyWord(KeyWords.NULL_LITERAL_EXPRESSION));

        ObjectCreationExpr node40 = new ObjectCreationExpr();
        Assert.assertEquals(mapper.getMappingForNode(node40, null, 0, false, null), kwc.getKeyWord(KeyWords.OBJ_CREATE_EXPRESSION));

        SingleMemberAnnotationExpr node42 = new SingleMemberAnnotationExpr();
        Assert.assertEquals(mapper.getMappingForNode(node42, null, 0, false, null), kwc.getKeyWord(KeyWords.SINGLE_MEMBER_ANNOTATION_EXPRESSION));

        StringLiteralExpr node43 = new StringLiteralExpr();
        Assert.assertEquals(mapper.getMappingForNode(node43, null, 0, false, null), kwc.getKeyWord(KeyWords.STRING_LITERAL_EXPRESSION));

        SuperExpr node44 = new SuperExpr();
        Assert.assertEquals(mapper.getMappingForNode(node44, null, 0, false, null), kwc.getKeyWord(KeyWords.SUPER_EXPRESSION));

        ThisExpr node45 = new ThisExpr();
        Assert.assertEquals(mapper.getMappingForNode(node45, null, 0, false, null), kwc.getKeyWord(KeyWords.THIS_EXPRESSION));

        TypeExpr node46 = new TypeExpr();
        Assert.assertEquals(mapper.getMappingForNode(node46, null, 0, false, null), kwc.getKeyWord(KeyWords.TYPE_EXPRESSION));

        UnaryExpr node47 = new UnaryExpr();
        Assert.assertEquals(mapper.getMappingForNode(node47, null, 0, false, null), kwc.getKeyWord(KeyWords.UNARY_EXPRESSION));

        VariableDeclarationExpr node48 = new VariableDeclarationExpr();
        Assert.assertEquals(mapper.getMappingForNode(node48, null, 0, false, null), kwc.getKeyWord(KeyWords.VARIABLE_DECLARATION_EXPRESSION));

        AssertStmt node49 = new AssertStmt();
        Assert.assertEquals(mapper.getMappingForNode(node49, null, 0, false, null), kwc.getKeyWord(KeyWords.ASSERT_STMT));

        BlockStmt node50 = new BlockStmt();
        Assert.assertEquals(mapper.getMappingForNode(node50, null, 0, false, null), kwc.getKeyWord(KeyWords.BLOCK_STMT));

        BreakStmt node51 = new BreakStmt();
        Assert.assertEquals(mapper.getMappingForNode(node51, null, 0, false, null), kwc.getKeyWord(KeyWords.BREAK));

        CatchClause node52 = new CatchClause();
        Assert.assertEquals(mapper.getMappingForNode(node52, null, 0, false, null), kwc.getKeyWord(KeyWords.CATCH_CLAUSE_STMT));

        ContinueStmt node53 = new ContinueStmt();
        Assert.assertEquals(mapper.getMappingForNode(node53, null, 0, false, null), kwc.getKeyWord(KeyWords.CONTINUE_STMT));

        DoStmt node54 = new DoStmt();
        Assert.assertEquals(mapper.getMappingForNode(node54, null, 0, false, null), kwc.getKeyWord(KeyWords.DO_STMT));

        ExplicitConstructorInvocationStmt node56 = new ExplicitConstructorInvocationStmt();
        Assert.assertEquals(mapper.getMappingForNode(node56, null, 0, false, null), kwc.getKeyWord(KeyWords.EXPL_CONSTR_INVOC_STMT));

        ExpressionStmt node57 = new ExpressionStmt();
        Assert.assertEquals(mapper.getMappingForNode(node57, null, 0, false, null), kwc.getKeyWord(KeyWords.EXPRESSION_STMT));

        ForStmt node58 = new ForStmt();
        Assert.assertEquals(mapper.getMappingForNode(node58, null, 0, false, null), kwc.getKeyWord(KeyWords.FOR_STMT));

        ForeachStmt node59 = new ForeachStmt();
        Assert.assertEquals(mapper.getMappingForNode(node59, null, 0, false, null), kwc.getKeyWord(KeyWords.FOR_EACH_STMT));

        IfStmt node60 = new IfStmt();
        Assert.assertEquals(mapper.getMappingForNode(node60, null, 0, false, null), kwc.getKeyWord(KeyWords.IF_STMT));

        LabeledStmt node61 = new LabeledStmt();
        Assert.assertEquals(mapper.getMappingForNode(node61, null, 0, false, null), kwc.getKeyWord(KeyWords.LABELED_STMT));

        ReturnStmt node62 = new ReturnStmt();
        Assert.assertEquals(mapper.getMappingForNode(node62, null, 0, false, null), kwc.getKeyWord(KeyWords.RETURN_STMT));

        SwitchEntryStmt node63 = new SwitchEntryStmt();
        Assert.assertEquals(mapper.getMappingForNode(node63, null, 0, false, null), kwc.getKeyWord(KeyWords.SWITCH_ENTRY_STMT));

        SwitchStmt node64 = new SwitchStmt();
        Assert.assertEquals(mapper.getMappingForNode(node64, null, 0, false, null), kwc.getKeyWord(KeyWords.SWITCH_STMT));

        SynchronizedStmt node65 = new SynchronizedStmt();
        Assert.assertEquals(mapper.getMappingForNode(node65, null, 0, false, null), kwc.getKeyWord(KeyWords.SYNCHRONIZED_STMT));

        ThrowStmt node66 = new ThrowStmt();
        Assert.assertEquals(mapper.getMappingForNode(node66, null, 0, false, null), kwc.getKeyWord(KeyWords.THROW_STMT));

        TryStmt node67 = new TryStmt();
        Assert.assertEquals(mapper.getMappingForNode(node67, null, 0, false, null), kwc.getKeyWord(KeyWords.TRY_STMT));

        WhileStmt node69 = new WhileStmt();
        Assert.assertEquals(mapper.getMappingForNode(node69, null, 0, false, null), kwc.getKeyWord(KeyWords.WHILE_STMT));

        ClassOrInterfaceType node70 = new ClassOrInterfaceType();
        Assert.assertEquals(mapper.getMappingForNode(node70, null, 0, false, null), kwc.getKeyWord(KeyWords.CLASS_OR_INTERFACE_TYPE));

        // some nodes that were added after the first generation
        BlockComment node71 = new BlockComment();
        Assert.assertEquals(mapper.getMappingForNode(node71, null, 0, false, null), kwc.getKeyWord(KeyWords.BLOCK_COMMENT));

        JavadocComment node72 = new JavadocComment();
        Assert.assertEquals(mapper.getMappingForNode(node72, null, 0, false, null), kwc.getKeyWord(KeyWords.JAVADOC_COMMENT));

        LineComment node73 = new LineComment();
        Assert.assertEquals(mapper.getMappingForNode(node73, null, 0, false, null), kwc.getKeyWord(KeyWords.LINE_COMMENT));

        ArrayAccessExpr node74 = new ArrayAccessExpr();
        Assert.assertEquals(mapper.getMappingForNode(node74, null, 0, false, null), kwc.getKeyWord(KeyWords.ARRAY_ACCESS_EXPRESSION));

        ArrayCreationExpr node75 = new ArrayCreationExpr();
        Assert.assertEquals(mapper.getMappingForNode(node75, null, 0, false, null), kwc.getKeyWord(KeyWords.ARRAY_CREATE_EXPRESSION));

        ArrayInitializerExpr node76 = new ArrayInitializerExpr();
        Assert.assertEquals(mapper.getMappingForNode(node76, null, 0, false, null), kwc.getKeyWord(KeyWords.ARRAY_INIT_EXPRESSION));
    }
}
