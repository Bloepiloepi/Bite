package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.object.BiteStructure;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

public class InstantiationStatement extends Expression implements Statement {
	private final TypeSpecification type;
	
	public InstantiationStatement(Token token, TypeSpecification type) {
		super(token);
		this.type = type;
	}
	
	private TypeInstanceSymbol returnType;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		type.analyze();
		returnType = type.getSymbol();
	}
	
	@Override
	public BiteObject<?> getValue() {
		BiteStructure object = BiteObject.structure(returnType);
		
		TypeSymbol baseType = returnType.getBaseType();
		ActivationRecord record = CallStack.current().push(ScopeType.STRUCTURE, baseType.getContext());
		
		for (String name : baseType.getSubSymbols().keySet()) {
			object.store(name, BiteObject.empty());
		}
		record.store("this", object);
		
		baseType.getStatements().run();
		
		CallStack.current().pop();
		return object;
	}
	
	@Override
	public void execute() {
		getValue();
	}
}
