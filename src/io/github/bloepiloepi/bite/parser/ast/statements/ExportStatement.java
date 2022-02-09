package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.description.OperatorDescription;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.expression.Variable;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.List;

public class ExportStatement implements Statement {
	private final Token token;
	
	private final List<Expression> expressions;
	private final List<String> exportNames;
	
	public ExportStatement(Token token, List<Expression> expressions, List<String> exportNames) {
		this.token = token;
		this.expressions = expressions;
		this.exportNames = exportNames;
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
		
		for (int i = 0; i < expressions.size(); i++) {
			Expression expression = expressions.get(i);
			String exportName = exportNames.get(i);
			
			if (expression instanceof OperatorDescription operator) {
				expression.analyze();
				SemanticAnalyzer.current.addExport(operator.getSymbol());
			} else {
				if (SemanticAnalyzer.current.getExports().containsKey(exportName)) {
					Main.error("The name '" + exportName + "' has already been exported: " + expression.getToken().getPosition().format());
				}
				
				expression.analyze();
				TypeInstanceSymbol type = expression.getReturnType(true);
				if (expression instanceof Variable variable && type.getName().equals(variable.getName())) {
					SemanticAnalyzer.current.addExport(exportName, type.getBaseType());
				} else {
					SemanticAnalyzer.current.addExport(exportName, type);
				}
			}
		}
	}
	
	@Override
	public void execute() {
		for (int i = 0; i < expressions.size(); i++) {
			Expression expression = expressions.get(i);
			String exportName = exportNames.get(i);
			
			if (!(expression instanceof OperatorDescription)) {
				CallStack.current().addExport(exportName, expression.getValue());
			}
		}
	}
}
