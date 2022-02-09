package io.github.bloepiloepi.bite.parser.ast.expression;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.object.BiteStructure;
import io.github.bloepiloepi.bite.runtime.object.StructureField;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.*;

import java.util.List;

public class BinaryOperator extends Expression {
	private final Expression leftHand;
	private final Expression rightHand;
	private final Operator operator;
	
	public BinaryOperator(Token token, Expression leftHand, Expression rightHand, Operator operator) {
		super(token);
		this.leftHand = leftHand;
		this.rightHand = rightHand;
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public Expression getLeftHand() {
		return leftHand;
	}
	
	public Expression getRightHand() {
		return rightHand;
	}
	
	private TypeInstanceSymbol returnType;
	private OperatorSymbol symbol;
	private boolean operandsSwapped;
	
	@Override
	public TypeInstanceSymbol getReturnTypeNonValid() {
		return returnType;
	}
	
	@Override
	public void analyze() {
		if (operator != Operator.DOT) {
			leftHand.analyze();
			rightHand.analyze();
			
			TypeInstanceSymbol left = leftHand.getReturnType(true);
			TypeInstanceSymbol right = rightHand.getReturnType(true);
			
			List<TypeInstanceSymbol> operands = List.of(left, right);
			symbol = SemanticAnalyzer.current.currentScope.lookupOperator(operator, operands, false);
			
			if (symbol != null) {
				returnType = symbol.getReturnType(operands);
				operandsSwapped = symbol.isOperandsSwapped();
			} else {
				Main.error("No such overloaded operator found: " + getToken().getPosition().format());
			}
		} else {
			if (!(rightHand instanceof Variable right)) {
				Main.error("Access operator only applies to variables on right hand: " + getToken().getPosition().format());
				return;
			}
			
			leftHand.analyze();
			
			TypeInstanceSymbol left = leftHand.getReturnType(true);
			
			boolean instanced = left.getBaseType().getSubSymbols().containsKey(right.getName());
			if (!instanced && !left.getBaseType().getStaticSubSymbols().containsKey(right.getName())) {
				Main.error("Variable '" + right.getName() + "' does not exist on objects of type '" + left.getName() + "': " + right.getToken().getPosition().format());
			}
			
			Symbol symbol = (instanced ? left.getBaseType().getSubSymbols() : left.getBaseType().getStaticSubSymbols()).get(right.getName());
			
			returnType = symbol.getType().getReal(left);
		}
	}
	
	@Override
	public BiteObject<?> getValue() {
		if (operator == Operator.DOT) {
			BiteObject<?> obj = leftHand.getValue();
			BiteStructure left = (BiteStructure) obj;
			Variable variable = (Variable) rightHand;
			
			return new StructureField(left, variable.getName());
		} else {
			//No casting here because operators are exact
			BiteObject<?> left = leftHand.getValue();
			BiteObject<?> right = rightHand.getValue();
			
			ActivationRecord record = CallStack.current().push(ScopeType.FUNCTION, symbol.getContext());
			String leftName = symbol.getOperandNames().get(operandsSwapped ? 1 : 0);
			String rightName = symbol.getOperandNames().get(operandsSwapped ? 0 : 1);
			record.store(leftName, left);
			record.store(rightName, right);
			record.store("_swapped", BiteObject.booleanValue(operandsSwapped));
			
			symbol.getBlock().run();
			
			return CallStack.current().pop().getReturnValue();
		}
	}
}
