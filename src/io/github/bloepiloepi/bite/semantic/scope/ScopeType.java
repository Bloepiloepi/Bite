package io.github.bloepiloepi.bite.semantic.scope;

public enum ScopeType {
	GLOBAL,
	FILE,
	FUNCTION,
	STRUCTURE,
	IF,
	LOOP;
	
	public boolean canReturn() {
		return this == FUNCTION;
	}
}
