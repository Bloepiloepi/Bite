package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.AST;

public interface Statement extends AST {
	Token getToken();
	void execute();
}
