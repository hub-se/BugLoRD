package se.de.hu_berlin.informatik.javatokenizer.tokenizer;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapperWithMetaData;

public class SemanticMapper {

	IBasicNodeMapper<String> mapper;
	
	public SemanticMapper(boolean long_tokens, int childCountStepWidth) {
		mapper = new Node2AbstractionMapperWithMetaData
				.Builder(long_tokens ? new KeyWordConstants() : new KeyWordConstantsShort())
				.setchildCountStepWidth(childCountStepWidth)
				.setMaxListMembers(3)
				.usesStringAbstraction()
				.usesVariableNameAbstraction()
				.usesPrivateMethodAbstraction()
				.usesClassNameAbstraction()
				.usesPackageAndImportAbstraction()
				.usesAnnotationAbstraction()
				// remove/show public method names?
//				.usesMethodNameAbstraction()
				.usesGenericTypeNameAbstraction()
				.usesCommentAbstraction()
//				.ignoresWrappers()
				.build();
	}
	
	public IBasicNodeMapper<String> getMapper() {
		return mapper;
	}
	
}
