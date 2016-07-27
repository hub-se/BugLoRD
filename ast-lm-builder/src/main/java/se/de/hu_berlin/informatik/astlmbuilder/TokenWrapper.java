package se.de.hu_berlin.informatik.astlmbuilder;

public class TokenWrapper {

	private String token;
	private int lineNumber;
	
	public TokenWrapper(String token, int lineNumber) {
		super();
		this.token = token;
		this.lineNumber = lineNumber;
	}

	public String getToken() {
		return token;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
}
