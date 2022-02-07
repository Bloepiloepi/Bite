package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.FieldSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.Symbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

public class Variable extends Expression {
	private final String name;
	
	public Variable(Token token, String name) {
		super(token);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	private int scopeLevel;
	private TypeInstanceSymbol returnType;
	private FieldSymbol field;
	
	public int getScopeLevel() {
		return scopeLevel;
	}
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		Symbol symbol = SemanticAnalyzer.current.currentScope.lookup(name, false);
		
		if (symbol == null) {
			Main.error("Variable '" + name + "' not found: " + getToken().getPosition().format());
			return;
		}
		
		if (symbol instanceof FieldSymbol fieldSymbol) {
			field = fieldSymbol;
		}
		
		scopeLevel = symbol.getScopeLevel();
		returnType = symbol.getType();
	}
	
	@Override
	public BiteObject<?> getValue() {
		if (field != null) return field.createExpression();
		return CallStack.current().peek().getObject(name, scopeLevel);
	}
}
