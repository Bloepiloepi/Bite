package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.FieldSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.VariableSymbol;

public class Declaration extends Expression implements Statement {
	private final TypeSpecification type;
	private final String name;
	
	private final boolean global;
	private boolean onStructure = false;
	
	public Declaration(TypeSpecification type, String name, boolean global) {
		super(type.getToken());
		this.type = type;
		this.name = name;
		this.global = global;
	}
	
	public TypeSpecification getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setOnStructure(boolean onStructure) {
		this.onStructure = onStructure;
	}
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return NativeTypes.VOID_INSTANCE;
	}
	
	@Override
	public BiteObject<?> getValue() {
		Main.error("Something weird happened");
		return null;
	}
	
	private VariableSymbol variableSymbol;
	
	public VariableSymbol getVariableSymbol() {
		return variableSymbol;
	}
	
	@Override
	public void execute() {
	
	}
	
	@Override
	public void analyze() {
		//TODO also check parent scope in case of control scopes (if, loops)
		if (global && SemanticAnalyzer.current.currentScope.getScopeType() != ScopeType.FILE && !onStructure) {
			Main.error("Global is not allowed here: " + getToken().getPosition().format());
		}
		
		variableSymbol = createVariableSymbol(!global && onStructure);
		
		if (global) {
			if (onStructure) {
				SemanticAnalyzer.current.currentScope.insertGlobal(variableSymbol, getToken());
			} else {
				SemanticAnalyzer.globalScope.insert(variableSymbol, getToken());
			}
		} else {
			SemanticAnalyzer.current.currentScope.insert(variableSymbol, getToken());
		}
	}
	
	private VariableSymbol createVariableSymbol(boolean field) {
		type.allowInfer(true);
		type.analyze();
		TypeInstanceSymbol typeSymbol = type.getSymbol();
		
		if (field) {
			return new FieldSymbol(SemanticAnalyzer.current.currentScope.getScopeLevel(), name, typeSymbol);
		}
		
		return new VariableSymbol(name, typeSymbol);
	}
}
