package io.github.bloepiloepi.bite.runtime.stack;

import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.semantic.scope.ScopeType;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class CallStack {
	public static final ActivationRecord global = new ActivationRecord(ScopeType.GLOBAL, 0, null, null);
	private static CallStack current = new CallStack();
	
	private final Deque<ActivationRecord> stack = new ArrayDeque<>(64);
	private final Map<String, BiteObject<?>> exports = new HashMap<>();
	
	public CallStack() {
		stack.push(global);
		stack.push(new ActivationRecord(ScopeType.FILE, 1, global, global));
	}
	
	public void push(ActivationRecord record) {
		stack.push(record);
	}
	
	public ActivationRecord push(ScopeType type, ActivationRecord context) {
		ActivationRecord record = createRecord(type, context);
		stack.push(record);
		return record;
	}
	
	public ActivationRecord createRecord(ScopeType type, ActivationRecord context) {
		return new ActivationRecord(type, context.getScopeLevel() + 1, context, peek());
	}
	
	public ActivationRecord peek() {
		return stack.peek();
	}
	
	public ActivationRecord pop() {
		return stack.pop();
	}
	
	public void addExport(String name, BiteObject<?> object) {
		exports.put(name, object);
	}
	
	public Map<String, BiteObject<?>> getExports() {
		return exports;
	}
	
	public static CallStack current() {
		return current;
	}
	
	public static CallStack runFile(StatementList statements) {
		CallStack previous = current;
		CallStack file = new CallStack();
		current = file;
		statements.run();
		current = previous;
		
		return file;
	}
}
