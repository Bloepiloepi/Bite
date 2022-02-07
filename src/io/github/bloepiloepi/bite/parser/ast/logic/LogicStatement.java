package io.github.bloepiloepi.bite.parser.ast.logic;

import io.github.bloepiloepi.bite.parser.ast.statements.Statement;

public interface LogicStatement extends Statement {
	
	void execute();
}
