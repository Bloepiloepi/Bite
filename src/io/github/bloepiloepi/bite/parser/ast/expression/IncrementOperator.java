package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.statements.Statement;
import io.github.bloepiloepi.bite.runtime.object.Assignable;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

public class IncrementOperator extends Expression implements Statement {
	private final Expression expression;
	private final boolean after;
	private final boolean plus;
	
	public IncrementOperator(Token token, Expression expression, boolean after, boolean plus) {
		super(token);
		this.expression = expression;
		this.after = after;
		this.plus = plus;
	}
	
	private int scopeLevel = -1;
	private TypeInstanceSymbol type;
	private boolean integer;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return type;
	}
	
	@Override
	public void analyze() {
		expression.analyze();
		
		if (expression instanceof Variable variable) {
			scopeLevel = variable.getSymbol().getScopeLevel();
			type = expression.getReturnType(true);
		} else if (expression instanceof BinaryOperator op && op.getOperator() == Operator.DOT) {
			type = expression.getReturnType(true);
		} else if (expression instanceof BinaryOperator op && op.getOperator() == Operator.LIST_ACCESS) {
			type = expression.getReturnType(true);
		} else {
			Main.error("Cannot assign a value: " + getToken().getPosition().format());
			return;
		}
		
		if (type.equals(NativeTypes.INTEGER_INSTANCE)) {
			integer = true;
		} else if (type.equals(NativeTypes.FLOAT_INSTANCE)) {
			integer = false;
		} else {
			Main.error("Increment operator only applies to numbers: " + getToken().getPosition().format());
		}
	}
	
	@Override
	public BiteObject<?> getValue() {
		BiteObject<?> value = expression.getValue().cast(type.getBaseType());
		BiteObject<?> incrementedValue;
		Number number = (Number) value.getValue();
		
		if (integer) {
			incrementedValue = BiteObject.intValue(number.intValue() + (plus ? 1 : -1));
		} else {
			incrementedValue = BiteObject.floatValue(number.floatValue() + (plus ? 1 : -1));
		}
		
		if (value instanceof Assignable assignable) {
			assignable.storeValue(incrementedValue);
		} else if (expression instanceof Variable variable) {
			CallStack.current().peek().store(variable.getName(), incrementedValue, scopeLevel);
		}
		
		return after ? value : incrementedValue;
	}
	
	@Override
	public void execute() {
		getValue();
	}
}
