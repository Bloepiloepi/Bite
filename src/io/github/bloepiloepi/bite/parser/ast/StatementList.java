package io.github.bloepiloepi.bite.parser.ast;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.parser.ast.statements.Statement;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;

import java.util.List;

public class StatementList implements AST {
	public static final StatementList EMPTY = new StatementList(List.of());
	
	private final List<Statement> statements;
	
	public StatementList(List<Statement> statements) {
		this.statements = statements;
	}
	
	@Override
	public void analyze() {
		for (Statement statement : statements) {
			if (SemanticAnalyzer.current.currentScope.isReturned()) {
				Main.error("Unreachable statement: " + statement.getToken().getPosition().format());
			}
			statement.analyze();
		}
	}
	
	public void run() {
		for (Statement statement : statements) {
			statement.execute();
			if (CallStack.current().peek().isReturned()) {
				break;
			}
		}
	}
}
