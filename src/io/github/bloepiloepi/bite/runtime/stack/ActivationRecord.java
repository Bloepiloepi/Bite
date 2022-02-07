package io.github.bloepiloepi.bite.runtime.stack;

import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;

import java.util.HashMap;
import java.util.Map;

public class ActivationRecord {
	private final ScopeType type;
	private final int scopeLevel;
	private final ActivationRecord parent;
	private final ActivationRecord caller;
	
	private final Map<String, BiteObject<?>> storage = new HashMap<>();
	private BiteObject<?> returnValue = BiteObject.empty();
	private boolean returned;
	
	public ActivationRecord(ScopeType type, int scopeLevel, ActivationRecord parent, ActivationRecord caller) {
		this.type = type;
		this.scopeLevel = scopeLevel;
		this.parent = parent;
		this.caller = caller;
	}
	
	public ScopeType getType() {
		return type;
	}
	
	public int getScopeLevel() {
		return scopeLevel;
	}
	
	public BiteObject<?> getReturnValue() {
		return returnValue;
	}
	
	public boolean isReturned() {
		return returned;
	}
	
	public void return_(BiteObject<?> returnValue) {
		this.returned = true;
		this.returnValue = returnValue;
	}
	
	public ActivationRecord getCaller() {
		return caller;
	}
	
	public void store(String name, BiteObject<?> object) {
		store(name, object, -1);
	}
	
	public void store(String name, BiteObject<?> object, int scopeLevel) {
		if (scopeLevel == -1 || this.scopeLevel == scopeLevel) {
			storage.put(name, object);
		} else if (parent != null) {
			parent.store(name, object, scopeLevel);
		} else {
			throw new RuntimeException("Cannot store in scope level " + scopeLevel);
		}
	}
	
	public BiteObject<?> getObject(String name, int scopeLevel) {
		if (scopeLevel == -1 || this.scopeLevel == scopeLevel) {
			return storage.get(name);
		} else if (parent != null) {
			return parent.getObject(name, scopeLevel);
		} else {
			throw new RuntimeException("Cannot get in scope level " + scopeLevel);
		}
	}
}
