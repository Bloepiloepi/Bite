package io.github.bloepiloepi.bite.lexer;

import java.util.HashMap;

public enum TokenType {
	//Operators
	PLUS("+", true),
	DOUBLE_PLUS("++"),
	PLUS_ASSIGN("+="),
	MINUS("-", true),
	DOUBLE_MINUS("--"),
	MINUS_ASSIGN("-="),
	DIVIDE("/", true),
	DIVIDE_ASSIGN("/="),
	MULTIPLY("*", true),
	MULTIPLY_ASSIGN("*="),
	EQUALS("=="),
	GREATER_THAN(">"),
	LESS_THAN("<"),
	DOUBLE_AND("&&"),
	DOUBLE_OR("||"),
	
	//Lexer native
	INTEGER_CONST(null),
	FLOAT_CONST(null),
	
	STRING(null),
	IDENTIFIER(null),
	
	//Punctuation & syntax
	COMMA(","),
	DOT("."),
	COLON(":", true),
	GENERIC("::"),
	BRACE_OPEN("{"),
	BRACE_CLOSE("}"),
	LPAREN("("),
	RPAREN(")"),
	ASSIGN("=", true),
	SEMI(";"),
	SQUARE_BRACE_OPEN("["),
	SQUARE_BRACE_CLOSE("]"),
	AND("&"),
	UNKNOWN("?"),
	
	//Misc keywords
	OPERATOR("operator"),
	IMPORT("import"),
	FROM("from"),
	EXPORT("export"),
	AS("as"),
	NEW("new"),
	
	//Logic keywords
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	FOR("for"),
	RETURN("return"),
	
	//Boolean keywords
	TRUE("true"),
	FALSE("false"),
	NULL("null"),
	
	//Type keywords
	STRUCT("struct"),
	VOID("void"),
	
	//Modifiers
	GLOBAL("global"),
	
	//Misc
	EOF(null);
	
	private static final HashMap<String, TokenType> BY_VALUE = new HashMap<>();
	
	static {
		for (TokenType e: values()) {
			if (e.value != null) {
				BY_VALUE.put(e.value, e);
			}
		}
	}
	
	public final String value;
	public boolean checkDoubleFirst = false;
	
	TokenType(String value) {
		this.value = value;
	}
	
	TokenType(String value, boolean checkDoubleFirst) {
		this.value = value;
		this.checkDoubleFirst = checkDoubleFirst;
	}
	
	public static TokenType typeOfToken(String token) {
		return BY_VALUE.get(token);
	}
}
