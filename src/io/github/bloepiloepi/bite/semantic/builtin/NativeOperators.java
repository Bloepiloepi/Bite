package io.github.bloepiloepi.bite.semantic.builtin;

import io.github.bloepiloepi.bite.parser.ast.expression.Operator;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopedSymbolTable;
import io.github.bloepiloepi.bite.semantic.symbol.GenericTypeSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class NativeOperators {
	private static final List<String> operandNames = List.of("op1", "op2");
	
	public static void addToTable(ScopedSymbolTable table) {
		addToTable(table, Operator.PLUS, List.of(), NativeTypes.INTEGER_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.intValue((Integer) record.getObject("op1", -1).getValue() + (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.PLUS, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() + (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.PLUS, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() + (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.PLUS, List.of(), NativeTypes.INTEGER_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.intValue((Integer) record.getObject("op1", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.PLUS, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.MINUS, List.of(), NativeTypes.INTEGER_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.intValue((Integer) record.getObject("op1", -1).getValue() - (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.MINUS, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() - (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.MINUS, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() - (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.MINUS, List.of(), NativeTypes.INTEGER_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.intValue(-((Integer) record.getObject("op1", -1).getValue())));
		}, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.MINUS, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue(-((Float) record.getObject("op1", -1).getValue())));
		}, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.DIVIDE, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Integer) record.getObject("op1", -1).getValue() / ((Integer) record.getObject("op2", -1).getValue()).floatValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.DIVIDE, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() / (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.DIVIDE, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() / (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.MULTIPLY, List.of(), NativeTypes.INTEGER_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.intValue((Integer) record.getObject("op1", -1).getValue() * (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.MULTIPLY, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() * (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.MULTIPLY, List.of(), NativeTypes.FLOAT_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.floatValue((Float) record.getObject("op1", -1).getValue() * (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.GREATER_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Integer) record.getObject("op1", -1).getValue() > (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.GREATER_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Float) record.getObject("op1", -1).getValue() > (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.GREATER_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Integer) record.getObject("op1", -1).getValue() > (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		addToTable(table, Operator.GREATER_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Float) record.getObject("op1", -1).getValue() > (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.LESS_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Integer) record.getObject("op1", -1).getValue() < (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.LESS_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Float) record.getObject("op1", -1).getValue() < (Integer) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.INTEGER_INSTANCE);
		addToTable(table, Operator.LESS_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Integer) record.getObject("op1", -1).getValue() < (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.INTEGER_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		addToTable(table, Operator.LESS_THAN, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Float) record.getObject("op1", -1).getValue() < (Float) record.getObject("op2", -1).getValue()));
		}, NativeTypes.FLOAT_INSTANCE, NativeTypes.FLOAT_INSTANCE);
		
		addToTable(table, Operator.DOUBLE_AND, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Boolean) record.getObject("op1", -1).getValue() && (Boolean) record.getObject("op2", -1).getValue()));
		}, NativeTypes.BOOLEAN_INSTANCE, NativeTypes.BOOLEAN_INSTANCE);
		
		addToTable(table, Operator.DOUBLE_OR, List.of(), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue((Boolean) record.getObject("op1", -1).getValue() || (Boolean) record.getObject("op2", -1).getValue()));
		}, NativeTypes.BOOLEAN_INSTANCE, NativeTypes.BOOLEAN_INSTANCE);
		
		TypeInstanceSymbol contentGeneric = new TypeInstanceSymbol(new GenericTypeSymbol("contentType"), List.of());
		addToTable(table, Operator.LIST_ACCESS, List.of("contentType"), contentGeneric, record -> {
			Object object = record.getObject("op1", -1).getValue();
			if (object instanceof List list) {
				//TODO exception for out of bounds
				CallStack.current().peek().return_((BiteObject<?>) list.get((Integer) record.getObject("op2", -1).getValue()));
			}
		}, new TypeInstanceSymbol(TypeSymbol.LIST, List.of(contentGeneric)), NativeTypes.INTEGER_INSTANCE);
		
		TypeInstanceSymbol firstGeneric = new TypeInstanceSymbol(new GenericTypeSymbol("first"), List.of());
		TypeInstanceSymbol secondGeneric = new TypeInstanceSymbol(new GenericTypeSymbol("second"), List.of());
		addToTable(table, Operator.EQUALS, List.of("first", "second"), NativeTypes.BOOLEAN_INSTANCE, record -> {
			CallStack.current().peek().return_(BiteObject.booleanValue(record.getObject("op1", -1).equals(record.getObject("op2", -1))));
		}, firstGeneric, secondGeneric);
		
		TypeInstanceSymbol anythingGeneric = new TypeInstanceSymbol(new GenericTypeSymbol("other"), List.of());
		addToTable(table, Operator.PLUS, List.of("other"), NativeTypes.STRING_INSTANCE, record -> {
			Boolean swapped = (Boolean) record.getObject("_swapped", -1).getValue();
			String result;
			if (swapped) {
				result = record.getObject("op2", -1).getValue() + (String) record.getObject("op1", -1).getValue();
			} else {
				result = (String) record.getObject("op1", -1).getValue() + record.getObject("op2", -1).getValue();
			}
			
			CallStack.current().peek().return_(BiteObject.stringValue(result));
		}, NativeTypes.STRING_INSTANCE, anythingGeneric);
	}
	
	private static void addToTable(ScopedSymbolTable table, Operator operator, List<String> generics, TypeInstanceSymbol returnType, Consumer<ActivationRecord> statements, TypeInstanceSymbol... operands) {
		List<TypeInstanceSymbol> staticOperands = Arrays.asList(operands);
		
		OperatorSymbol symbol = new OperatorSymbol(operator, operandNames, staticOperands, returnType, generics, new NativeStatementList(statements));
		symbol.setContext(CallStack.global);
		table.insertOperator(symbol);
	}
}
