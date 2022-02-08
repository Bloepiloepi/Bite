package io.github.bloepiloepi.bite.semantic.symbol;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OperatorSymbol extends Symbol {
	private final Operator operator;
	private final List<String> operandNames;
	private final List<TypeInstanceSymbol> staticOperands;
	private final List<String> generics;
	private final StatementList block;
	
	private ActivationRecord context;
	
	public OperatorSymbol(Operator operator, List<String> operandNames, List<TypeInstanceSymbol> staticOperands, TypeInstanceSymbol type, List<String> generics, StatementList block) {
		super(null, type);
		this.operator = operator;
		this.operandNames = operandNames;
		this.staticOperands = staticOperands;
		this.generics = generics;
		this.block = block;
	}
	
	public Operator getOperator() {
		return operator;
	}
	
	public List<String> getOperandNames() {
		return operandNames;
	}
	
	public List<TypeInstanceSymbol> getOperands() {
		return staticOperands;
	}
	
	public List<String> getGenerics() {
		return generics;
	}
	
	public ActivationRecord getContext() {
		return context;
	}
	
	public void setContext(ActivationRecord context) {
		this.context = context;
	}
	
	public TypeInstanceSymbol getReturnType(List<TypeInstanceSymbol> operands) {
		return getType().resolveGenerics(name -> getGenericValue(name, operands));
	}
	
	private TypeInstanceSymbol getGenericValue(String name, List<TypeInstanceSymbol> operands) {
		return getGenericValue(name, staticOperands, operands);
	}
	
	private TypeInstanceSymbol getGenericValue(String name, List<TypeInstanceSymbol> formalOperands, List<TypeInstanceSymbol> operands) {
		for (int i = 0; i < formalOperands.size(); i++) {
			TypeInstanceSymbol formalSymbol = formalOperands.get(i);
			TypeInstanceSymbol symbol = operands.get(i);
			if (formalSymbol.getBaseType().isGeneric() && formalSymbol.getName().equals(name)) {
				return operands.get(i);
			} else {
				TypeInstanceSymbol result = getGenericValue(name, formalSymbol.getGenerics(), symbol.getGenerics());
				if (result != null)
					return result;
			}
		}
		
		return null;
	}
	
	private boolean operandsSwapped;
	
	public boolean isOperandsSwapped() {
		return operandsSwapped;
	}
	
	public boolean appliesTo(List<TypeInstanceSymbol> toTest) {
		Map<String, TypeInstanceSymbol> genericReplacements = new HashMap<>();
		
		if (staticOperands.size() != toTest.size())
			return false;
		
		if (operator.hasStaticOperands() || staticOperands.size() == 1) {
			for (int i = 0; i < toTest.size(); i++) {
				if (!toTest.get(i).equalsUnGenerified(staticOperands.get(i), genericReplacements)) return false;
				genericReplacements.putAll(toTest.get(i).getGenericReplacements());
			}
			
			return true;
		} else {
			boolean flag;
			
			//This checks if the operands equal, but maybe they are swapped
			operandsSwapped = !toTest.get(0).equalsUnGenerified(staticOperands.get(0), genericReplacements);
			genericReplacements.putAll(toTest.get(0).getGenericReplacements());
			
			if (!operandsSwapped) {
				flag = toTest.get(1).equalsUnGenerified(staticOperands.get(1), genericReplacements);
				genericReplacements.putAll(toTest.get(1).getGenericReplacements());
			} else {
				flag = toTest.get(0).equalsUnGenerified(staticOperands.get(1), genericReplacements);
				genericReplacements.putAll(toTest.get(0).getGenericReplacements());
				
				//TODO what???
				toTest.get(1).equalsUnGenerified(staticOperands.get(0), genericReplacements);
				flag = flag && toTest.get(1).equalsUnGenerified(staticOperands.get(0), genericReplacements);
				genericReplacements.putAll(toTest.get(1).getGenericReplacements());
			}
			
			return flag;
		}
	}
	
	public StatementList getBlock() {
		return block;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OperatorSymbol symbol)) return false;
		return Objects.equals(operator, symbol.operator) &&
				Objects.equals(staticOperands, symbol.staticOperands) &&
				Objects.equals(generics, symbol.generics);
	}
}
