package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.definition.FunctionDefinition;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.object.BiteStructure;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.List;

public class InstantiationStatement extends Expression implements Statement {
	private final TypeSpecification type;
	private final List<Expression> arguments;
	
	public InstantiationStatement(Token token, TypeSpecification type, List<Expression> arguments) {
		super(token);
		this.type = type;
		this.arguments = arguments;
	}
	
	private TypeInstanceSymbol returnType;
	private List<TypeInstanceSymbol> argumentTypes;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		type.analyze();
		returnType = type.getSymbol();
		
		if (returnType.getBaseType().isPrimitive()) {
			Main.error("Cannot instantiate primitive types: " + getToken().getPosition().format());
		}
		
		argumentTypes = returnType.getBaseType().getConstructorArgumentTypes();
		Call.checkCall(argumentTypes, arguments, getToken());
	}
	
	@Override
	public BiteObject<?> getValue() {
		BiteStructure object = BiteObject.structure(returnType);
		
		TypeSymbol baseType = returnType.getBaseType();
		FunctionDefinition constructor = baseType.getConstructor();
		ActivationRecord record = CallStack.current().createRecord(ScopeType.STRUCTURE, constructor.context());
		
		for (int i = 0; i < arguments.size(); i++) {
			String name = constructor.parameterNames().get(i);
			BiteObject<?> argument = arguments.get(i).getValue().cast(argumentTypes.get(i).getBaseType());
			record.store(name, argument);
		}
		
		CallStack.current().push(record);
		for (String name : baseType.getSubSymbols().keySet()) {
			object.store(name, BiteObject.empty());
		}
		record.store("this", object);
		
		constructor.block().run();
		
		CallStack.current().pop();
		return object;
	}
	
	@Override
	public void execute() {
		getValue();
	}
}
