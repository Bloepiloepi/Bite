package io.github.bloepiloepi.bite.semantic.builtin;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;

import java.util.function.Consumer;

public class NativeStatementList extends StatementList {
	private final Consumer<ActivationRecord> statements;
	
	public NativeStatementList(Consumer<ActivationRecord> statements) {
		super(null);
		this.statements = statements;
	}
	
	@Override
	public void analyze() {
	
	}
	
	@Override
	public void run() {
		statements.accept(CallStack.current().peek());
	}
}
