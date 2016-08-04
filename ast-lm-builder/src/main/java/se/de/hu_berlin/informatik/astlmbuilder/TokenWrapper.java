package se.de.hu_berlin.informatik.astlmbuilder;

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
	
	
}
