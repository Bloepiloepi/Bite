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
import io.github.bloepiloepi.bite.semantic.symbol.*;

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
		leftHand.analyze();
		
		TypeInstanceSymbol leftReturnType;
		if (leftHand instanceof Declaration declaration) {
			Variable variable = new Variable(declaration.getToken(), declaration.getName());
			variable.analyze();
			leftReturnType = variable.getReturnType(false);
		} else if (leftHand instanceof Variable variable) {
			scopeLevel = variable.getSymbol().getScopeLevel();
			leftReturnType = leftHand.getReturnType(false);
		} else if (leftHand instanceof BinaryOperator op && op.getOperator() == Operator.DOT) {
			leftReturnType = leftHand.getReturnType(false);
		} else if (leftHand instanceof BinaryOperator op && op.getOperator() == Operator.LIST_ACCESS) {
			leftReturnType = leftHand.getReturnType(true);
		} else {
			Main.error("Cannot assign a value: " + getToken().getPosition().format());
			return;
		}
		if (!leftReturnType.isComplete()) {
			leftReturnType = infer();
		}
		
		TypeInstanceSymbol rightReturnType = rightHand.getReturnType(true);
		List<TypeInstanceSymbol> operands = List.of(leftReturnType, rightReturnType);
		symbol = SemanticAnalyzer.current.currentScope.lookupOperator(Operator.ASSIGN, operands, false);
		if (symbol == null) {
			if (!leftReturnType.equals(rightReturnType)) {
				Main.error("Invalid type, '" + leftReturnType.getName() + "' required: " + rightHand.getToken().getPosition().format());
			}
		} else {
			overloaded = true;
		}
	}
	
	private TypeInstanceSymbol infer() {
		if (rightHand instanceof ExpressionFunctionDefinition func) {
			TypeInstanceSymbol complete = func.getReturnType(true);
			if (leftHand instanceof Variable variable) {
				SemanticAnalyzer.current.currentScope.replace(variable.getSymbol(), Variable.getInferred(variable.getSymbol(), complete));
			} else if (leftHand instanceof Declaration declaration) {
				SemanticAnalyzer.current.currentScope.replace(declaration.getVariableSymbol(), new VariableSymbol(declaration.getName(), complete));
			} else if (leftHand instanceof BinaryOperator op && op.getOperator() == Operator.DOT && op.getRightHand() instanceof Variable variable) {
				TypeInstanceSymbol left = op.getLeftHand().getReturnType(true);
				
				boolean instanced = left.getBaseType().getSubSymbols().containsKey(variable.getName());
				Symbol symbol = (instanced ? left.getBaseType().getSubSymbols() : left.getBaseType().getStaticSubSymbols()).get(variable.getName());
				
				(instanced ? left.getBaseType().getSubSymbols() : left.getBaseType().getStaticSubSymbols()).put(variable.getName(), Variable.getInferred(symbol, complete));
			}
			return complete;
		} else {
			Main.error("Cannot infer type: " + leftHand.getToken().getPosition().format());
			return NativeTypes.VOID_INSTANCE;
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
