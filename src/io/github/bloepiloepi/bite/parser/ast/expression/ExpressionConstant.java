package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

public abstract class ExpressionConstant<T> extends Expression {
	private final BiteObject<T> value;
	
	public ExpressionConstant(Token token, BiteObject<T> value) {
		super(token);
		this.value = value;
	}
	
	@Override
	public BiteObject<T> getValue() {
		return value;
	}
	
	public void analyze() {
	
	}
	
	public static class IntegerConstant extends ExpressionConstant<Integer> {
		public IntegerConstant(Token token, Integer value) {
			super(token, BiteObject.intValue(value));
		}
		
		@Override
		public TypeInstanceSymbol getReturnTypeNonValid() {
			return NativeTypes.INTEGER_INSTANCE;
		}
	}
	public static class FloatConstant extends ExpressionConstant<Float> {
		public FloatConstant(Token token, Float value) {
			super(token, BiteObject.floatValue(value));
		}
		
		@Override
		public TypeInstanceSymbol getReturnTypeNonValid() {
			return NativeTypes.FLOAT_INSTANCE;
		}
	}
	public static class StringConstant extends ExpressionConstant<String> {
		public StringConstant(Token token, String value) {
			super(token, BiteObject.stringValue(value));
		}
		
		@Override
		public TypeInstanceSymbol getReturnTypeNonValid() {
			return NativeTypes.STRING_INSTANCE;
		}
	}
	public static class BooleanConstant extends ExpressionConstant<Boolean> {
		public BooleanConstant(Token token, Boolean value) {
			super(token, BiteObject.booleanValue(value));
		}
		
		@Override
		public TypeInstanceSymbol getReturnTypeNonValid() {
			return NativeTypes.BOOLEAN_INSTANCE;
		}
	}
	public static class NullConstant extends ExpressionConstant<Object> {
		public NullConstant(Token token) {
			super(token, null);
		}
		
		@Override
		public TypeInstanceSymbol getReturnTypeNonValid() {
			return NativeTypes.NULL_INSTANCE;
		}
	}
}
