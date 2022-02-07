package io.github.bloepiloepi.bite.parser.ast.definition;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.parser.ast.statements.Declaration;
import io.github.bloepiloepi.bite.parser.ast.statements.Statement;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.GenericTypeSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.VariableSymbol;

import java.util.ArrayList;
import java.util.List;

public class OperatorDefinition implements Definition, Statement {
	private final Token token;
	
	private final TypeSpecification type;
	private final Operator operator;
	private final Declaration declaration1;
	private final Declaration declaration2;
	private final ArrayList<Declaration> onlyCall_parameters;
	private final ArrayList<String> generics;
	private final StatementList block;
	
	public OperatorDefinition(Token token, TypeSpecification type, Operator operator, Declaration declaration1, Declaration declaration2, ArrayList<Declaration> onlyCall_parameters, ArrayList<String> generics, StatementList block) {
		this.token = token;
		this.type = type;
		this.operator = operator;
		this.declaration1 = declaration1;
		this.declaration2 = declaration2;
		this.onlyCall_parameters = onlyCall_parameters;
		this.generics = generics;
		this.block = block;
	}
	
	private OperatorSymbol symbol;
	
	@Override
	public void define() {
	
	}
	
	@Override
	public void analyze() {
		List<String> operandNames = new ArrayList<>();
		List<TypeInstanceSymbol> staticOperands = new ArrayList<>();
		
		SemanticAnalyzer.current.newScope(ScopeType.FUNCTION);
		SemanticAnalyzer.current.currentScope.insert(new VariableSymbol("_swapped", NativeTypes.BOOLEAN_INSTANCE), null);
		
		for (String generic : generics) {
			SemanticAnalyzer.current.currentScope.insert(new GenericTypeSymbol(generic), token);
		}
		
		type.analyze();
		SemanticAnalyzer.current.currentScope.setReturnType(type.getSymbol());
		
		int operatorCount = 0;
		if (declaration1 != null) operatorCount++;
		if (declaration2 != null) operatorCount++;
		if (onlyCall_parameters != null) operatorCount += onlyCall_parameters.size();
		
		if (declaration1 != null) {
			declaration1.getType().allowGeneric(false);
			declaration1.analyze(); //Puts in scope
		}
		if (declaration2 != null) {
			declaration2.getType().allowGeneric(false);
			declaration2.analyze(); //Puts in scope
		}
		
		if (onlyCall_parameters != null) {
			for (Declaration declaration : onlyCall_parameters) {
				declaration.getType().allowGeneric(false);
				declaration.analyze(); //Puts in scope
			}
		}
		
		TypeInstanceSymbol symbol1 = declaration1 == null ? null : declaration1.getType().getSymbol();
		TypeInstanceSymbol symbol2 = declaration2 == null ? null : declaration2.getType().getSymbol();
		
		if (operatorCount == 0) {
			Main.error("An operator without operands is not allowed: " + token.getPosition().format());
		} else if (operatorCount == 1 && onlyCall_parameters == null) {
			if (declaration1 == null) {
				Main.error("First operand of operator cannot be empty: " + token.getPosition().format());
				return;
			}
			
			operandNames.add(declaration1.getName());
			staticOperands.add(symbol1);
		} else if (operatorCount == 2 && onlyCall_parameters == null) {
			operandNames.add(declaration1.getName());
			operandNames.add(declaration2.getName());
			staticOperands.add(symbol1);
			staticOperands.add(symbol2);
		} else {
			if (declaration1 == null) {
				Main.error("Call operator must have a first operand: " + token.getPosition().format());
				return;
			}
			
			operandNames.add(declaration1.getName());
			staticOperands.add(symbol1);
			
			for (Declaration declaration : onlyCall_parameters) {
				TypeInstanceSymbol typeSymbol = declaration.getType().getSymbol();
				
				operandNames.add(declaration.getName());
				staticOperands.add(typeSymbol);
			}
		}
		
		for (String generic : generics) {
			//TODO ?
		}
		
		block.analyze();
		
		SemanticAnalyzer.current.previousScope(token);
		
		symbol = new OperatorSymbol(operator, operandNames, staticOperands, type.getSymbol(), generics, block);
		SemanticAnalyzer.current.currentScope.insertOperator(symbol);
	}
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void execute() {
		define(); // ??
		
		symbol.setContext(CallStack.current().peek());
	}
}
