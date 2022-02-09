package io.github.bloepiloepi.bite.semantic.symbol;

import java.util.*;
import java.util.function.Function;

public class TypeInstanceSymbol extends Symbol {
	private final TypeSymbol type;
	private final List<TypeInstanceSymbol> generics;
	private final boolean complete;
	
	public TypeInstanceSymbol(TypeSymbol type, List<TypeInstanceSymbol> generics) {
		this(type, generics, true);
	}
	
	public TypeInstanceSymbol(TypeSymbol type, List<TypeInstanceSymbol> generics, boolean complete) {
		super(type.getName());
		this.type = type;
		this.generics = generics;
		this.complete = complete;
	}
	
	public TypeSymbol getBaseType() {
		return type;
	}
	
	public List<TypeInstanceSymbol> getGenerics() {
		return generics;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	@Override
	public String getName() {
		if (generics.size() == 0) {
			return name;
		} else if (generics.size() == 1) {
			return name + "::" + generics.get(0).getName();
		} else {
			StringBuilder builder = new StringBuilder(name).append("::(");
			for (int i = 0, genericsSize = generics.size(); i < genericsSize; i++) {
				if (i != 0) builder.append(", ");
				TypeInstanceSymbol generic = generics.get(i);
				builder.append(generic.getName());
			}
			builder.append(")");
			return builder.toString();
		}
	}
	
	public TypeInstanceSymbol getReal(TypeInstanceSymbol genericHolder) {
		if (getBaseType().isGeneric()) {
			String name = getBaseType().getName();
			int index = genericHolder.getBaseType().getFormalGenerics().indexOf(name);
			
			return genericHolder.generics.get(index);
		} else {
			List<TypeInstanceSymbol> realGenerics = new ArrayList<>();
			
			for (TypeInstanceSymbol generic : generics) {
				realGenerics.add(generic.getReal(genericHolder));
			}
			
			return new TypeInstanceSymbol(type, realGenerics, complete);
		}
	}
	
	public TypeInstanceSymbol resolveGenerics(Function<String, TypeInstanceSymbol> function) {
		if (getBaseType().isGeneric()) {
			return function.apply(getBaseType().getName());
		} else {
			List<TypeInstanceSymbol> resolved = new ArrayList<>();
			
			for (TypeInstanceSymbol generic : generics) {
				resolved.add(generic.resolveGenerics(function));
			}
			
			return new TypeInstanceSymbol(type, resolved, complete);
		}
	}
	
	public boolean canAutoCast(TypeInstanceSymbol other) {
		return generics.size() == 0 && type.canAutoCast(other.type);
	}
	
	public boolean equalsExact(TypeInstanceSymbol other) {
		return name.equals(other.name) && Objects.equals(generics, other.generics);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof TypeInstanceSymbol that) {
			if (type.equals(TypeSymbol.UNKNOWN) || that.getBaseType().equals(TypeSymbol.UNKNOWN)) {
				return true;
			}
			
			return canAutoCast(that) ||
					(name.equals(that.name) && Objects.equals(generics, that.generics));
		} else if (o instanceof Symbol) {
			return o.equals(this);
		}
		
		return false;
	}
	
	private Map<String, TypeInstanceSymbol> genericReplacements;
	
	public Map<String, TypeInstanceSymbol> getGenericReplacements() {
		return genericReplacements;
	}
	
	public boolean equalsUnGenerified(TypeInstanceSymbol other) {
		return equalsUnGenerified(other, new HashMap<>());
	}
	
	public boolean equalsUnGenerified(TypeInstanceSymbol other, Map<String, TypeInstanceSymbol> replacements) {
		genericReplacements = replacements;
		
		if (this == other) return true;
		if (type.isGeneric()) {
			genericReplacements.put(type.getName(), other);
			return true;
		}
		if (other.type.isGeneric()) {
			genericReplacements.put(other.type.getName(), this);
			return true;
		}
		
		if (!this.type.equalsExact(other.type)) return false;
		
		for (int i = 0; i < generics.size(); i++) {
			if (!generics.get(i).equalsUnGenerified(other.generics.get(i)))
				return false;
			
			final boolean[] flag = new boolean[1];
			generics.get(i).getGenericReplacements().forEach((name, replacement) -> {
				if (genericReplacements.containsKey(name) && !genericReplacements.get(name).equals(replacement)) {
					flag[0] = true;
				}
				genericReplacements.put(name, replacement);
			});
			
			if (flag[0]) return false;
		}
		
		return true;
	}
}
