package io.github.bloepiloepi.bite.parser.ast.definition;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.builtin.NativeTypes;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public class ListDefinition extends Expression {
	private final List<Expression> values;
	
	public ListDefinition(Token token, List<Expression> values) {
		super(token);
		this.values = values;
	}
	
	private TypeInstanceSymbol returnType;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		TypeInstanceSymbol contentType = NativeTypes.UNKNOWN_INSTANCE;
		
		for (Expression expression : values) {
			expression.analyze();
			
			TypeInstanceSymbol expressionReturnType = expression.getReturnType();
			if (!contentType.equals(expressionReturnType)) {
				Main.error("Not the same types in list: " + getToken().getPosition().format());
			} else {
				contentType = expressionReturnType;
			}
		}
		
		returnType = new TypeInstanceSymbol(TypeSymbol.LIST, List.of(contentType));
	}
	
	@Override
	public BiteObject<?> getValue() {
		List<BiteObject<?>> list = new ArrayList<>();
		
		for (Expression expression : values)
			list.add(expression.getValue());
		
		return BiteObject.builtinObject(list);
	}
}
