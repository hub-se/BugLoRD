package se.de.hu_berlin.informatik.astlmbuilder.mapping;

public interface ITokenMapperShort<T,V> extends ITokenMapper<T, V> {
	
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
	
	
	public static final String WHILE_STATEMENT = KEYWORD_MARKER + "A";
	public static final String TRY_STATEMENT = KEYWORD_MARKER + "B";
	public static final String THROW_STATEMENT = KEYWORD_MARKER + "C";
	public static final String THROWS_STATEMENT = KEYWORD_MARKER + "D";
	public static final String SYNCHRONIZED_STATEMENT = KEYWORD_MARKER + "E";
	public static final String SWITCH_STATEMENT = KEYWORD_MARKER + "F";
	public static final String SWITCH_ENTRY_STATEMENT = KEYWORD_MARKER + "G";
	public static final String RETURN_STATEMENT = KEYWORD_MARKER + "H";
	public static final String LABELED_STATEMENT = KEYWORD_MARKER + "I";
	public static final String IF_STATEMENT = KEYWORD_MARKER + "J";
	public static final String ELSE_STATEMENT = KEYWORD_MARKER + "K";
	public static final String FOR_STATEMENT = KEYWORD_MARKER + "L";
	public static final String FOR_EACH_STATEMENT = KEYWORD_MARKER + "M";
	public static final String EXPRESSION_STATEMENT = KEYWORD_MARKER + "N";
	public static final String EXPLICIT_CONSTRUCTOR_STATEMENT = KEYWORD_MARKER + "O";
	public static final String EMPTY_STATEMENT = KEYWORD_MARKER + "P";
	public static final String DO_STATEMENT = KEYWORD_MARKER + "Q";
	public static final String CONTINUE_STATEMENT = KEYWORD_MARKER + "R";
	public static final String CATCH_CLAUSE_STATEMENT = KEYWORD_MARKER + "S";
	public static final String BLOCK_STATEMENT = KEYWORD_MARKER + "T";
	public static final String VARIABLE_DECLARATION_ID = KEYWORD_MARKER + "U";
	public static final String VARIABLE_DECLARATION_EXPRESSION = KEYWORD_MARKER + "V";
	public static final String TYPE_EXPRESSION = KEYWORD_MARKER + "W";
	public static final String SUPER_EXPRESSION = KEYWORD_MARKER + "X";
	public static final String QUALIFIED_NAME_EXPRESSION = KEYWORD_MARKER + "Y";
	public static final String NULL_LITERAL_EXPRESSION = KEYWORD_MARKER + "Z";
	public static final String METHOD_REFERENCE_EXPRESSION = KEYWORD_MARKER + "a";
	public static final String BODY_STMT = KEYWORD_MARKER + "b";
	public static final String THIS_EXPRESSION = KEYWORD_MARKER + "c";
	public static final String LAMBDA_EXPRESSION = KEYWORD_MARKER + "d";
	public static final String BREAK = KEYWORD_MARKER + "e";
	public static final String INSTANCEOF_EXPRESSION = KEYWORD_MARKER + "f";
	public static final String FIELD_ACCESS_EXPRESSION = KEYWORD_MARKER + "g";
	public static final String CONDITIONAL_EXPRESSION = KEYWORD_MARKER + "h";
	public static final String CLASS_EXPRESSION = KEYWORD_MARKER + "i";
	public static final String CAST_EXPRESSION = KEYWORD_MARKER + "j";
	public static final String ASSIGN_EXPRESSION = KEYWORD_MARKER + "k";
	public static final String ARRAY_INIT_EXPRESSION = KEYWORD_MARKER + "l";
	public static final String ARRAY_CREATE_EXPRESSION = KEYWORD_MARKER + "m";
	public static final String ARRAY_ACCESS_EXPRESSION = KEYWORD_MARKER + "n";
	public static final String CLASS_OR_INTERFACE_TYPE = KEYWORD_MARKER + "o";
	public static final String EXTENDS_STATEMENT = KEYWORD_MARKER + "p";
	public static final String IMPLEMENTS_STATEMENT = KEYWORD_MARKER + "q";
	public static final String METHOD_DECLARATION = KEYWORD_MARKER + "r";
	public static final String BINARY_EXPRESSION = KEYWORD_MARKER + "s";
	public static final String UNARY_EXPRESSION = KEYWORD_MARKER + "t";
	public static final String METHOD_CALL_EXPRESSION = KEYWORD_MARKER + "u";
	// if a private method is called we handle it differently
	public static final String PRIVATE_METHOD_CALL_EXPRESSION = KEYWORD_MARKER + "v";
	public static final String NAME_EXPRESSION = KEYWORD_MARKER + "w";
	public static final String OBJ_CREATE_EXPRESSION = KEYWORD_MARKER + "x";
	public static final String PARAMETER = KEYWORD_MARKER + "y";
	public static final String ENCLOSED_EXPRESSION = KEYWORD_MARKER + "z";
	
	public static final String INTEGER_LITERAL_EXPRESSION = KEYWORD_MARKER + "0";
	public static final String DOUBLE_LITERAL_EXPRESSION = KEYWORD_MARKER + "1";
	public static final String STRING_LITERAL_EXPRESSION = KEYWORD_MARKER + "2";
	public static final String BOOLEAN_LITERAL_EXPRESSION = KEYWORD_MARKER + "3";
	public static final String CHAR_LITERAL_EXPRESSION = KEYWORD_MARKER + "4";
	public static final String LONG_LITERAL_EXPRESSION = KEYWORD_MARKER + "5";
	
	public static final String INTEGER_LITERAL_MIN_VALUE_EXPRESSION = KEYWORD_MARKER + "0A";
	public static final String LONG_LITERAL_MIN_VALUE_EXPRESSION = KEYWORD_MARKER + "5A";
	
	public static final String TYPE_REFERENCE = KEYWORD_MARKER + "6";
	public static final String TYPE_PAR = KEYWORD_MARKER + "7"; 
	public static final String TYPE_VOID = KEYWORD_MARKER + "8";
	public static final String TYPE_PRIMITIVE = KEYWORD_MARKER + "9";
	
	public static final String TYPE_PARAMETERS_START = KEYWORD_MARKER + "TP";
	
	public static final String COMPILATION_UNIT = KEYWORD_MARKER + "CA";
	public static final String LINE_COMMENT = KEYWORD_MARKER + "CB";
	public static final String BLOCK_COMMENT = KEYWORD_MARKER + "CD";
	public static final String JAVADOC_COMMENT = KEYWORD_MARKER + "CE";

	public static final String MARKER_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "QA";
	public static final String NORMAL_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "QB";
	public static final String SINGLE_MEMBER_ANNOTATION_EXPRESSION = KEYWORD_MARKER + "QC";
	
	public static final String MULTI_TYPE_PARAMETER = KEYWORD_MARKER + "BA"; 
	public static final String ASSERT_STMT = KEYWORD_MARKER + "BB";
	public static final String MEMBER_VALUE_PAIR = KEYWORD_MARKER + "BC"; 
	public static final String TYPE_DECLARATION_STATEMENT = KEYWORD_MARKER + "BD";
	public static final String TYPE_UNION = KEYWORD_MARKER + "BE";
	public static final String TYPE_INTERSECTION = KEYWORD_MARKER + "BF";
	public static final String TYPE_WILDCARD = KEYWORD_MARKER + "BG";

	public static final String TYPE_UNKNOWN = KEYWORD_MARKER + "BU";
	

	public static final String CONSTRUCTOR_DECLARATION = KEYWORD_MARKER + "AA";
	public static final String INITIALIZER_DECLARATION = KEYWORD_MARKER + "AB";
	public static final String ENUM_CONSTANT_DECLARATION = KEYWORD_MARKER + "AC";
	public static final String VARIABLE_DECLARATION = KEYWORD_MARKER + "AD";
	public static final String ENUM_DECLARATION = KEYWORD_MARKER + "AE";
	public static final String ANNOTATION_DECLARATION = KEYWORD_MARKER + "AF";
	public static final String ANNOTATION_MEMBER_DECLARATION = KEYWORD_MARKER + "AG";
	public static final String EMPTY_MEMBER_DECLARATION = KEYWORD_MARKER + "AH";
	public static final String EMPTY_TYPE_DECLARATION = KEYWORD_MARKER + "AI";
	public static final String PACKAGE_DECLARATION = KEYWORD_MARKER + "AJ";
	public static final String IMPORT_DECLARATION = KEYWORD_MARKER + "AK";
	public static final String FIELD_DECLARATION = KEYWORD_MARKER + "AL";
	public static final String CLASS_OR_INTERFACE_DECLARATION = KEYWORD_MARKER + "AM";
	public static final String CLASS_DECLARATION = KEYWORD_MARKER + "AN";
	public static final String INTERFACE_DECLARATION = KEYWORD_MARKER + "AO";
	
	public static final String UNKNOWN = KEYWORD_MARKER + "UU";
	
	// closing tags for some special nodes
	public static final String END_SUFFIX = "_";
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
	
	/**
	 * Clears the black list of method names from this mapper
	 */
	public void clearPrivMethodBlackList();
	
}
