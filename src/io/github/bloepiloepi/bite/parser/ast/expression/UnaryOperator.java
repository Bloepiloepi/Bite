package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.List;

public class UnaryOperator extends Expression {
	private final Expression expression;
	private final Operator operator;
	
	public UnaryOperator(Token token, Expression expression, Operator operator) {
		super(token);
		this.expression = expression;
		this.operator = operator;
	}
	
	private TypeInstanceSymbol returnType;
	private OperatorSymbol symbol;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		expression.analyze();
		TypeInstanceSymbol type = expression.getReturnType(true);
		
		List<TypeInstanceSymbol> operands = List.of(type);
		
		symbol = SemanticAnalyzer.current.currentScope.lookupOperator(operator, operands, false);
		if (symbol == null) {
			Main.error("No such overloaded operator found: " + getToken().getPosition().format());
		}
		assert symbol != null;
		returnType = symbol.getReturnType(operands);
	}
	
	@Override
	public BiteObject<?> getValue() {
		BiteObject<?> object = expression.getValue();
		
		ActivationRecord record = CallStack.current().push(ScopeType.FUNCTION, symbol.getContext());
		String name = symbol.getOperandNames().get(0);
		record.store(name, object);
		
		symbol.getBlock().run();
		
		return CallStack.current().pop().getReturnValue();
	}
}
