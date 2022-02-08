package io.github.bloepiloepi.bite.semantic.symbol;

import io.github.bloepiloepi.bite.runtime.object.BiteStructure;
import io.github.bloepiloepi.bite.runtime.object.StructureField;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;

public class FieldSymbol extends VariableSymbol {
	private final int structureScopeLevel;
	
	public FieldSymbol(int structureScopeLevel, String name, TypeInstanceSymbol type) {
		super(name, type);
		this.structureScopeLevel = structureScopeLevel;
	}
	
	public int getStructureScopeLevel() {
		return structureScopeLevel;
	}
	
	public StructureField createExpression() {
		return new StructureField((BiteStructure) CallStack.current().peek().getObject("this", structureScopeLevel), name);
	}
}
