package se.de.hu_berlin.informatik.javatokenizer.tokenizer;

import java.io.IOException;
import java.io.StreamTokenizer;

/**
 * A tokenizer implementation that tokenizes source code
 * (Java, but may function for other languages) into
 * tokens that are separated by white spaces.
 * 
 * @author Simon Heiden
 */
public class Tokenizer {
	
	/**
	 * Enumeration of all states that the {@link Tokenizer} can be in.
	 */
	private enum TokenizerState { idle, inBlockComment, inLineComment, inString, inWord, readBackSlash, inApostrophe, readBackSlashApostrophe, done }

	/**
	 * Stores the current state of the {@link Tokenizer}. Initial state is {@link TokenizerState}.{@code idle}.
	 */
	private TokenizerState state = TokenizerState.idle;
	
	/**
	 * Stores the necessary {@link StreamTokenizer}.
	 */
	final StreamTokenizer st;
	
	/**
	 * Tthe last ttype value of the {@link StreamTokenizer}
	 */
	private int ttype = 0;
	
	/**
	 * @return the ttype
	 */
	public int getTtype() {
		return ttype;
	}

	/**
	 * Initializes a {@link Tokenizer} object with a provided {@link StreamTokenizer}.
	 * @param st
	 * {@link StreamTokenizer} that has a (Java source code) file as its input
	 * @param eol
	 * states if ends of lines (EOL) should be incorporated
	 */
	public Tokenizer(final StreamTokenizer st, final boolean eol) {
		super();
		this.st = st;
		setStreamTokenizerAttributes(eol);
	}

	/**
	 * Sets the attributes of this objects's {@link StreamTokenizer}.
	 * @param eol
	 * states if ends of lines (EOL) should be incorporated.
	 */
	private void setStreamTokenizerAttributes(final boolean eol) {
		st.resetSyntax();
		st.wordChars('0','9');
		st.wordChars('a','z');
		st.wordChars('A','Z');
		st.wordChars('_', '_');		
		st.wordChars('#', '$');
		st.wordChars('@', '@');
		//st.wordChars('"', '"');
		//st.wordChars('\'', '\'');
		//st.wordChars('\\', '\\');
		st.whitespaceChars(0,' ');
		st.whitespaceChars(127,160);
		//make space and tab ordinary chars
		st.ordinaryChar(' ');
		st.ordinaryChar('	');
		st.wordChars(161,256);
		//st.quoteChar('"');
		if (eol) {
			st.eolIsSignificant(true);
		} else {
			st.eolIsSignificant(false);
		}
		//ignore comments
//		st.slashSlashComments(true);
//		st.slashStarComments(true);
	}
	
	/**
	 * Returns the next token. May be used by the calling class.
	 * @return
	 * the next token as a {@link String}
	 * @throws IOException
	 * may be thrown by the underlying {@link StreamTokenizer}
	 */
	public String getNextToken() throws IOException {
		StringBuilder token = new StringBuilder();
		
		ttype = nextToken();
		while (state != TokenizerState.done) {
			//end of file -> return
			/*
			 * Stores a {@link String} that is used to replace spaces and tabs in actual strings and characters in the input source code.
			 */
			String SPACE_REPLACEMENT = "_";
			if (ttype == StreamTokenizer.TT_EOF) {
				break;
			}
			//line comment
			else if (state == TokenizerState.inLineComment) {
				if (ttype == StreamTokenizer.TT_EOL) {
					state = TokenizerState.done;
				}
			} 
			
			//block comment section
			else if (state == TokenizerState.inBlockComment) {
				if (ttype == '*') {
					ttype = nextToken();
					if (ttype != StreamTokenizer.TT_EOF) {
						if (ttype == '/') {
							state = TokenizerState.done;
						} else {
							//anything other than that -> don't care for now
							st.pushBack();
						}
					} else {
						//EOF
						break;
					}
				}
			}
				
			//if we are not in a String or a char or a comment section
			else if (state == TokenizerState.idle || state == TokenizerState.inWord) {
				switch (ttype) {
				case StreamTokenizer.TT_WORD:
					state = TokenizerState.inWord;
					//token is an identifier
					token.append(st.sval);
					break;
				case '\\':
					token.append("\\");
					break;
				case '"':
					state = TokenizerState.inString;
					token.append("\"");
					break;
				case '\'':
					state = TokenizerState.inApostrophe;
					token.append("\'");
					break;
				case StreamTokenizer.TT_EOL:
					if (state == TokenizerState.inWord) {
						state = TokenizerState.done;
						break;
					}
					//token is the end of the current line
					return null;
				default:					
					//if we're inside a word sequence, anything below would end it...
					if (state != TokenizerState.inWord) {
						if (ttype == '/') {
							if (nextToken() != StreamTokenizer.TT_EOF) {
								if (st.ttype == '/') {
									state = TokenizerState.inLineComment;
									break;
								} else if (st.ttype == '*') {
									state = TokenizerState.inBlockComment;
									break;
								} else {
									//anything other than that -> don't care
									st.pushBack();
								}
							} else {
								//push back EOF-token
								st.pushBack();
							}
						}
						
						//token is a whitespace/control character (return null)
						if (ttype >= 0 && ttype <= ' ' 
								|| ttype >= 127 && ttype <= 160) {
							return null;
						}
						
						//parse a possible operator
						token.append(parseOperator());
						
					} else {
						//don't consume the last token
						st.pushBack();
					}
					
					state = TokenizerState.done;
				}
			} 
			
			//in String or read backslash (in String)!
			else if (state == TokenizerState.inString || state == TokenizerState.readBackSlash) {
				switch (ttype) {
				case StreamTokenizer.TT_WORD:
					state = TokenizerState.inString;
					//\TODO: is replace really needed?
					token.append(st.sval.replace("\n", "\\n")
							.replace("\r", "\\r")
							.replace("\t", "\\t")
							.replace("\b", "\\b")
							.replace("\f", "\\f"));
					break;
				case '\\':
					//check on double backslashes (double BS negate each other...)
					if (state == TokenizerState.inString) {
						state = TokenizerState.readBackSlash;
					} else {//last character was a backslash
						state = TokenizerState.inString;
					}
					token.append("\\");
					break;
				case '"':
					token.append("\"");
					if (state == TokenizerState.inString) {
						state = TokenizerState.done;
					} else {//last character was a backslash
						state = TokenizerState.inString;
					}
					break;
				case '\'':
					state = TokenizerState.inString;
					token.append("\'");
					break;
				case StreamTokenizer.TT_EOL:
					//that shouldn't happen, though...
					state = TokenizerState.done;
					break;
				default:
					state = TokenizerState.inString;
					//token is a whitespace
					if (ttype == ' ') {
						//set spaces to be underscores in Strings for proper tokenization...
						token.append(SPACE_REPLACEMENT);
						break;
					}
					//token is a control character (return null)
					if (ttype >= 0 && ttype < ' ' 
					|| ttype >= 127 && ttype <= 160) {
						st.pushBack();
						state = TokenizerState.done;
						break;
					}
					//token is any other character like a bracket, a semicolon, a plus-sign, etc.
					//we're in a String, so just add it!
					token.append((char) ttype);
				}
			} 
			
			//in apostrophes or read backslash (in apostrophes)!
			else if (state == TokenizerState.inApostrophe || state == TokenizerState.readBackSlashApostrophe) {
				switch (ttype) {
				case StreamTokenizer.TT_WORD:
					state = TokenizerState.inApostrophe;
					//\TODO: is replace really needed?
					token.append(st.sval.replace("\n", "\\n")
							.replace("\r", "\\r")
							.replace("\t", "\\t")
							.replace("\b", "\\b")
							.replace("\f", "\\f"));
					break;
				case '\\':
					//check on double backslashes (double BS negate each other...)
					if (state == TokenizerState.inApostrophe) {
						state = TokenizerState.readBackSlashApostrophe;
					} else {//last character was a backslash
						state = TokenizerState.inApostrophe;
					}
					token.append("\\");
					break;
				case '"':
					state = TokenizerState.inApostrophe;
					token.append("\"");
					break;
				case '\'':
					if (state == TokenizerState.inApostrophe) {
						state = TokenizerState.done;
					} else {//last character was a backslash
						state = TokenizerState.inApostrophe;
					}
					token.append("\'");
					break;
				case StreamTokenizer.TT_EOL:
					//that shouldn't happen, though...
					state = TokenizerState.done;
					break;
				default:
					state = TokenizerState.inApostrophe;
					//token is a whitespace
					if (ttype == ' ') {
						//set spaces to be underscores in Strings for proper tokenization...
						token.append(SPACE_REPLACEMENT);
						break;
					}
					//token is a control character (return null)
					if (ttype >= 0 && ttype < ' ' 
					|| ttype >= 127 && ttype <= 160) {
						st.pushBack();
						state = TokenizerState.done;
						break;
					}
					//token is any other character like a bracket, a semicolon, a plus-sign, etc.
					//we're in a String, so just add it!
					token.append((char) ttype);
				}
			}
			
			if (state != TokenizerState.done) {
				ttype = nextToken();
			}
		}
		
		//if we are not in a comment section, set the state back to idle
		if (state != TokenizerState.inBlockComment && state != TokenizerState.inLineComment) {
			state = TokenizerState.idle;
		}
		
		if (token.toString().compareTo("") != 0) {
			return token.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * Parses an operator that may consist of multiple characters.
	 * @throws IOException 
	 * may be thrown by the underlying {@link StreamTokenizer}
	 */
	private String parseOperator() throws IOException {
		//token is any other character like a bracket, a semicolon, a plus-sign, etc.
		String token = String.valueOf((char)ttype);
		
		//how to deal with "multiple-space" operators...
		switch(ttype) {
		//characters which may only be followed by '='
		case '=':
		case '!':
		case '%':
		case '*':
		case '/':
		case '^':
			//characters which may only be followed by '=' or the same character
		case '|':
		case '&':
		case '+':
		case '-':
			//characters which may only be followed by "=" or the same character 
			//(or triple in case of '>'), maybe followed by '='
		case '<':
		case '>':
			if (nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_EOL) {
					ttype = st.ttype;
					break;
				} else if (st.ttype == '=') {
					//add "=" to the token (==, !=, %=, *=, ...)
					token += "=";
				} else if (st.ttype == ttype) {
					switch(ttype) {
					//characters which may be followed by the same character
					case '|':
					case '&':
					case '+':
					case '-':
						//characters which may be followed by the same character 
						//(or triple in case of '>'), maybe followed by '='
					case '<':
					case '>':
						//add the same character as formerly read to the token (||, &&, ++, --, ...)
						token += String.valueOf((char)ttype);
						switch(ttype) {
						//characters which may be followed by '=' 
						//(or the same character in case of '>')
						case '<':
						case '>':
							if (nextToken() != StreamTokenizer.TT_EOF) {
								if (st.ttype == StreamTokenizer.TT_EOL) {
									ttype = st.ttype;
									break;
								} else if (st.ttype == '=') {
									//add "=" to the token (<<=, >>=)
									token += "=";
								} else if (ttype == '>' && st.ttype == '>') {
									//add ">" to the token (>>>)
									token += ">";
									if (nextToken() != StreamTokenizer.TT_EOF) {
										if (st.ttype == StreamTokenizer.TT_EOL) {
											break;
										} else if (st.ttype == '=') {
											//add "=" to the token (>>>=)
											token += "=";
										} else {
											//anything other than that makes no sense
											st.pushBack();
										}
									}
								} else {
									//anything other than that makes no sense
									st.pushBack();
								}
							} else {
								//EOF
								ttype = st.ttype;
							}
						} //end switch
					}
				} else {
					//anything other than that makes no sense
					st.pushBack();
				}
			} else {
				//EOF
				ttype = st.ttype;
			}
		} //end switch
		
		return token;
	}

	private int nextToken() throws IOException {
		return st.nextToken();
	}
	
	public int getLineNo() {
		if (ttype != StreamTokenizer.TT_EOL) {
			return st.lineno();
		} else {
			return st.lineno()-1;
		}
	}

}
