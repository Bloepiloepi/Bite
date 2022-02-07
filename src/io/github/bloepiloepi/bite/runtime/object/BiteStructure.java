package io.github.bloepiloepi.bite.runtime.object;

import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.HashMap;
import java.util.Map;

public class BiteStructure extends BiteObject<Map<String, BiteObject<?>>> {
	private final TypeInstanceSymbol type;
	
	public BiteStructure(TypeInstanceSymbol type) {
		super(new HashMap<>());
		this.type = type;
	}
	
	public TypeInstanceSymbol getType() {
		return type;
	}
	
	public boolean has(String name) {
		return value.containsKey(name);
	}
	
	public void store(String name, BiteObject<?> object) {
		value.put(name, object);
	}
	
	public BiteObject<?> get(String name) {
		return value.get(name);
	}
}
