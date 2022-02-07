package io.github.bloepiloepi.bite.semantic.symbol;

public class GenericTypeSymbol extends TypeSymbol {
	
	public GenericTypeSymbol(String name) {
		super(name);
	}
	
	public TypeInstanceSymbol getReal(TypeInstanceSymbol genericHolder) {
		return genericHolder.getGenerics().get(genericHolder.getBaseType().getFormalGenerics().indexOf(getName()));
	}
	
	@Override
	public boolean isGeneric() {
		return true;
	}
}
