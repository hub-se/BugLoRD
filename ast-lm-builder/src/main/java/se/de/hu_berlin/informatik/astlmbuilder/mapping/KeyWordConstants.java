package se.de.hu_berlin.informatik.astlmbuilder.mapping;

public class KeyWordConstants {
	
	public static final String SPLIT = ",";
	public static final String ID_MARKER = ";";
	public static final String GROUP_START = "[";
	public static final String GROUP_END = "]";
	public static final String BIG_GROUP_START = "(";
	public static final String BIG_GROUP_END = ")";
	public static final String TYPEARG_START = "<";
	public static final String TYPEARG_END = ">";
	
	public static final String KEYWORD_MARKER = "$";
	public static final String KEYWORD_SERIALIZE = "%"; // marks the beginning of the serialization
	
	// some values are needed as chars for performance
	public static final char C_SPLIT = ',';
	public static final char C_ID_MARKER = ';';
	public static final char C_GROUP_START = '[';
	public static final char C_GROUP_END = ']';
	public static final char C_BIG_GROUP_START = '(';
	public static final char C_BIG_GROUP_END = ')';
	public static final char C_TYPEARG_START = '<';
	public static final char C_TYPEARG_END = '>';
	
	public static final char C_KEYWORD_MARKER = '$';
	public static final char C_KEYWORD_SERIALIZE = '%'; // marks the beginning of the serialization
	
	public static final String TYPE_PARAMETERS_START = KEYWORD_MARKER + "TYPE_PARS";
	
	public static final String COMPILATION_UNIT = KEYWORD_MARKER + "COMP_UNIT";
	public static final String LINE_COMMENT = KEYWORD_MARKER + "LINE_COMMENT";
	public static final String BLOCK_COMMENT = KEYWORD_MARKER + "BLOCK_COMMENT";
	public static final String JAVADOC_COMMENT = KEYWORD_MARKER + "JAVADOC_COMMENT";
	
	public static final String CONSTRUCTOR_DECLARATION = KEYWORD_MARKER + "CNSTR_DEC";
	public static final String INITIALIZER_DECLARATION = KEYWORD_MARKER + "INIT_DEC";
	public static final String ENUM_CONSTANT_DECLARATION = KEYWORD_MARKER + "ENUM_CONST_DEC";
	public static final String VARIABLE_DECLARATION = KEYWORD_MARKER + "VAR_DEC";
	public static final String ENUM_DECLARATION = KEYWORD_MARKER + "ENUM_DEC";
	public static final String ANNOTATION_DECLARATION = KEYWORD_MARKER + "ANN_DEC";
	public static final String ANNOTATION_MEMBER_DECLARATION = KEYWORD_MARKER + "ANN_MEMBER_DEC";
	public static final String EMPTY_MEMBER_DECLARATION = KEYWORD_MARKER + "EMPTY_MEMBER_DEC";
	public static final String EMPTY_TYPE_DECLARATION = KEYWORD_MARKER + "EMPTY_TYPE_DEC";
	public static final String WHILE_STATEMENT = KEYWORD_MARKER + "WHILE";
	public static final String TRY_STATEMENT = KEYWORD_MARKER + "TRY";
	public static final String THROW_STATEMENT = KEYWORD_MARKER + "THROW";
	public static final String THROWS_STATEMENT = KEYWORD_MARKER + "THROWS";
	public static final String SYNCHRONIZED_STATEMENT = KEYWORD_MARKER + "SYNC";
	public static final String SWITCH_STATEMENT = KEYWORD_MARKER + "SWITCH";
	public static final String SWITCH_ENTRY_STATEMENT = KEYWORD_MARKER + "SWITCH_ENTRY";
	public static final String RETURN_STATEMENT = KEYWORD_MARKER + "RETURN";
	public static final String LABELED_STATEMENT = KEYWORD_MARKER + "LABELED";
	public static final String IF_STATEMENT = KEYWORD_MARKER + "IF";
	public static final String ELSE_STATEMENT = KEYWORD_MARKER + "ELSE";
	public static final String FOR_STATEMENT = KEYWORD_MARKER + "FOR";
	public static final String FOR_EACH_STATEMENT = KEYWORD_MARKER + "FOR_EACH";
	public static final String EXPRESSION_STATEMENT = KEYWORD_MARKER + "EXPR_STMT";
	public static final String EXPLICIT_CONSTRUCTOR_STATEMENT = KEYWORD_MARKER + "EXPL_CONSTR";
	public static final String EMPTY_STATEMENT = KEYWORD_MARKER + "EMPTY";
	public static final String DO_STATEMENT = KEYWORD_MARKER + "DO";
	public static final String CONTINUE_STATEMENT = KEYWORD_MARKER + "CONTINUE";
	public static final String CATCH_CLAUSE_STATEMENT = KEYWORD_MARKER + "CATCH";
	public static final String BLOCK_STATEMENT = KEYWORD_MARKER + "BLOCK";
	public static final String VARIABLE_DECLARATION_ID = KEYWORD_MARKER + "VAR_DEC_ID";
	public static final String VARIABLE_DECLARATION_EXPRESSION = KEYWORD_MARKER + "VAR_DEC_EXPR";
	public static final String TYPE_EXPRESSION = KEYWORD_MARKER + "TYPE_EXPR";
	public static final String SUPER_EXPRESSION = KEYWORD_MARKER + "SUPER";
	public static final String QUALIFIED_NAME_EXPRESSION = KEYWORD_MARKER + "QUALIFIED_NAME";
	public static final String NULL_LITERAL_EXPRESSION = KEYWORD_MARKER + "NULL_LIT";
	public static final String METHOD_REFERENCE_EXPRESSION = KEYWORD_MARKER + "MT_REF";
	public static final String BODY_STMT = KEYWORD_MARKER + "BODY";
	public static final String LONG_LITERAL_MIN_VALUE_EXPRESSION = KEYWORD_MARKER + "LONG_LIT_MIN";
	public static final String LAMBDA_EXPRESSION = KEYWORD_MARKER + "LAMBDA";
	public static final String INTEGER_LITERAL_MIN_VALUE_EXPRESSION = KEYWORD_MARKER + "INT_LIT_MIN";
	public static final String INSTANCEOF_EXPRESSION = KEYWORD_MARKER + "INSTANCEOF";
	public static final String FIELD_ACCESS_EXPRESSION = KEYWORD_MARKER + "FIELD_ACC";
	public static final String CONDITIONAL_EXPRESSION = KEYWORD_MARKER + "CONDITION";
	public static final String CLASS_EXPRESSION = KEYWORD_MARKER + "CLASS";
	public static final String CAST_EXPRESSION = KEYWORD_MARKER + "CAST";
	public static final String ASSIGN_EXPRESSION = KEYWORD_MARKER + "ASSIGN";
	public static final String ARRAY_INIT_EXPRESSION = KEYWORD_MARKER + "INIT_ARR";
	public static final String ARRAY_CREATE_EXPRESSION = KEYWORD_MARKER + "CREATE_ARR";
	public static final String ARRAY_ACCESS_EXPRESSION = KEYWORD_MARKER + "ARR_ACC";
	public static final String PACKAGE_DECLARATION = KEYWORD_MARKER + "P_DEC";
	public static final String IMPORT_DECLARATION = KEYWORD_MARKER + "IMP_DEC";
	public static final String FIELD_DECLARATION = KEYWORD_MARKER + "FIELD_DEC";
	public static final String CLASS_OR_INTERFACE_TYPE = KEYWORD_MARKER + "CI_TYPE";
	public static final String CLASS_OR_INTERFACE_DECLARATION = KEYWORD_MARKER + "CI_DEC";
	public static final String CLASS_DECLARATION = KEYWORD_MARKER + "CLS_DEC";
	public static final String INTERFACE_DECLARATION = KEYWORD_MARKER + "INTF_DEC";
	public static final String EXTENDS_STATEMENT = KEYWORD_MARKER + "EXTENDS";
	public static final String IMPLEMENTS_STATEMENT = KEYWORD_MARKER + "IMPLEMENTS";
	public static final String METHOD_DECLARATION = KEYWORD_MARKER + "MT_DEC";
	public static final String BINARY_EXPRESSION = KEYWORD_MARKER + "BIN_EXPR";
	public static final String UNARY_EXPRESSION = KEYWORD_MARKER + "UNARY_EXPR";
	public static final String METHOD_CALL_EXPRESSION = KEYWORD_MARKER + "MT_CALL";
	// if a private method is called we handle it differently
	public static final String PRIVATE_METHOD_CALL_EXPRESSION = KEYWORD_MARKER + "MT_CALL_PRIV";
	public static final String NAME_EXPRESSION = KEYWORD_MARKER + "NAME_EXPR";
	public static final String INTEGER_LITERAL_EXPRESSION = KEYWORD_MARKER + "INT_LIT";
	public static final String DOUBLE_LITERAL_EXPRESSION = KEYWORD_MARKER + "DOUBLE_LIT";
	public static final String STRING_LITERAL_EXPRESSION = KEYWORD_MARKER + "STR_LIT";
	public static final String BOOLEAN_LITERAL_EXPRESSION = KEYWORD_MARKER + "BOOL_LIT";
	public static final String CHAR_LITERAL_EXPRESSION = KEYWORD_MARKER + "CHAR_LIT";
	public static final String LONG_LITERAL_EXPRESSION = KEYWORD_MARKER + "LONG_LIT";
	public static final String THIS_EXPRESSION = KEYWORD_MARKER + "THIS";
	public static final String BREAK = KEYWORD_MARKER + "BREAK";
	public static final String OBJ_CREATE_EXPRESSION = KEYWORD_MARKER + "NEW_OBJ";
	public static final String MARKER_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "MARKER_ANN_EXPR";
	public static final String NORMAL_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "NORMAL_ANN_EXPR";
	public static final String SINGLE_MEMBER_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "SM_ANN_EXPR";
	
	public static final String PARAMETER = KEYWORD_MARKER + "PAR";
	public static final String MULTI_TYPE_PARAMETER = KEYWORD_MARKER + "MTYPE_PAR"; 
	public static final String ENCLOSED_EXPRESSION = KEYWORD_MARKER + "ENCLOSED";
	public static final String ASSERT_STMT = KEYWORD_MARKER + "ASSERT";
	public static final String MEMBER_VALUE_PAIR = KEYWORD_MARKER + "MV_PAIR"; 
	
	public static final String TYPE_DECLARATION_STATEMENT = KEYWORD_MARKER + "TYPE_DEC";
	public static final String TYPE_REFERENCE = KEYWORD_MARKER + "REF_TYPE";
	public static final String TYPE_PRIMITIVE = KEYWORD_MARKER + "PRIM_TYPE";
	public static final String TYPE_UNION = KEYWORD_MARKER + "UNION_TYPE";
	public static final String TYPE_INTERSECTION = KEYWORD_MARKER + "INTERSECT_TYPE";
	public static final String TYPE_PAR = KEYWORD_MARKER + "TYPE_PAR"; 
	public static final String TYPE_WILDCARD = KEYWORD_MARKER + "WILDCARD_TYPE";
	public static final String TYPE_VOID = KEYWORD_MARKER + "VOID_TYPE";
	public static final String TYPE_UNKNOWN = KEYWORD_MARKER + "UNKNOWN_TYPE";
	
	public static final String UNKNOWN = KEYWORD_MARKER + "T_UNKNOWN";
	
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
	public static final String CLOSING_SWITCH = SWITCH_STATEMENT + END_SUFFIX;
	public static final String CLOSING_ENCLOSED = ENCLOSED_EXPRESSION + END_SUFFIX;
	public static final String CLOSING_BLOCK_STMT = BLOCK_STATEMENT + END_SUFFIX;
	public static final String CLOSING_EXPRESSION_STMT = EXPRESSION_STATEMENT + END_SUFFIX;
	public static final String CLOSING_COMPILATION_UNIT = COMPILATION_UNIT + END_SUFFIX;
	
}	
