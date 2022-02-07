package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.AST;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

public abstract class Expression implements AST {
	private final Token token;
	
	protected Expression(Token token) {
		this.token = token;
	}
	
	public TypeInstanceSymbol getReturnType() {
		TypeInstanceSymbol symbol = getReturnTypeNonValid();
		
		if (symbol == null) {
			Main.error("Non-valid expression: " + getToken().getPosition().format());
		}
		
		return symbol;
	}
	
	public abstract TypeInstanceSymbol getReturnTypeNonValid();
	public abstract BiteObject<?> getValue();
	
	public Token getToken() {
		return token;
	}
}
