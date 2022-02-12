package io.github.bloepiloepi.bite.semantic.symbol;

import io.github.bloepiloepi.bite.parser.ast.definition.FunctionDefinition;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeSymbol extends Symbol {
	
	//Types
	public static final TypeSymbol INTEGER = new TypeSymbol("int");
	public static final TypeSymbol FLOAT = new TypeSymbol("float");
	public static final TypeSymbol STRING = new TypeSymbol("string");
	public static final TypeSymbol BOOLEAN = new TypeSymbol("boolean");
	//TODO constructors
	public static final TypeSymbol FUNCTION = new TypeSymbol("function", Map.of(), Map.of(), List.of("returnType"), true);
	public static final TypeSymbol LIST = new TypeSymbol("list", Map.of(), Map.of(), List.of("contentType"), true);
	
	//Only expression values
	public static final TypeSymbol NULL = new TypeSymbol("null");
	public static final TypeSymbol VOID = new TypeSymbol("void");
	
	public static final TypeSymbol UNKNOWN = new TypeSymbol("?");
	
	private final Map<String, Symbol> subSymbols;
	private final Map<String, Symbol> staticSubSymbols;
	private final Map<String, BiteObject<?>> staticSubFields = new HashMap<>();
	private final List<String> generics;
	private final boolean primitive;
	
	private List<TypeInstanceSymbol> constructorArgumentTypes;
	private FunctionDefinition constructor;
	
	private final TypeInstanceSymbol staticType = new TypeInstanceSymbol(this, List.of());
	
	public TypeSymbol(String name, Map<String, Symbol> subSymbols, Map<String, Symbol> staticSubSymbols, List<String> generics, boolean primitive) {
		super(name);
		this.subSymbols = subSymbols;
		this.staticSubSymbols = staticSubSymbols;
		this.generics = generics;
		this.primitive = primitive;
		
		//Store the type structure
		CallStack.global.store(name, BiteObject.structure(staticType));
	}
	
	public TypeSymbol(String name) {
		this(name, new HashMap<>(), new HashMap<>(), List.of(), true);
	}
	
	public Map<String, Symbol> getSubSymbols() {
		return subSymbols;
	}
	
	public Map<String, Symbol> getStaticSubSymbols() {
		return staticSubSymbols;
	}
	
	public List<String> getFormalGenerics() {
		return generics;
	}
	
	public boolean isPrimitive() {
		return primitive;
	}
	
	public List<TypeInstanceSymbol> getConstructorArgumentTypes() {
		return constructorArgumentTypes;
	}
	
	public void setConstructorArgumentTypes(List<TypeInstanceSymbol> constructorArgumentTypes) {
		this.constructorArgumentTypes = constructorArgumentTypes;
	}
	
	public FunctionDefinition getConstructor() {
		return constructor;
	}
	
	public void setConstructor(FunctionDefinition constructor) {
		this.constructor = constructor;
	}
	
	@Override
	public TypeInstanceSymbol getType() {
		return staticType;
	}
	
	public boolean isGeneric() {
		return false;
	}
	
	public BiteObject<?> getStaticField(String name) {
		return staticSubFields.get(name);
	}
	
	public void setStaticField(String name, BiteObject<?> object) {
		staticSubFields.put(name, object);
	}
	
	public boolean canAutoCast(TypeSymbol other) {
		return super.equals(other) || (this == INTEGER && other == FLOAT);
	}
	
	public boolean canCast(TypeSymbol other) {
		return canAutoCast(other) || (this == FLOAT && other == INTEGER);
	}
	
	public boolean equalsExact(TypeSymbol other) {
		return super.equals(other);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof TypeSymbol that)
			return canAutoCast(that);
		return super.equals(o);
	}
}
