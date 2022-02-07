package io.github.bloepiloepi.bite.parser.ast.definition;

import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.parser.ast.statements.Declaration;
import io.github.bloepiloepi.bite.parser.ast.statements.Statement;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.object.BiteStructure;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.GenericTypeSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public class StructureDefinition implements Definition, Statement {
	private final Token token;
	
	private final List<Declaration> declarations;
	private final StatementList statements;
	
	private final String name;
	private final List<String> generics;
	
	public StructureDefinition(Token token, List<Declaration> declarations, StatementList statements, String name, List<String> generics) {
		this.token = token;
		this.declarations = declarations;
		this.statements = statements;
		this.name = name;
		this.generics = generics;
	}
	
	private TypeSymbol type;
	
	@Override
	public void define() {
	
	}
	
	@Override
	public void analyze() {
		SemanticAnalyzer.current.newScope(ScopeType.STRUCTURE);
		
		for (String generic : generics) {
			SemanticAnalyzer.current.currentScope.insert(new GenericTypeSymbol(generic), token);
		}
		for (Declaration declaration : declarations) {
			declaration.analyze();
		}
		
		type = new TypeSymbol(name, SemanticAnalyzer.current.currentScope.getSymbols(), SemanticAnalyzer.current.currentScope.getGlobalSymbols(), statements, generics);
		SemanticAnalyzer.current.currentScope.getEnclosingScope().insert(type, token);
		
		List<TypeInstanceSymbol> genericSymbols = new ArrayList<>();
		for (String generic : generics) {
			genericSymbols.add(new TypeInstanceSymbol(new GenericTypeSymbol(generic), List.of()));
		}
		SemanticAnalyzer.current.currentScope.initForStructures(new TypeInstanceSymbol(type, genericSymbols));
		
		statements.analyze();
		
		SemanticAnalyzer.current.previousScope(token);
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void execute() {
		define(); // ??
		
		ActivationRecord record = CallStack.current().peek();
		BiteStructure staticStructure = BiteObject.structure(type.getType());
		for (String name : type.getStaticSubSymbols().keySet()) {
			staticStructure.store(name, BiteObject.empty());
		}
		record.store(name, staticStructure);
		
		type.setContext(record);
	}
}
