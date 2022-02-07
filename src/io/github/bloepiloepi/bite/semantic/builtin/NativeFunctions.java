package io.github.bloepiloepi.bite.semantic.builtin;

import io.github.bloepiloepi.bite.parser.ast.definition.FunctionDefinition;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopedSymbolTable;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.VariableSymbol;

import java.util.List;
import java.util.function.Consumer;

public class NativeFunctions {
	public static void addToTable(ScopedSymbolTable table) {
		addToTable(table, "print", NativeTypes.VOID_INSTANCE);
	}
	
	public static void addToRecord(ActivationRecord record) {
		addToRecord(record, "print", NativeTypes.VOID_INSTANCE, contextRecord -> {
			System.out.println(contextRecord.getObject("message", -1).getValue());
		}, List.of("message"), List.of(NativeTypes.STRING_INSTANCE));
	}
	
	private static void addToTable(ScopedSymbolTable table, String name, TypeInstanceSymbol returnType) {
		table.insert(new VariableSymbol(name, new TypeInstanceSymbol(TypeSymbol.FUNCTION, List.of(returnType))), null);
	}
	
	private static void addToRecord(ActivationRecord record, String name, TypeInstanceSymbol returnType, Consumer<ActivationRecord> statements, List<String> parameterNames, List<TypeInstanceSymbol> parameterTypes) {
		record.store(name, BiteObject.builtinObject(new FunctionDefinition(returnType, parameterNames, parameterTypes, new NativeStatementList(statements), CallStack.global)));
	}
}
