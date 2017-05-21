package se.de.hu_berlin.informatik.astlmbuilder.wrapper;

public class TokenWrapper {

	private String token;
	private int startLineNumber;
	private int endLineNumber;
	
	public TokenWrapper(String token, int startLineNumber, int endLineNumber) {
		super();
		this.token = token;
		this.startLineNumber = startLineNumber;
		this.endLineNumber = endLineNumber;
	}

	public String getToken() {
		return token;
	}

	public int getStartLineNumber() {
		return startLineNumber;
	}
	
	public int getEndLineNumber() {
		return endLineNumber;
	}

	@Override
	public String toString() {
		return token;
	}

	@Override
	public int hashCode() {
		int hashCode = 527 + (token == null ? 0 : token.hashCode());
		hashCode = 31 * hashCode + startLineNumber;
		hashCode = 31 * hashCode + endLineNumber;
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TokenWrapper) {
			TokenWrapper token = (TokenWrapper) obj;
			if (!this.token.equals(token.getToken())) {
				return false;
			}
			if (this.startLineNumber != token.getStartLineNumber()) {
				return false;
			}
			if (this.endLineNumber != token.getEndLineNumber()) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	
}
