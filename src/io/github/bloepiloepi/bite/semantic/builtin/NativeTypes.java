package io.github.bloepiloepi.bite.semantic.builtin;

import io.github.bloepiloepi.bite.semantic.scope.ScopedSymbolTable;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.util.List;

public class NativeTypes {
	public static final TypeInstanceSymbol INTEGER_INSTANCE = new TypeInstanceSymbol(TypeSymbol.INTEGER, List.of());
	public static final TypeInstanceSymbol FLOAT_INSTANCE = new TypeInstanceSymbol(TypeSymbol.FLOAT, List.of());
	public static final TypeInstanceSymbol BOOLEAN_INSTANCE = new TypeInstanceSymbol(TypeSymbol.BOOLEAN, List.of());
	public static final TypeInstanceSymbol STRING_INSTANCE = new TypeInstanceSymbol(TypeSymbol.STRING, List.of());
	public static final TypeInstanceSymbol NULL_INSTANCE = new TypeInstanceSymbol(TypeSymbol.NULL, List.of());
	
	public static final TypeInstanceSymbol VOID_INSTANCE = new TypeInstanceSymbol(TypeSymbol.VOID, List.of());
	public static final TypeInstanceSymbol UNKNOWN_INSTANCE = new TypeInstanceSymbol(TypeSymbol.UNKNOWN, List.of());
	
	public static void addToTable(ScopedSymbolTable table) {
		table.insert(TypeSymbol.INTEGER, null);
		table.insert(TypeSymbol.FLOAT, null);
		table.insert(TypeSymbol.STRING, null);
		table.insert(TypeSymbol.BOOLEAN, null);
		table.insert(TypeSymbol.FUNCTION, null);
		table.insert(TypeSymbol.LIST, null);
	}
}
