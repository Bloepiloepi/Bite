package io.github.bloepiloepi.bite.semantic.symbol;

import java.util.Objects;

public abstract class Symbol {
	protected final String name;
	private final TypeInstanceSymbol type;
	private int scopeLevel;
	
	public Symbol(String name, TypeInstanceSymbol type) {
		this.name = name;
		this.type = type;
	}
	
	public Symbol(String name) {
		this.name = name;
		this.type = null;
	}
	
	public String getName() {
		return name;
	}
	
	public TypeInstanceSymbol getType() {
		return type;
	}
	
	public int getScopeLevel() {
		return scopeLevel;
	}
	
	public void setScopeLevel(int scopeLevel) {
		this.scopeLevel = scopeLevel;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Symbol symbol)) return false;
		return name.equals(symbol.name) &&
				Objects.equals(type, symbol.type);
	}
}
