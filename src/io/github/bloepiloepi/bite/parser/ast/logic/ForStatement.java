package io.github.bloepiloepi.bite.parser.ast.logic;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.statements.Statement;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.Symbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

public class ForStatement implements LogicStatement {
	private final Token token;
	
	private final Statement initialize;
	private final Expression expression;
	private final Statement change;
	
	private final StatementList block;
	
	public ForStatement(Token token, Statement initialize, Expression expression, Statement change, StatementList block) {
		this.token = token;
		this.initialize = initialize;
		this.expression = expression;
		this.change = change;
		this.block = block;
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void execute() {
		for (initialize.execute(); (Boolean) expression.getValue().getValue(); change.execute()) {
			block.run();
		}
	}
	
	@Override
	public void analyze() {
		initialize.analyze();
		expression.analyze();
		change.analyze();
		
		Symbol expressionType = expression.getReturnType(true);
		
		if (!expressionType.equals(TypeSymbol.BOOLEAN)) {
			Main.error("Logic statement expression should be a boolean: " + token.getPosition().format());
		}
		
		SemanticAnalyzer.current.newScope(ScopeType.LOOP);
		block.analyze();
		SemanticAnalyzer.current.previousScope(token);
	}
}
