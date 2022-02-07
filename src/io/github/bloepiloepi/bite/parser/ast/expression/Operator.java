package io.github.bloepiloepi.bite.parser.ast.expression;

public enum Operator {
	PLUS,
	MINUS,
	DIVIDE,
	MULTIPLY,
	DOT,
	EQUALS,
	GREATER_THAN,
	LESS_THAN,
	DOUBLE_AND,
	DOUBLE_OR,
	CALL,
	LIST_ACCESS,
	ASSIGN;
	
	public boolean hasStaticOperands() {
		return this == Operator.LIST_ACCESS || this == Operator.GREATER_THAN || this == Operator.LESS_THAN || this == ASSIGN || this == CALL;
	}
}
