package io.github.bloepiloepi.bite.semantic.scope;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.semantic.builtin.NativeFunctions;
import io.github.bloepiloepi.bite.semantic.builtin.NativeOperators;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.symbol.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopedSymbolTable {
	private final Map<String, Symbol> symbols = new HashMap<>();
	private final Map<String, Symbol> globalSymbols = new HashMap<>();
	private final List<OperatorSymbol> operators = new ArrayList<>();
	
	private final ScopeType scopeType;
	private final int scopeLevel;
	private boolean returned;
	private TypeInstanceSymbol returnType;
	private final ScopedSymbolTable enclosingScope;
	
	public ScopedSymbolTable(ScopeType scopeName, int scopeLevel, ScopedSymbolTable enclosingScope) {
		this.scopeType = scopeName;
		this.scopeLevel = scopeLevel;
		this.returnType = null;
		this.enclosingScope = enclosingScope;
	}
	
	public ScopedSymbolTable(ScopeType scopeType, int scopeLevel, TypeInstanceSymbol returnType, ScopedSymbolTable enclosingScope) {
		this.scopeType = scopeType;
		this.scopeLevel = scopeLevel;
		this.returnType = returnType;
		this.enclosingScope = enclosingScope;
	}
	
	public boolean isReturned() {
		return returned;
	}
	
	public void return_() {
		this.returned = true;
		if (!scopeType.canReturn()) {
			//Enclosing scope should return as well
			enclosingScope.return_();
		}
	}
	
	private void insert(Symbol symbol, Token token, boolean global, boolean allowThis) {
		if (!allowThis && symbol.getName().equals("this")) {
			Main.error("'this' cannot be declared: " + token.getPosition().format());
		}
		Symbol previousSymbol = lookup(symbol.getName(), true);
		if (previousSymbol != null) {
			Main.error("'" + symbol.getName() + "' is already declared: " + token.getPosition().format());
		}
		
		symbol.setScopeLevel(scopeLevel);
		if (global) {
			globalSymbols.put(symbol.getName(), symbol);
		} else {
			symbols.put(symbol.getName(), symbol);
		}
	}
	
	public void insert(Symbol symbol, Token token) {
		insert(symbol, token, false, false);
	}
	
	public void insertGlobal(Symbol symbol, Token token) {
		insert(symbol, token, true, false);
	}
	
	public void insertOperator(OperatorSymbol symbol) {
		symbol.setScopeLevel(scopeLevel);
		operators.add(symbol);
	}
	
	public Symbol lookup(String name, boolean currentScopeOnly) {
		Symbol symbol = symbols.get(name);
		if (symbol == null)
			symbol = globalSymbols.get(name);
		
		if (symbol != null) return symbol;
		if (currentScopeOnly) return null;
		
		if (enclosingScope != null) return enclosingScope.lookup(name, false);
		
		return null;
	}
	
	public OperatorSymbol lookupOperator(Operator operator, List<TypeInstanceSymbol> types, boolean currentScopeOnly) {
		for (OperatorSymbol symbol : operators) {
			boolean flag;
			if (operator == null) {
				flag = true;
			} else {
				flag = symbol.getOperator() == operator;
			}
			
			if (flag && symbol.appliesTo(types)) return symbol;
		}
		
		if (currentScopeOnly) return null;
		if (enclosingScope != null) return enclosingScope.lookupOperator(operator, types, false);
		
		return null;
	}
	
	public boolean isInsideScopeType(ScopeType scopeType) {
		if (this.scopeType == scopeType) return true;
		if (enclosingScope != null) return enclosingScope.isInsideScopeType(scopeType);
		return false;
	}
	
	public TypeInstanceSymbol getReturnType() {
		if (returnType != null) return returnType;
		if (enclosingScope != null) return enclosingScope.getReturnType();
		return null;
	}
	
	public void setReturnType(TypeInstanceSymbol returnType) {
		this.returnType = returnType;
	}
	
	public void initBuiltinSymbols() {
		NativeTypes.addToTable(this);
		NativeOperators.addToTable(this);
		NativeFunctions.addToTable(this);
	}
	
	public void initForStructures(TypeInstanceSymbol structureTypeSymbol) {
		insert(new VariableSymbol("this", structureTypeSymbol), null, false, true);
	}
	
	public Map<String, Symbol> getSymbols() {
		return symbols;
	}
	
	public Map<String, Symbol> getGlobalSymbols() {
		return globalSymbols;
	}
	
	public ScopeType getScopeName() {
		return scopeType;
	}
	
	public int getScopeLevel() {
		return scopeLevel;
	}
	
	public ScopeType getScopeType() {
		return scopeType;
	}
	
	public TypeInstanceSymbol getReturnTypeDirect() {
		return returnType;
	}
	
	public ScopedSymbolTable getEnclosingScope() {
		return enclosingScope;
	}
}
