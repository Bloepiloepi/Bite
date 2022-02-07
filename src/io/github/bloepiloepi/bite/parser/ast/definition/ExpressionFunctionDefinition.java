package io.github.bloepiloepi.bite.parser.ast.definition;

import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.expression.Expression;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;
import io.github.bloepiloepi.bite.parser.ast.statements.Declaration;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.FunctionTypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.ArrayList;
import java.util.List;

public class ExpressionFunctionDefinition extends Expression {
	private final TypeSpecification type;
	private final List<Declaration> parameters;
	private final StatementList statements;
	
	public ExpressionFunctionDefinition(Token token, TypeSpecification type, List<Declaration> parameters, StatementList statements) {
		super(token);
		this.type = type;
		this.parameters = parameters;
		this.statements = statements;
	}
	
	private TypeInstanceSymbol returnType;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		type.analyze();
		
		SemanticAnalyzer.current.newScope(ScopeType.FUNCTION, type.getSymbol());
		
		List<TypeInstanceSymbol> parameterTypes = new ArrayList<>();
		for (Declaration parameter : parameters) {
			parameter.analyze(); //Also puts the parameter in the current scope
			parameterTypes.add(parameter.getType().getSymbol());
		}
		
		returnType = new FunctionTypeInstanceSymbol(TypeSymbol.FUNCTION, List.of(type.getSymbol()), parameterTypes);
		SemanticAnalyzer.current.currentScope.setReturnType(returnType);
		
		statements.analyze();
		
		SemanticAnalyzer.current.previousScope(getToken());
	}
	
	@Override
	public BiteObject<?> getValue() {
		List<String> parameterNames = new ArrayList<>();
		List<TypeInstanceSymbol> parameterTypes = new ArrayList<>();
		
		for (Declaration declaration : parameters) {
			parameterNames.add(declaration.getName());
			parameterTypes.add(declaration.getType().getSymbol());
		}
		
		return BiteObject.builtinObject(new FunctionDefinition(returnType, parameterNames, parameterTypes, statements, CallStack.current().peek()));
	}
}
