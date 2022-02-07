package io.github.bloepiloepi.bite.lexer;

import io.github.bloepiloepi.bite.Main;

import java.util.Objects;

public class Lexer {
	
	private final String text;
	private Character currentChar;
	private int pos = 0;
	
	private final TextPositionManager textPositionManager;
	
	public Lexer(String filename, String text) {
		this.textPositionManager = new TextPositionManager(filename);
		this.text = text;
		try {
			this.currentChar = text.charAt(pos);
		} catch (StringIndexOutOfBoundsException e) {
			this.currentChar = null;
		}
	}
	
	private void next() {
		if (currentChar == '\n') {
			textPositionManager.newLine();
		}
		
		pos += 1;
		if (pos > text.length() - 1) {
			currentChar = null;
		} else {
			textPositionManager.advance(currentChar == '\t');
			
			try {
				currentChar = text.charAt(pos);
			} catch (StringIndexOutOfBoundsException e) {
				currentChar = null;
			}
		}
	}
	
	private Character peek() {
		int peekPosition = pos + 1;
		if (peekPosition > text.length() - 1) {
			return null;
		} else {
			Character peek;
			
			try {
				peek = text.charAt(peekPosition);
			} catch (StringIndexOutOfBoundsException e) {
				peek = null;
			}
			
			return peek;
		}
	}
	
	private void skipWhitespace() {
		while (currentChar != null && Character.isWhitespace(currentChar)) {
			next();
		}
	}
	
	private void skipLineComment() {
		while (currentChar != null && currentChar != '\n') {
			next();
		}
	}
	
	private void skipBlockComment() {
		Character peek = peek();
		if (peek == null) return;
		
		while (currentChar != null && !(currentChar == '*' && peek == '/')) {
			next();
		}
		next();
		next();
	}
	
	private Token number() {
		StringBuilder result = new StringBuilder();
		
		while (currentChar != null && Character.isDigit(currentChar)) {
			result.append(currentChar);
			next();
		}
		
		if (currentChar != null && currentChar == '.') {
			result.append(currentChar);
			next();
			
			while (currentChar != null && Character.isDigit(currentChar)) {
				result.append(currentChar);
				next();
			}
			
			return new Token(TokenType.FLOAT_CONST, textPositionManager.getCurrent(), result.toString());
		} else {
			return new Token(TokenType.INTEGER_CONST, textPositionManager.getCurrent(), result.toString());
		}
	}
	
	private Token identifier() {
		StringBuilder result = new StringBuilder();
		while (currentChar != null && (Character.isAlphabetic(currentChar) || Character.isDigit(currentChar) || currentChar == '_')) {
			result.append(currentChar);
			next();
		}
		
		TokenType tokenType = TokenType.typeOfToken(result.toString());
		return new Token(Objects.requireNonNullElse(tokenType, TokenType.IDENTIFIER), textPositionManager.getCurrent(), result.toString());
	}
	
	private Token string() {
		StringBuilder result = new StringBuilder();
		
		next();
		while (currentChar != null && currentChar != '"') {
			result.append(currentChar);
			next();
		}
		next();
		
		return new Token(TokenType.STRING, textPositionManager.getCurrent(), result.toString());
	}
	
	public Token getNextToken() {
		while (currentChar != null) {
			if (Character.isWhitespace(currentChar)) {
				skipWhitespace();
				continue;
			}
			
			if (currentChar == '/') {
				Character peek = this.peek();
				if (peek == null) {
					next();
					continue;
				}
				
				if (peek == '*') {
					skipBlockComment();
				} else if (peek == '/') {
					skipLineComment();
				}
				
				continue;
			}
			
			if (Character.isDigit(currentChar)) {
				return number();
			}
			
			if (Character.isAlphabetic(currentChar) || currentChar == '_') {
				return identifier();
			}
			
			if (currentChar == '"') {
				return string();
			}
			
			Token token;
			TokenType tokenType = TokenType.typeOfToken(currentChar.toString());
			
			if (tokenType != null) {
				if (tokenType.checkDoubleFirst) {
					Character peek = this.peek();
					
					if (peek != null) {
						TokenType tokenType2 = TokenType.typeOfToken(currentChar.toString() + peek);
						
						if (tokenType2 != null) {
							token = new Token(tokenType2, textPositionManager.getCurrent(), currentChar.toString() + peek);
							next();
							next();
							return token;
						}
					}
				}
				
				token = new Token(tokenType, textPositionManager.getCurrent(), currentChar.toString());
			} else {
				Character peek = this.peek();
				if (peek == null) error();
				assert peek != null;
				
				tokenType = TokenType.typeOfToken(currentChar.toString() + peek);
				if (tokenType == null) {
					error();
				}
				
				token = new Token(tokenType, textPositionManager.getCurrent(), currentChar.toString() + peek);
				next();
			}
			
			next();
			return token;
		}
		
		return new Token(TokenType.EOF, this.textPositionManager.getCurrent(), "");
	}
	
	public Token peekToken(int peek) {
		int line = textPositionManager.getCurrent().line();
		int column = textPositionManager.getCurrent().column();
		int prevPos = pos;
		
		for (int i = 0; i < peek - 1; i++) {
			getNextToken();
		}
		Token token = getNextToken();
		
		pos = prevPos;
		try {
			currentChar = text.charAt(pos);
		} catch (StringIndexOutOfBoundsException e) {
			currentChar = null;
		}
		textPositionManager.setLine(line);
		textPositionManager.setColumn(column);
		
		return token;
	}
	
	private void error() {
		TextPosition current = textPositionManager.getCurrent();
		Main.error("Unknown token '" + currentChar + "' at " + current.format());
	}
}
