package io.github.bloepiloepi.bite.semantic.symbol;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
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
	public static final TypeSymbol FUNCTION = new TypeSymbol("function", Map.of(), Map.of(), StatementList.EMPTY, List.of("returnType"));
	public static final TypeSymbol LIST = new TypeSymbol("list", Map.of(), Map.of(), StatementList.EMPTY, List.of("contentType"));
	
	//Only expression values
	public static final TypeSymbol NULL = new TypeSymbol("null");
	public static final TypeSymbol VOID = new TypeSymbol("void");
	
	public static final TypeSymbol UNKNOWN = new TypeSymbol("?");
	
	private final Map<String, Symbol> subSymbols;
	private final Map<String, Symbol> staticSubSymbols;
	private final Map<String, BiteObject<?>> staticSubFields = new HashMap<>();
	private final StatementList statements;
	private final List<String> generics;
	
	private ActivationRecord context = CallStack.global;
	
	private final TypeInstanceSymbol staticType = new TypeInstanceSymbol(this, List.of());
	
	public TypeSymbol(String name, Map<String, Symbol> subSymbols, Map<String, Symbol> staticSubSymbols, StatementList statements, List<String> generics) {
		super(name);
		this.subSymbols = subSymbols;
		this.staticSubSymbols = staticSubSymbols;
		this.statements = statements;
		this.generics = generics;
		
		//Store the type structure
		CallStack.global.store(name, BiteObject.structure(staticType));
	}
	
	public TypeSymbol(String name) {
		this(name, new HashMap<>(), new HashMap<>(), StatementList.EMPTY, List.of());
	}
	
	public Map<String, Symbol> getSubSymbols() {
		return subSymbols;
	}
	
	public Map<String, Symbol> getStaticSubSymbols() {
		return staticSubSymbols;
	}
	
	public StatementList getStatements() {
		return statements;
	}
	
	public List<String> getFormalGenerics() {
		return generics;
	}
	
	public ActivationRecord getContext() {
		return context;
	}
	
	public void setContext(ActivationRecord context) {
		this.context = context;
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
}
