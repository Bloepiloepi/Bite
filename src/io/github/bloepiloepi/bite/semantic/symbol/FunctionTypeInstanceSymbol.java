package io.github.bloepiloepi.bite.semantic.symbol;

import java.util.ArrayList;
import java.util.List;

public class FunctionTypeInstanceSymbol extends TypeInstanceSymbol {
	private final List<TypeInstanceSymbol> parameterTypes;
	
	public FunctionTypeInstanceSymbol(TypeSymbol type, List<TypeInstanceSymbol> generics, List<TypeInstanceSymbol> parameterTypes) {
		super(type, generics);
		this.parameterTypes = parameterTypes;
	}
	
	public List<TypeInstanceSymbol> getParameterTypes() {
		return parameterTypes;
	}
	
	@Override
	public String getName() {
		StringBuilder builder = new StringBuilder(name).append("::(");
		builder.append(getGenerics().get(0).getName()).append(".(");
		for (int i = 0, genericsSize = parameterTypes.size(); i < genericsSize; i++) {
			if (i != 0) builder.append(", ");
			TypeInstanceSymbol type = parameterTypes.get(i);
			builder.append(type.getName());
		}
		builder.append("))");
		return builder.toString();
	}
	
	@Override
	public FunctionTypeInstanceSymbol getReal(TypeInstanceSymbol genericHolder) {
		List<TypeInstanceSymbol> realGenerics = new ArrayList<>();
		List<TypeInstanceSymbol> realParameters = new ArrayList<>();
		
		for (TypeInstanceSymbol generic : getGenerics()) {
			realGenerics.add(generic.getReal(genericHolder));
		}
		for (TypeInstanceSymbol parameter : parameterTypes) {
			realParameters.add(parameter.getReal(genericHolder));
		}
		
		return new FunctionTypeInstanceSymbol(getBaseType(), realGenerics, realParameters);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof FunctionTypeInstanceSymbol that) {
			return super.equals(o) && parameterTypes.equals(that.parameterTypes);
		} else {
			return super.equals(o);
		}
	}
}
