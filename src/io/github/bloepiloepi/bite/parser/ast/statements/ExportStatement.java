package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.description.OperatorDescription;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.expression.Variable;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.VariableSymbol;

public class ExportStatement implements Statement {
	private final Token token;
	
	private final Expression expression;
	private final String exportName;
	
	public ExportStatement(Token token, Expression expression, String exportName) {
		this.token = token;
		this.expression = expression;
		this.exportName = exportName;
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void analyze() {
		if (SemanticAnalyzer.current.fileScope != SemanticAnalyzer.current.currentScope) {
			Main.error("Export only allowed on top level: " + token.getPosition().format());
		}
		
		if (expression instanceof OperatorDescription expression) {
			expression.analyze();
			SemanticAnalyzer.current.addExport(expression.getSymbol());
		} else {
			expression.analyze();
			TypeInstanceSymbol type = expression.getReturnType();
			if (expression instanceof Variable variable && type.getName().equals(variable.getName())) {
				SemanticAnalyzer.current.addExport(exportName, type.getBaseType());
			} else {
				SemanticAnalyzer.current.addExport(exportName, type);
			}
		}
	}
	
	@Override
	public void execute() {
		if (!(expression instanceof OperatorDescription)) {
			CallStack.current().addExport(exportName, expression.getValue());
		}
	}
}
