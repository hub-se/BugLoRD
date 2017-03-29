package se.de.hu_berlin.informatik.astlmbuilder.mapping.mapper;

import se.de.hu_berlin.informatik.astlmbuilder.mapping.keywords.IKeyWordProvider;
import se.de.hu_berlin.informatik.utils.miscellaneous.IBuilder;

/**
 * Maps nodes to sequences of tokens that are either the abstract identifiers themselves, 
 * or they wrap the identifiers and various information of the respecting nodes in the following 
 * manner:
 * 
 * <p> General format for elements with 
 * <br> maximum abstraction: {@code $node_id}, and
 * <br> other abstraction level: {@code ($node_id,[member_1],[member_2],...,[member_n])},
 * <br> where each {@code member_k} is again an element itself.
 * 
 * @author Simon
 */
public class Node2AbstractionMapper extends SimpleMapper<String> implements IAbstractionMapper {

	final private int maxListMembers;

	final private boolean stringAndCharAbstraction;
	final private boolean numberAbstraction;
	final private boolean localMethodAbstraction;
	final private boolean methodNameAbstraction;
	final private boolean variableNameAbstraction;
	final private boolean genericTypeNameAbstraction;
	final private boolean classNameAbstraction;
	final private boolean packageAndImportAbstraction;
	final private boolean annotationAbstraction;

	private Node2AbstractionMapper(Builder builder) {
		super(builder.provider);
		this.maxListMembers = builder.maxListMembers;
		this.stringAndCharAbstraction = builder.stringAndCharAbstraction;
		this.numberAbstraction = builder.numberAbstraction;
		this.localMethodAbstraction = builder.localMethodAbstraction;
		this.methodNameAbstraction = builder.methodNameAbstraction;
		this.variableNameAbstraction = builder.variableNameAbstraction;
		this.genericTypeNameAbstraction = builder.genericTypeNameAbstraction;
		this.classNameAbstraction = builder.classNameAbstraction;
		this.packageAndImportAbstraction = builder.packageAndImportAbstraction;
		this.annotationAbstraction = builder.annotationAbstraction;
	}

	@Override
	public int getMaxListMembers() {
		return maxListMembers;
	}

	@Override
	public boolean usesStringAndCharAbstraction() {
		return stringAndCharAbstraction;
	}

	@Override
	public boolean usesNumberAbstraction() {
		return numberAbstraction;
	}

	@Override
	public boolean usesLocalMethodAbstraction() {
		return localMethodAbstraction;
	}

	@Override
	public boolean usesMethodNameAbstraction() {
		return methodNameAbstraction;
	}

	@Override
	public boolean usesVariableNameAbstraction() {
		return variableNameAbstraction;
	}

	@Override
	public boolean usesGenericTypeNameAbstraction() {
		return genericTypeNameAbstraction;
	}

	@Override
	public boolean usesClassNameAbstraction() {
		return classNameAbstraction;
	}

	@Override
	public boolean usesPackageAndImportAbstraction() {
		return packageAndImportAbstraction;
	}

	@Override
	public boolean usesAnnotationAbstraction() {
		return annotationAbstraction;
	}

	public static class Builder implements IBuilder<Node2AbstractionMapper> {

		private int maxListMembers = -1;
		private IKeyWordProvider<String> provider = null;
		private boolean stringAndCharAbstraction = false;
		private boolean numberAbstraction = false;
		private boolean localMethodAbstraction = false;
		private boolean methodNameAbstraction = false;
		private boolean variableNameAbstraction = false;
		private boolean genericTypeNameAbstraction = false;
		private boolean classNameAbstraction = false;
		private boolean packageAndImportAbstraction = false;
		private boolean annotationAbstraction = false;

		/**
		 * Creates an {@link Builder} object with the given parameters.
		 * @param provider
		 * a keyword provider
		 */
		public Builder(IKeyWordProvider<String> provider) {
			super();
			this.provider = provider;
		}
		
		public Builder setMaxListMembers(int maxListMembers) {
			this.maxListMembers = maxListMembers;
			return this;
		}
		
		public Builder usesStringAndCharAbstraction() {
			this.stringAndCharAbstraction = true;
			return this;
		}

		public Builder usesNumberAbstraction() {
			this.numberAbstraction = true;
			return this;
		}

		public Builder usesLocalMethodAbstraction() {
			this.localMethodAbstraction = true;
			return this;
		}

		public Builder usesMethodNameAbstraction() {
			this.methodNameAbstraction = true;
			return this;
		}

		public Builder usesVariableNameAbstraction() {
			this.variableNameAbstraction = true;
			return this;
		}

		public Builder usesGenericTypeNameAbstraction() {
			this.genericTypeNameAbstraction = true;
			return this;
		}

		public Builder usesClassNameAbstraction() {
			this.classNameAbstraction = true;
			return this;
		}

		public Builder usesPackageAndImportAbstraction() {
			this.packageAndImportAbstraction = true;
			return this;
		}

		public Builder usesAnnotationAbstraction() {
			this.annotationAbstraction = true;
			return this;
		}

		@Override
		public Node2AbstractionMapper build() {
			return new Node2AbstractionMapper(this);
		}

	}

}
