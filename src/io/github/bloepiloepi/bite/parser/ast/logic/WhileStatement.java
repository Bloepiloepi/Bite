package io.github.bloepiloepi.bite.parser.ast.logic;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.Symbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

public class WhileStatement implements LogicStatement {
	private final Token token;
	
	private final Expression expression;
	private final StatementList block;
	
	public WhileStatement(Token token, Expression expression, StatementList block) {
		this.token = token;
		this.expression = expression;
		this.block = block;
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void execute() {
		while ((Boolean) expression.getValue().getValue()) {
			block.run();
		}
	}
	
	@Override
	public void analyze() {
		expression.analyze();
		Symbol expressionType = expression.getReturnType();
		
		if (!expressionType.equals(TypeSymbol.BOOLEAN)) {
			Main.error("Logic statement expression should be a boolean: " + token.getPosition().format());
		}
		
		SemanticAnalyzer.current.newScope(ScopeType.LOOP);
		block.analyze();
		SemanticAnalyzer.current.previousScope(token);
	}
}
