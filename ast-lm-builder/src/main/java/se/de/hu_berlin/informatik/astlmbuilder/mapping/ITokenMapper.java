package se.de.hu_berlin.informatik.astlmbuilder.mapping;

import java.util.Collection;

import com.github.javaparser.ast.Node;

public interface ITokenMapper<T> {

	public static final String COMPILATION_UNIT = "COMP_UNIT";
	public static final String LINE_COMMENT = "LINE_COMMENT";
	public static final String BLOCK_COMMENT = "BLOCK_COMMENT";
	public static final String JAVADOC_COMMENT = "JAVADOC_COMMENT";
	
	public static final String CONSTRUCTOR_DECLARATION = "CNSTR_DEC";
	public static final String INITIALIZER_DECLARATION = "INIT_DEC";
	public static final String VARIABLE_DECLARATION = "VAR_DEC";
	public static final String WHILE_STATEMENT = "WHILE";
	public static final String TRY_STATEMENT = "TRY";
	public static final String THROW_STATEMENT = "THROW";
	public static final String SYNCHRONIZED_STATEMENT = "SYNC";
	public static final String SWITCH_STATEMENT = "SWITCH";
	public static final String SWITCH_ENTRY_STATEMENT = "SWITCH_ENTRY";
	public static final String RETURN_STATEMENT = "RETURN";
	public static final String LABELED_STATEMENT = "LABELED";
	public static final String IF_STATEMENT = "IF";
	public static final String FOR_STATEMENT = "FOR";
	public static final String FOR_EACH_STATEMENT = "FOR_EACH";
	public static final String EXPRESSION_STATEMENT = "EXPR_STMT";
	public static final String EXPLICIT_CONSTRUCTOR_STATEMENT = "EXPL_CONSTR";
	public static final String EMPTY_STATEMENT = "EMPTY";
	public static final String DO_STATEMENT = "DO";
	public static final String CONTINUE_STATEMENT = "CONTINUE";
	public static final String CATCH_CLAUSE_STATEMENT = "CATCH";
	public static final String BLOCK_STATEMENT = "BLOCK";
	public static final String VARIABLE_DECLARATION_ID = "VAR_DEC_ID";
	public static final String VARIABLE_DECLARATION_EXPRESSION = "VAR_DEC_EXPR";
	public static final String TYPE_EXPRESSION = "TYPE_EXPR";
	public static final String SUPER_EXPRESSION = "SUPER";
	public static final String QUALIFIED_NAME_EXPRESSION = "QUALIFIED_NAME";
	public static final String NULL_LITERAL_EXPRESSION = "NULL_LIT";
	public static final String METHOD_REFERENCE_EXPRESSION = "M_REF";
	public static final String LONG_LITERAL_MIN_VALUE_EXPRESSION = "LONG_LIT_MIN";
	public static final String LAMBDA_EXPRESSION = "LAMBDA";
	public static final String INTEGER_LITERAL_MIN_VALUE_EXPRESSION = "INT_LIT_MIN";
	public static final String INSTANCEOF_EXPRESSION = "INSTANCEOF";
	public static final String FIELD_ACCESS_EXPRESSION = "FIELD_ACC";
	public static final String CONDITIONAL_EXPRESSION = "CONDITION";
	public static final String CLASS_EXPRESSION = "CLASS";
	public static final String CAST_EXPRESSION = "CAST";
	public static final String ASSIGN_EXPRESSION = "ASSIGN";
	public static final String ARRAY_INIT_EXPRESSION = "INIT_ARR";
	public static final String ARRAY_CREATE_EXPRESSION = "CREATE_ARR";
	public static final String ARRAY_ACCESS_EXPRESSION = "ARR_ACC";
	public static final String TYPE_UNKNOWN = "T_UNKNOWN";
	public static final String PACKAGE_DECLARATION = "P_DEC";
	public static final String IMPORT_DECLARATION = "IMP_DEC";
	public static final String FIELD_DECLARATION = "FIELD_DEC";
	public static final String CLASS_TYPE = "CLASS_TYPE";
	public static final String CLASS_DECLARATION = "CLASS_DEC";
	public static final String METHOD_DECLARATION = "M_DEC";
	public static final String BINARY_EXPRESSION = "BIN_EXPR";
	public static final String UNARY_EXPRESSION = "UNARY_EXPR";
	public static final String METHOD_CALL_EXPRESSION = "M_CALL";
	// if a private method is called we handle it differently
	public static final String PRIVATE_METHOD_CALL_EXPRESSION = "M_CALL_PRIV";
	public static final String NAME_EXPRESSION = "NAME_EXPR";
	public static final String INTEGER_LITERAL_EXPRESSION = "INT_LIT";
	public static final String DOUBLE_LITERAL_EXPRESSION = "DOUBLE_LIT";
	public static final String STRING_LITERAL_EXPRESSION = "STR_LIT";
	public static final String BOOLEAN_LITERAL_EXPRESSION = "BOOL_LIT";
	public static final String CHAR_LITERAL_EXPRESSION = "CHAR_LIT";
	public static final String LONG_LITERAL_EXPRESSION = "LONG_LIT";
	public static final String THIS_EXPRESSION = "THIS";
	public static final String BREAK = "BREAK";
	public static final String OBJ_CREATE_EXPRESSION = "CREATE_OBJ";
	public static final String PARAMETER = "PAR"; 
	public static final String ENCLOSED_EXPRESSION = "ENCLOSED";
	public static final String ASSERT_STMT = "ASSERT";
	
	public static final String TYPE_DECLARATION_STATEMENT = "TYPE_DEC";
	public static final String TYPE_REFERENCE = "REF_TYPE";
	public static final String TYPE_PRIMITIVE = "PRIM_TYPE";
	public static final String TYPE_UNION = "UNION_TYPE";
	public static final String TYPE_INTERSECTION = "INTERSECT_TYPE";
	public static final String TYPE_PAR = "TYPE_PAR"; 
	public static final String TYPE_WILDCARD = "WILDCARD_TYPE";
	public static final String TYPE_VOID = "VOID_TYPE";
	
	// closing tags for some special nodes
	public static final String END_SUFFIX = "_END";
	public static final String CLOSING_MDEC = METHOD_DECLARATION + END_SUFFIX;
	public static final String CLOSING_CNSTR = CONSTRUCTOR_DECLARATION + END_SUFFIX;
	public static final String CLOSING_IF = IF_STATEMENT + END_SUFFIX;
	public static final String CLOSING_WHILE = WHILE_STATEMENT + END_SUFFIX;
	public static final String CLOSING_FOR = FOR_STATEMENT + END_SUFFIX;
	public static final String CLOSING_TRY = TRY_STATEMENT + END_SUFFIX;
	public static final String CLOSING_CATCH = CATCH_CLAUSE_STATEMENT + END_SUFFIX;
	public static final String CLOSING_FOR_EACH = FOR_EACH_STATEMENT + END_SUFFIX;
	public static final String CLOSING_DO = DO_STATEMENT + END_SUFFIX;
	
	/**
	 * Returns the mapping of the abstract syntax tree node to fit the language model
	 * 
	 * @param aNode The node that should be mapped
	 * @return the string representation
	 */
	public MappingWrapper<T> getMappingForNode( Node aNode );
	
	/**
	 * Returns a closing token for some block nodes
	 * 
	 * @param aNode
	 * an AST node for which the closing token shall be generated
	 * @return Closing token or null if the node has none
	 */
	public T getClosingToken(Node aNode);
	
	/**
	 * Passes a black list of method names to the mapper.
	 * @param aBL A collection of method names that should be handled differently
	 */
	public void setPrivMethodBlackList( Collection<String> aBL );
	
	/**
	 * Clears the black list of method names from this mapper
	 */
	public void clearPrivMethodBlackList();
	
}
