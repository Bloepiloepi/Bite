package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.definition.ExpressionFunctionDefinition;
import io.github.bloepiloepi.bite.parser.ast.expression.BinaryOperator;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.parser.ast.expression.Variable;
import io.github.bloepiloepi.bite.runtime.object.Assignable;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.List;

public class Assignment extends Expression implements Statement {
	private final Expression leftHand;
	private final Expression rightHand;
	
	public Assignment(Token token, Expression leftHand, Expression rightHand) {
		super(token);
		this.leftHand = leftHand;
		this.rightHand = rightHand;
		
		if (!(leftHand instanceof Declaration || leftHand instanceof Variable || leftHand instanceof BinaryOperator)) {
			//Non-assignable value
			Main.error("Cannot assign a value to left hand: " + getToken().getPosition().format());
		}
	}
	
	@Override
	public BiteObject<?> getValue() {
		Main.error("Something weird happened");
		return null;
	}
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return NativeTypes.BOOLEAN_INSTANCE;
	}
	
	private OperatorSymbol symbol;
	private int scopeLevel = -1;
	private boolean overloaded = false;
	
	@Override
	public void analyze() {
		rightHand.analyze();
		if (leftHand instanceof Declaration declaration) {
			//Infer the generic for functions
			declaration.getType().allowInfer(true);
			declaration.getType().analyze();
			if (declaration.getType().shouldInfer()) {
				if (rightHand instanceof ExpressionFunctionDefinition func) {
					declaration.getType().infer(func.getReturnType());
				}
			}
		}
		
		leftHand.analyze();
		
		TypeInstanceSymbol leftReturnType;
		if (leftHand instanceof Declaration declaration) {
			Variable variable = new Variable(declaration.getToken(), declaration.getName());
			variable.analyze();
			leftReturnType = variable.getReturnType();
		} else if (leftHand instanceof Variable variable) {
			scopeLevel = variable.getScopeLevel();
			leftReturnType = leftHand.getReturnType();
		} else if (leftHand instanceof BinaryOperator op && op.getOperator() == Operator.DOT) {
			leftReturnType = leftHand.getReturnType();
		} else if (leftHand instanceof BinaryOperator op && op.getOperator() == Operator.LIST_ACCESS) {
			leftReturnType = leftHand.getReturnType();
		} else {
			Main.error("Cannot assign a value: " + getToken().getPosition().format());
			return;
		}
		
		List<TypeInstanceSymbol> operands = List.of(leftReturnType, rightHand.getReturnType());
		symbol = SemanticAnalyzer.current.currentScope.lookupOperator(Operator.ASSIGN, operands, false);
		if (symbol == null) {
			if (!leftReturnType.equals(rightHand.getReturnType())) {
				Main.error("Invalid type, '" + leftReturnType.getName() + "' required: " + rightHand.getToken().getPosition().format());
			}
		} else {
			overloaded = true;
		}
	}
	
	@Override
	public void execute() {
		BiteObject<?> left = leftHand instanceof Declaration ? null : leftHand.getValue();
		
		//Calculate value to store
		BiteObject<?> value;
		if (!overloaded) {
			value = rightHand.getValue();
		} else {
			BiteObject<?> right = rightHand.getValue();
			
			ActivationRecord record = CallStack.current().push(ScopeType.FUNCTION, symbol.getContext());
			String leftName = symbol.getOperandNames().get(0);
			String rightName = symbol.getOperandNames().get(1);
			record.store(leftName, left);
			record.store(rightName, right);
			
			symbol.getBlock().run();
			
			value = CallStack.current().pop().getReturnValue();
		}
		
		if (left instanceof Assignable assignable) {
			assignable.storeValue(rightHand.getValue());
		} else {
			String name = null;
			if (leftHand instanceof Variable variable) {
				name = variable.getName();
			} else if (leftHand instanceof Declaration declaration) {
				name = declaration.getName();
			}
			
			CallStack.current().peek().store(name, value, scopeLevel);
		}
	}
}
