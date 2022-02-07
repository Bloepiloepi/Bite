package io.github.bloepiloepi.bite.parser.ast.logic;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

public class IfStatement implements LogicStatement {
	private final Token token;
	private final Token elseToken;
	private final Expression expression;
	
	private final StatementList block;
	private final StatementList elseBlock;
	private final IfStatement elseIf;
	
	public IfStatement(Token token, Token elseToken, Expression expression, StatementList block, StatementList elseBlock, IfStatement elseIf) {
		this.token = token;
		this.elseToken = elseToken;
		this.expression = expression;
		this.block = block;
		this.elseBlock = elseBlock;
		this.elseIf = elseIf;
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void execute() {
		Boolean value = (Boolean) expression.getValue().getValue();
		if (value) {
			block.run();
		} else if (elseBlock != null) {
			elseBlock.run();
		} else if (elseIf != null) {
			elseIf.execute();
		}
	}
	
	@Override
	public void analyze() {
		expression.analyze();
		TypeInstanceSymbol expressionType = expression.getReturnType();
		
		if (!expressionType.getBaseType().equals(TypeSymbol.BOOLEAN)) {
			Main.error("Logic statement expression should be a boolean: " + token.getPosition().format());
		}
		
		SemanticAnalyzer.current.newScope(ScopeType.IF);
		
		block.analyze();
		
		SemanticAnalyzer.current.previousScope(token);
		
		if (elseBlock != null) {
			SemanticAnalyzer.current.newScope(ScopeType.IF);
			elseBlock.analyze();
			SemanticAnalyzer.current.previousScope(elseToken);
		} else if (elseIf != null) {
			elseIf.analyze();
		}
	}
}
