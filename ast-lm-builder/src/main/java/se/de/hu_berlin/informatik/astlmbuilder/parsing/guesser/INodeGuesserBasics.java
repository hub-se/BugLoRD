package se.de.hu_berlin.informatik.astlmbuilder.parsing.guesser;

import java.util.EnumSet;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.InformationWrapper;
import se.de.hu_berlin.informatik.astlmbuilder.parsing.dispatcher.IKeyWordDispatcher;

/**
 * Interface that provides functionality to guess AST nodes only from available information.
 */
public interface INodeGuesserBasics {
	
	public IKeyWordDispatcher getDispatcher();
	
	public IKeyWordProvider<String> getKeyWordProvider();

	//TODO: implement these...
	//"guess" nodes and node lists based only on keywords and available information

	/**
	 * Tries to "guess" a node of the expected type based only on the
	 * available information.
	 * @param expectedSuperClazz
	 * the expected type of the node
	 * @param info
	 * the currently available information
	 * @return
	 * a node of the expected type
	 * @param <T>
	 * the type of node returned
	 */
	public <T extends Node> T guessNode(Class<T> expectedSuperClazz, InformationWrapper info);

	/**
	 * Tries to "guess" a method identifier based on the given information.
	 * @param info
	 * the currently available information
	 * @return
	 * a method identifier
	 */
	public String guessMethodIdentifier(InformationWrapper info);

	/**
	 * Tries to "guess" a node list of the expected type based only on the
	 * available information and the number of elements in the original list.
	 * @param expectedSuperClazz
	 * the expected type of the nodes in the list
	 * @param listMemberCount
	 * the number of list elements in the original list
	 * @param info
	 * the currently available information
	 * @return
	 * a list of nodes of the expected type
	 * @param <T>
	 * the type of nodes in the list
	 */
	public <T extends Node> NodeList<T> guessList(Class<T> expectedSuperClazz, int listMemberCount, InformationWrapper info);
	
	/**
	 * Tries to "guess" a node list of the expected type based only on the
	 * available information.
	 * @param expectedSuperClazz
	 * the expected type of the nodes in the list
	 * @param info
	 * the currently available information
	 * @return
	 * a list of nodes of the expected type
	 * @param <T>
	 * the type of nodes in the list
	 */
	public <T extends Node> NodeList<T> guessList(Class<T> expectedSuperClazz, InformationWrapper info);

	/**
	 * Tries to "guess" a BodyDeclaration node list based only on the
	 * available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a list of BodyDeclaration nodes
	 */
	public NodeList<BodyDeclaration<?>> guessBodyDeclarationList(InformationWrapper info);

	/**
	 * Tries to "guess" a TypeDeclaration node list based only on the
	 * available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a list of TypeDeclaration nodes
	 */
	public NodeList<TypeDeclaration<?>> guessTypeDeclarationList(InformationWrapper info);

	/**
	 * Tries to "guess" a modifier set based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a set of modifiers
	 */
	public EnumSet<Modifier> guessModifiers(InformationWrapper info);
	
	/**
	 * Tries to "guess" a boolean value based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a boolean value
	 */
	public boolean guessBoolean(InformationWrapper info);
	
	/**
	 * Tries to "guess" a String value based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a String value
	 */
	public String guessStringValue(InformationWrapper info);
	
	/**
	 * Tries to "guess" a primitive type based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a primitive type
	 */
	public Primitive guessPrimitive(InformationWrapper info);
	
	/**
	 * Tries to "guess" an assign operator based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * an assign operator
	 */
	public AssignExpr.Operator guessAssignOperator(InformationWrapper info);
	
	/**
	 * Tries to "guess" a unary operator based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a unary operator
	 */
	public UnaryExpr.Operator guessUnaryOperator(InformationWrapper info);

	/**
	 * Tries to "guess" a binary operator based only on the available information.
	 * @param info
	 * the currently available information
	 * @return
	 * a binary operator
	 */
	public BinaryExpr.Operator guessBinaryOperator(InformationWrapper info);
	
}
