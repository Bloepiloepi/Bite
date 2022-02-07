package io.github.bloepiloepi.bite.parser.ast.definition;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;

import java.util.List;

public record FunctionDefinition(TypeInstanceSymbol returnType,
                                 List<String> parameterNames,
                                 List<TypeInstanceSymbol> parameterTypes,
                                 StatementList block,
                                 ActivationRecord context) {
}
