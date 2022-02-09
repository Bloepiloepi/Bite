package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

public class ReturnStatement implements Statement {
	private final Token token;
	private final Expression expression;
	
	public ReturnStatement(Token token, Expression expression) {
		this.token = token;
		this.expression = expression;
	}
	
	public Expression getExpression() {
		return expression;
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	private TypeInstanceSymbol scopeReturnType;
	
	@Override
	public void analyze() {
		if (!SemanticAnalyzer.current.currentScope.isInsideScopeType(ScopeType.FUNCTION)) {
			Main.error("Return statement is not allowed here: " + expression.getToken().getPosition().format());
		}
		
		scopeReturnType = SemanticAnalyzer.current.currentScope.getReturnType();
		
		TypeInstanceSymbol returnType;
		if (expression != null) {
			expression.analyze();
			returnType = expression.getReturnType(true);
		} else {
			returnType = NativeTypes.VOID_INSTANCE;
		}
		
		if (returnType.equals(scopeReturnType)) {
			//TODO
			//SemanticAnalyzer.current.currentScope.return_();
		} else {
			if (expression == null) Main.error("Missing return value: " + token.getPosition().format());
			else Main.error("Invalid type, '" + SemanticAnalyzer.current.currentScope.getReturnType().getName() + "' required: " + expression.getToken().getPosition().format());
		}
	}
	
	@Override
	public void execute() {
		if (expression != null) {
			CallStack.current().peek().return_(expression.getValue().cast(scopeReturnType.getBaseType()));
		} else {
			CallStack.current().peek().return_(null);
		}
	}
}
