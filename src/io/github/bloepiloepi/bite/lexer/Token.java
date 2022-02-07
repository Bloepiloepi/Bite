package io.github.bloepiloepi.bite.lexer;

public class Token {
	
	private final TokenType tokenType;
	private final TextPosition textPosition;
	private final String value;
	
	public Token(TokenType tokenType, TextPosition textPosition, String value) {
		this.tokenType = tokenType;
		this.textPosition = textPosition;
		this.value = value;
	}
	
	public TokenType getType() {
		return tokenType;
	}
	
	public TextPosition getPosition() {
		return textPosition;
	}
	
	public String getValue() {
		return value;
	}
}
