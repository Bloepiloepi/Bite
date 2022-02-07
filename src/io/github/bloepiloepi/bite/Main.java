package io.github.bloepiloepi.bite;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.builtin.NativeFunctions;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;

import java.nio.file.Path;

public class Main {
    
    public static void main(String[] args) {
        long before = System.currentTimeMillis();
        StatementList list = SemanticAnalyzer.analyzeFile(Path.of("test/main.bite"));
        long after = System.currentTimeMillis();
        System.out.println("Analyze Time (milliseconds): " + (after - before));
        before = after;
        
        NativeFunctions.addToRecord(CallStack.global);
        list.run();
        
        after = System.currentTimeMillis();
        System.out.println("Run Time (milliseconds): " + (after - before));
    }
    
    public static void error(String error) {
        System.err.println(error);
        System.exit(1);
    }
}
