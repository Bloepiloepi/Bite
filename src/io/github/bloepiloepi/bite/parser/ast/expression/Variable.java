package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.*;

public class Variable extends Expression {
	private final String name;
	
	public Variable(Token token, String name) {
		super(token);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	private TypeInstanceSymbol returnType;
	private Symbol symbol;
	
	public Symbol getSymbol() {
		return symbol;
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
		
		this.symbol = symbol;
		returnType = symbol.getType();
	}
	
	@Override
	public BiteObject<?> getValue() {
		if (symbol instanceof FieldSymbol field) return field.createExpression();
		return CallStack.current().peek().getObject(name, symbol.getScopeLevel());
	}
	
	public static VariableSymbol getInferred(Symbol previousSymbol, TypeInstanceSymbol newType) {
		if (previousSymbol instanceof FieldSymbol field) {
			return new FieldSymbol(field.getStructureScopeLevel(), field.getName(), newType);
		} else {
			return new VariableSymbol(previousSymbol.getName(), newType);
		}
	}
}
