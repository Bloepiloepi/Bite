package io.github.bloepiloepi.bite.runtime.object;

import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.Objects;

public class BiteObject<T> {
	protected final T value;
	
	public BiteObject(T value) {
		this.value = value;
	}
	
	public T getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BiteObject<?> that = (BiteObject<?>) o;
		return Objects.equals(value, that.value);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
	
	public static BiteObject<Integer> intValue(Integer value) {
		return new BiteObject<>(value);
	}
	
	public static BiteObject<Float> floatValue(Float value) {
		return new BiteObject<>(value);
	}
	
	public static BiteObject<String> stringValue(String value) {
		return new BiteObject<>(value);
	}
	
	public static BiteObject<Boolean> booleanValue(Boolean value) {
		return new BiteObject<>(value);
	}
	
	public static BiteStructure structure(TypeInstanceSymbol type) {
		return new BiteStructure(type);
	}
	
	public static <T> BiteObject<T> builtinObject(T value) {
		return new BiteObject<>(value);
	}
	
	public static <T> BiteObject<T> empty() {
		return new BiteObject<>(null);
	}
}
