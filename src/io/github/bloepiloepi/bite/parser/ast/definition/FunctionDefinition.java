package io.github.bloepiloepi.bite.parser.ast.definition;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;

import java.util.List;

public record FunctionDefinition(List<String> parameterNames,
                                 StatementList block,
                                 ActivationRecord context) {
}
