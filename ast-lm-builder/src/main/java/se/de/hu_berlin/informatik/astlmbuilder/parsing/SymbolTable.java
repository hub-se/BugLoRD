package se.de.hu_berlin.informatik.astlmbuilder.parsing;

import java.util.ArrayList;
import java.util.List;

import se.de.hu_berlin.informatik.astlmbuilder.parsing.VariableInfoWrapper.VariableScope;

/**
 * Stores data about variables for a specific node
 */
public class SymbolTable {

	List<VariableInfoWrapper> symbolTable = null;

	public SymbolTable( List<VariableInfoWrapper> aSimpleSymbolTable ) {
		symbolTable = aSimpleSymbolTable;
	}
	
	public List<VariableInfoWrapper> getSymbolTable() {
		return symbolTable;
	}

	
	public void setSymbolTable(List<VariableInfoWrapper> symbolTable) {
		this.symbolTable = symbolTable;
	}
	
	/**
	 * @return all variable info wrappers in this symbol table that have a global scope
	 */
	public List<VariableInfoWrapper> getAllGlobalVarInfoWrapper() {
		List<VariableInfoWrapper> result = new ArrayList<>();
		
		for( VariableInfoWrapper viw : symbolTable ) {
			if( viw.getScope() == VariableScope.GLOBAL ) {
				result.add( viw );
			}
		}
		
		return result;
	}
	
	/**
	 * @return all variable info wrappers in this symbol table that are arguments
	 */
	public List<VariableInfoWrapper> getAllParameterVarInfoWrapper() {
		List<VariableInfoWrapper> result = new ArrayList<>();
		
		for( VariableInfoWrapper viw : symbolTable ) {
			if( viw.getScope() == VariableScope.PARAMETER ) {
				result.add( viw );
			}
		}
		
		return result;
	}
	
	/**
	 * @return all variable info wrappers in this symbol table that have a local scope
	 */
	public List<VariableInfoWrapper> getAllLocalVarInfoWrapper() {
		List<VariableInfoWrapper> result = new ArrayList<>();
		
		for( VariableInfoWrapper viw : symbolTable ) {
			if( viw.getScope() == VariableScope.LOCAL ) {
				result.add( viw );
			}
		}
		
		return result;
	}
	
	/**
	 * Searches for all variable info objects that have a specific type
	 * @param aType The type of the variable
	 * @return All global, local and argument variables of the given type
	 */
	public List<VariableInfoWrapper> getAllVarInfoWrapperWithType( String aType ) {
		if ( aType == null || aType.trim().length() == 0 ) {
			return null;
		}
		
		List<VariableInfoWrapper> result = new ArrayList<>();
		String type = aType.trim().toLowerCase();
		
		for ( VariableInfoWrapper viw : symbolTable ) {
			if ( viw.getType().equals( type ) ) {
				result.add( viw );
			}
		}
		
		return result;
	}
	
	/**
	 * Searches for all variable info objects that have a specific name
	 * @param aName The name of the variable
	 * @return All global, local and argument variables with the given name
	 */
	public List<VariableInfoWrapper> getAllVarInfoWrapperWithName( String aName ) {
		if ( aName == null || aName.trim().length() == 0 ) {
			return null;
		}
		
		List<VariableInfoWrapper> result = new ArrayList<>();
		String name = aName.trim().toLowerCase();
		
		for ( VariableInfoWrapper viw : symbolTable ) {
			if ( viw.getName().equals( name ) ) {
				result.add( viw );
			}
		}
		
		return result;
	}
	
}
