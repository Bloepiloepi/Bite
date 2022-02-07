package io.github.bloepiloepi.bite.parser.ast.statements;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.ast.description.OperatorDescription;
import io.github.bloepiloepi.bite.runtime.object.BiteObject;
import io.github.bloepiloepi.bite.runtime.stack.ActivationRecord;
import io.github.bloepiloepi.bite.runtime.stack.CallStack;
import io.github.bloepiloepi.bite.semantic.scope.SemanticAnalyzer;
import io.github.bloepiloepi.bite.semantic.symbol.Symbol;
import io.github.bloepiloepi.bite.semantic.symbol.VariableSymbol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImportStatement implements Statement {
	private final Token token;
	
	private final List<String> importedSymbols;
	private final List<OperatorDescription> importedOperators;
	private final String filename;
	private final boolean importAll;
	
	private File file;
	
	public ImportStatement(Token token, List<String> importedSymbols, List<OperatorDescription> importedOperators, String filename, boolean importAll) {
		this.token = token;
		this.importedSymbols = importedSymbols;
		this.importedOperators = importedOperators;
		this.filename = filename;
		this.importAll = importAll;
	}
	
	private final List<String> importedObjects = new ArrayList<>();
	
	@Override
	public Token getToken() {
		return token;
	}
	
	@Override
	public void execute() {
		SemanticAnalyzer fileAnalyzer = SemanticAnalyzer.analyzersByFile.get(file.toPath());
		
		ActivationRecord record = CallStack.current().peek();
		CallStack fileStack = CallStack.runFile(fileAnalyzer.getStatements());
		if (importAll) {
			fileStack.getExports().forEach(record::store);
		} else {
			for (String name : importedSymbols) {
				record.store(name, fileStack.getExports().getOrDefault(name, BiteObject.empty()));
			}
		}
	}
	
	@Override
	public void analyze() {
		file = new File(SemanticAnalyzer.current.getFilepath().getParent().toFile(), filename);
		
		if (SemanticAnalyzer.currentlyAnalyzing.contains(file.toPath())) {
			Main.error("Recursive imports are not allowed: " + token.getPosition().format());
		}
		
		if (!SemanticAnalyzer.analyzersByFile.containsKey(file.toPath())) {
			SemanticAnalyzer.analyzeFile(file.toPath());
		}
		
		SemanticAnalyzer fileAnalyzer = SemanticAnalyzer.analyzersByFile.get(file.toPath());
		
		if (importAll) {
			fileAnalyzer.getExports().forEach((name, type) -> SemanticAnalyzer.current.currentScope.insert(new VariableSymbol(name, type), token));
			fileAnalyzer.getTypeExports().forEach((name, type) -> SemanticAnalyzer.current.currentScope.insert(type, token));
			fileAnalyzer.getOperatorExports().forEach((symbol) -> SemanticAnalyzer.current.currentScope.insertOperator(symbol));
		} else {
			importedSymbols.forEach((name) -> {
				Symbol symbol = null;
				if (fileAnalyzer.getExports().containsKey(name)) {
					importedObjects.add(name);
					symbol = new VariableSymbol(name, fileAnalyzer.getExports().get(name));
				} else if (fileAnalyzer.getTypeExports().containsKey(name)) {
					symbol = fileAnalyzer.getTypeExports().get(name);
				} else {
					Main.error("Cannot find symbol '" + name + "': " + token.getPosition().format());
				}
				
				SemanticAnalyzer.current.currentScope.insert(symbol, token);
			});
			importedOperators.forEach((description) -> SemanticAnalyzer.current.currentScope.insertOperator(description.findOperatorSymbolIn(fileAnalyzer.getOperatorExports())));
		}
	}
}
