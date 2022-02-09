package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.definition.FunctionDefinition;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.FunctionTypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.ArrayList;
import java.util.List;

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
	private List<TypeInstanceSymbol> argumentTypes;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		expression.analyze();
		TypeInstanceSymbol type = expression.getReturnType(true);
		
		if (type instanceof FunctionTypeInstanceSymbol func) {
			function = true;
			returnType = type.getGenerics().get(0);
			
			argumentTypes = func.getParameterTypes();
			if (arguments.size() > argumentTypes.size()) {
				Main.error("Too many arguments: " + getToken().getPosition().format());
			} else if (arguments.size() < argumentTypes.size()) {
				Main.error("Not enough arguments: " + getToken().getPosition().format());
			}
			
			for (int i = 0; i < arguments.size(); i++) {
				Expression argument = arguments.get(i);
				argument.analyze();
				TypeInstanceSymbol argumentType = argument.getReturnType(true);
				TypeInstanceSymbol required = argumentTypes.get(i);
				
				if (!argumentType.equals(required)) {
					Main.error("Invalid type, '" + required.getName() + "' required: " + argument.getToken().getPosition().format());
				}
			}
			
			return;
		}
		
		ArrayList<TypeInstanceSymbol> operands = new ArrayList<>();
		operands.add(type);
		
		for (Expression argument : arguments) {
			argument.analyze();
			TypeInstanceSymbol argumentType = argument.getReturnType(true);
			
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
				BiteObject<?> argument = arguments.get(i).getValue().cast(argumentTypes.get(i).getBaseType());
				record.store(name, argument);
			}
			
			CallStack.current().push(record);
			functionDefinition.block().run();
		} else {
			//Call operator
			BiteObject<?> object = expression.getValue();
			ActivationRecord record = CallStack.current().createRecord(ScopeType.FUNCTION, symbol.getContext());
			
			//No casting here because operators are exact
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
