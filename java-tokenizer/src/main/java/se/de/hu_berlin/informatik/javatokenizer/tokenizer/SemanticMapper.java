package se.de.hu_berlin.informatik.javatokenizer.tokenizer;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstants;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.KeyWordConstantsShort;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.IBasicNodeMapper;
import se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper.Node2AbstractionMapper;

public class SemanticMapper {

	IBasicNodeMapper<String> mapper;
	
	public SemanticMapper(boolean long_tokens) {
		mapper = new Node2AbstractionMapper.Builder(long_tokens ? new KeyWordConstants() : new KeyWordConstantsShort())
				.setMaxListMembers(20)
				.usesStringAbstraction()
				.usesVariableNameAbstraction()
				.usesPrivateMethodAbstraction()
				.usesClassNameAbstraction()
//				.usesMethodNameAbstraction()
				.usesGenericTypeNameAbstraction()
				.build();
	}
	
	public IBasicNodeMapper<String> getMapper() {
		return mapper;
	}
	
}
