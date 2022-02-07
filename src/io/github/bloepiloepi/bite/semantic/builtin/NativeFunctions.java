package io.github.bloepiloepi.bite.semantic.builtin;

import io.github.bloepiloepi.bite.parser.ast.definition.FunctionDefinition;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.ScopedSymbolTable;
import io.github.bloepiloepi.bite.semantic.symbol.FunctionTypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.VariableSymbol;

import java.util.List;
import java.util.function.Consumer;

public class NativeFunctions {
	public static void addToTable(ScopedSymbolTable table) {
		addToTable(table, "print", NativeTypes.VOID_INSTANCE, List.of(NativeTypes.UNKNOWN_INSTANCE));
	}
	
	public static void addToRecord(ActivationRecord record) {
		addToRecord(record, "print", contextRecord -> {
			System.out.println(contextRecord.getObject("message", -1).getValue());
		}, List.of("message"));
	}
	
	private static void addToTable(ScopedSymbolTable table, String name, TypeInstanceSymbol returnType, List<TypeInstanceSymbol> parameterTypes) {
		table.insert(new VariableSymbol(name, new FunctionTypeInstanceSymbol(TypeSymbol.FUNCTION, List.of(returnType), parameterTypes)), null);
	}
	
	private static void addToRecord(ActivationRecord record, String name, Consumer<ActivationRecord> statements, List<String> parameterNames) {
		record.store(name, BiteObject.builtinObject(new FunctionDefinition(parameterNames, new NativeStatementList(statements), CallStack.global)));
	}
}
