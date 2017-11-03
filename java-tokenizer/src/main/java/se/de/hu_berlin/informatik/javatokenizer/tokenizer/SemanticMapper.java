package se.de.hu_berlin.informatik.javatokenizer.tokenizer;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapperWithMetaData;

public class SemanticMapper {

	IBasicNodeMapper<String> mapper;
	
	public SemanticMapper(boolean long_tokens) {
		mapper = new Node2AbstractionMapperWithMetaData
				.Builder(long_tokens ? new KeyWordConstants() : new KeyWordConstantsShort())
				.setchildCountStepWidth(10)
				.setMaxListMembers(5)
				.usesStringAbstraction()
				.usesVariableNameAbstraction()
				.usesPrivateMethodAbstraction()
				.usesClassNameAbstraction()
				.usesPackageAndImportAbstraction()
				.usesAnnotationAbstraction()
//				.usesMethodNameAbstraction()
				.usesGenericTypeNameAbstraction()
				.usesCommentAbstraction()
				.build();
	}
	
	public IBasicNodeMapper<String> getMapper() {
		return mapper;
	}
	
}
