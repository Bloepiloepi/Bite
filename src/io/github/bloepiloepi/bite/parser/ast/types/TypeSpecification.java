package io.github.bloepiloepi.bite.parser.ast.types;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.*;

import java.util.ArrayList;
import java.util.List;

public class TypeSpecification extends Expression {
	public static final String VOID = "void";
	public static final String UNKNOWN = "?";
	
	private final String name;
	private final List<TypeSpecification> generics;
	
	private boolean allowGeneric = true;
	private boolean allowInfer = false;
	private boolean shouldInfer = false;
	
	public TypeSpecification(Token token, String name) {
		super(token);
		this.name = name;
		this.generics = null;
	}
	
	public TypeSpecification(Token token, String name, List<TypeSpecification> generics) {
		super(token);
		this.name = name;
		this.generics = generics;
	}
	
	public void allowGeneric(boolean allowGeneric) {
		this.allowGeneric = allowGeneric;
	}
	
	public void allowInfer(boolean allowInfer) {
		this.allowInfer = allowInfer;
	}
	
	public boolean shouldInfer() {
		return shouldInfer;
	}
	
	public void infer(TypeInstanceSymbol symbol) {
		this.symbol = symbol;
	}
	
	private TypeInstanceSymbol symbol = null;
	
	public TypeInstanceSymbol getSymbol() {
		return symbol;
	}
	
	@Override
	public void analyze() {
		if (symbol != null) { //Already analyzed
			return;
		}
		
		if (name.equals(UNKNOWN)) {
			symbol = NativeTypes.UNKNOWN_INSTANCE;
			return;
		} else if (name.equals(VOID)) {
			symbol = NativeTypes.VOID_INSTANCE;
			return;
		}
		
		Symbol type = SemanticAnalyzer.current.currentScope.lookup(name, false);
		if (type instanceof VariableSymbol && type.getType().getName().equals(name)) {
			type = type.getType().getBaseType();
		}
		if (type == null) {
			Main.error("Cannot resolve symbol '" + name + "': " + getToken().getPosition().format());
		}
		if (!allowGeneric && type instanceof GenericTypeSymbol) {
			Main.error("Generic type not allowed here: " + getToken().getPosition().format());
		}
		if (type instanceof TypeSymbol typeSymbol) {
			List<TypeInstanceSymbol> genericSymbols = new ArrayList<>();
			
			if (generics != null) {
				if (typeSymbol.getFormalGenerics().size() < generics.size()) {
					Main.error("Not all generics for '" + name + "' are specified: " + getToken().getPosition().format());
				} else if (typeSymbol.getFormalGenerics().size() > generics.size()) {
					Main.error("Too many generics for '" + name + "' specified: " + getToken().getPosition().format());
				}
				
				for (TypeSpecification specification : generics) {
					specification.analyze();
					genericSymbols.add(specification.getSymbol());
				}
			} else if (typeSymbol.getFormalGenerics().size() > 0) {
				if (!allowInfer) {
					Main.error("Not all generics for '" + name + "' are specified: " + getToken().getPosition().format());
				} else {
					shouldInfer = true;
				}
			}
			
			symbol = new TypeInstanceSymbol(typeSymbol, genericSymbols);
		} else {
			Main.error("Not a type: '" + name + "' at " + getToken().getPosition().format());
		}
	}
	
	@Override
	public BiteObject<?> getValue() {
		Main.error("Cannot use TypeSpecification as expression: " + getToken().getPosition().format());
		return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof TypeSpecification && ((TypeSpecification) obj).name.equals(name);
	}
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return NativeTypes.VOID_INSTANCE;
	}
}
