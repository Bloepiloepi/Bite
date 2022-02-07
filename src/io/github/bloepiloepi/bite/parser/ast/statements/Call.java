package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.definition.ExpressionFunctionDefinition;
import io.github.bloepiloepi.bite.parser.ast.definition.FunctionDefinition;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.ArrayList;

public class Call extends Expression implements Statement {
	private final Expression expression;
	private final ArrayList<Expression> arguments;
	
	public Call(Token token, Expression expression, ArrayList<Expression> arguments) {
		super(token);
		this.expression = expression;
		this.arguments = arguments;
	}
	
	private TypeInstanceSymbol returnType;
	private OperatorSymbol symbol;
	private boolean function = false;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		expression.analyze();
		TypeInstanceSymbol type = expression.getReturnType();
		
		if (type.getBaseType().equals(TypeSymbol.FUNCTION)) {
			function = true;
			returnType = type.getGenerics().get(0);
			
			for (Expression argument : arguments) {
				argument.analyze();
				TypeInstanceSymbol argumentType = argument.getReturnType();
				
				//TODO type check arguments
			}
			
			return;
		}
		
		ArrayList<TypeInstanceSymbol> operands = new ArrayList<>();
		operands.add(type);
		
		for (Expression argument : arguments) {
			argument.analyze();
			TypeInstanceSymbol argumentType = argument.getReturnType();
			
			operands.add(argumentType);
		}
		
		symbol = SemanticAnalyzer.current.currentScope.lookupOperator(Operator.CALL, operands, false);
		if (symbol == null) {
			Main.error("No such overloaded operator found: " + getToken().getPosition().format());
			return;
		}
		
		returnType = symbol.getReturnType(operands);
	}
	
	@Override
	public BiteObject<?> getValue() {
		if (function) {
			FunctionDefinition functionDefinition = (FunctionDefinition) expression.getValue().getValue();
			ActivationRecord record = CallStack.current().createRecord(ScopeType.FUNCTION, functionDefinition.context());
			
			for (int i = 0; i < arguments.size(); i++) {
				String name = functionDefinition.parameterNames().get(i);
				BiteObject<?> argument = arguments.get(i).getValue();
				record.store(name, argument);
			}
			
			CallStack.current().push(record);
			functionDefinition.block().run();
		} else {
			//Call operator
			BiteObject<?> object = expression.getValue();
			ActivationRecord record = CallStack.current().createRecord(ScopeType.FUNCTION, symbol.getContext());
			
			for (int i = 0; i < arguments.size(); i++) {
				String name = symbol.getOperandNames().get(i);
				BiteObject<?> argument = i > 0 ? arguments.get(i).getValue() : object;
				record.store(name, argument);
			}
			
			CallStack.current().push(record);
			symbol.getBlock().run();
		}
		
		return CallStack.current().pop().getReturnValue();
	}
	
	@Override
	public void execute() {
		getValue();
	}
}
