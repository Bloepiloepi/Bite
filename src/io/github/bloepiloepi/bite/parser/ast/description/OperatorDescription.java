package io.github.bloepiloepi.bite.parser.ast.description;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.scope.ScopedSymbolTable;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.ArrayList;
import java.util.List;

public class OperatorDescription extends Expression {
	private final Operator operator;
	private final TypeSpecification type1;
	private final TypeSpecification type2;
	private final List<TypeSpecification> onlyCall_types;
	
	public OperatorDescription(Token token, Operator operator, TypeSpecification type1, TypeSpecification type2, List<TypeSpecification> onlyCall_types) {
		super(token);
		this.operator = operator;
		this.type1 = type1;
		this.type2 = type2;
		this.onlyCall_types = onlyCall_types;
	}
	
	private OperatorSymbol symbol;
	
	public OperatorSymbol getSymbol() {
		return symbol;
	}
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return NativeTypes.VOID_INSTANCE;
	}
	
	@Override
	public void analyze() {
		findSymbol(SemanticAnalyzer.current.currentScope);
	}
	
	private void findSymbol(ScopedSymbolTable symbolTable) {
		List<TypeInstanceSymbol> staticOperands = makeOperands();
		
		symbol = symbolTable.lookupOperator(operator, staticOperands, false);
		if (symbol == null) {
			Main.error("No such overloaded operator found: " + getToken().getPosition().format());
		}
	}
	
	public OperatorSymbol findOperatorSymbolIn(List<OperatorSymbol> symbols) {
		List<TypeInstanceSymbol> staticOperands = makeOperands();
		
		for (OperatorSymbol symbol : symbols) {
			if (symbol.getOperator() == operator && symbol.appliesTo(staticOperands)) return symbol;
		}
		
		Main.error("No such overloaded operator found: " + getToken().getPosition().format());
		return null;
	}
	
	private List<TypeInstanceSymbol> makeOperands() {
		List<TypeInstanceSymbol> staticOperands = new ArrayList<>();
		
		int operatorCount = 0;
		if (type1 != null) operatorCount++;
		if (type2 != null) operatorCount++;
		if (onlyCall_types != null) operatorCount += onlyCall_types.size();
		
		if (type1 != null) {
			type1.analyze();
		}
		if (type2 != null) {
			type2.analyze();
		}
		
		TypeInstanceSymbol symbol1 = type1 == null ? null : type1.getSymbol();
		TypeInstanceSymbol symbol2 = type2 == null ? null : type2.getSymbol();
		
		if (operatorCount == 0) {
			Main.error("An operator without operands is not allowed: " + getToken().getPosition().format());
		} else if (operatorCount == 1 && onlyCall_types == null) {
			if (type1 == null) {
				Main.error("First operand of operator cannot be empty: " + getToken().getPosition().format());
			}
			
			staticOperands.add(symbol1);
		} else if (operatorCount == 2 && onlyCall_types == null) {
			staticOperands.add(symbol1);
			staticOperands.add(symbol2);
		} else {
			staticOperands.add(symbol1);
			
			for (TypeSpecification specification : onlyCall_types) {
				TypeInstanceSymbol typeSymbol = specification.getSymbol();
				
				staticOperands.add(typeSymbol);
			}
		}
		
		return staticOperands;
	}
	
	@Override
	public BiteObject<?> getValue() {
		Main.error("Something weird happened");
		return null;
	}
}
