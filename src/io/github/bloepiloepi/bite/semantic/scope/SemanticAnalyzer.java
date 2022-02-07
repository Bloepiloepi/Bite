package io.github.bloepiloepi.bite.semantic.scope;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Lexer;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.parser.Parser;
import io.github.bloepiloepi.bite.parser.ast.AST;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.semantic.symbol.OperatorSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeInstanceSymbol;
import io.github.bloepiloepi.bite.semantic.symbol.TypeSymbol;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SemanticAnalyzer {
	public static ScopedSymbolTable globalScope = null;
	
	public static HashMap<Path, SemanticAnalyzer> analyzersByFile = new HashMap<>();
	public static ArrayList<Path> currentlyAnalyzing = new ArrayList<>();
	public static SemanticAnalyzer current = null;
	
	public static StatementList analyzeFile(Path path) {
		StringBuilder source = new StringBuilder();
		
		try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
			stream.forEach(s -> source.append(s).append("\n"));
		} catch (IOException e) {
			Main.error("Could not read file '" + path + "': " + e);
		}
		
		Lexer lexer = new Lexer(path.getFileName().toString(), source.toString());
		Parser parser = new Parser(lexer);
		StatementList tree = parser.parse();
		SemanticAnalyzer analyzer = new SemanticAnalyzer(path, tree, parser.getEof());
		analyzersByFile.put(path, analyzer);
		currentlyAnalyzing.add(path);
		
		if (current == null) {
			globalScope = new ScopedSymbolTable(ScopeType.GLOBAL, 0, null);
			globalScope.initBuiltinSymbols();
			
			current = analyzer;
		}
		
		SemanticAnalyzer previous = current;
		current = analyzer;
		analyzer.analyze();
		current = previous;
		
		currentlyAnalyzing.remove(path);
		
		return tree;
	}
	
	private final Path filepath;
	private final StatementList statements;
	
	private final Map<String, TypeInstanceSymbol> exports = new HashMap<>();
	private final Map<String, TypeSymbol> typeExports = new HashMap<>();
	private final ArrayList<OperatorSymbol> operatorExports = new ArrayList<>();
	
	public ScopedSymbolTable currentScope = null;
	public ScopedSymbolTable fileScope = null;
	
	private final Token eof;
	
	public SemanticAnalyzer(Path filepath, StatementList statements, Token eof) {
		this.filepath = filepath;
		this.statements = statements;
		this.eof = eof;
	}
	
	public StatementList getStatements() {
		return statements;
	}
	
	public void analyze() {
		initializeNewFile();
		statements.analyze();
		previousScope(eof);
	}
	
	public void initializeNewFile() {
		currentScope = new ScopedSymbolTable(ScopeType.FILE, 1, globalScope);
		fileScope = currentScope;
	}
	
	public void newScope(ScopeType type) {
		currentScope = new ScopedSymbolTable(type, currentScope.getScopeLevel() + 1, currentScope);
	}
	
	public void newScope(ScopeType type, TypeInstanceSymbol returnType) {
		currentScope = new ScopedSymbolTable(type, currentScope.getScopeLevel() + 1, returnType, currentScope);
	}
	
	public void previousScope(Token endToken) {
		//TODO check if all paths return a value
		//if (currentScope.getReturnTypeDirect() == null || currentScope.getReturnType().getBaseType().equals(TypeSymbol.NONE)) {
			currentScope = currentScope.getEnclosingScope();
		//} else {
		//	Main.error("Missing return statement: " + endToken.getPosition().format());
		//}
	}
	
	public Path getFilepath() {
		return filepath;
	}
	
	public void addExport(String name, TypeInstanceSymbol type) {
		exports.put(name, type);
	}
	
	public void addExport(String name, TypeSymbol type) {
		typeExports.put(name, type);
	}
	
	public void addExport(OperatorSymbol symbol) {
		operatorExports.add(symbol);
	}
	
	public Map<String, TypeInstanceSymbol> getExports() {
		return exports;
	}
	
	public Map<String, TypeSymbol> getTypeExports() {
		return typeExports;
	}
	
	public ArrayList<OperatorSymbol> getOperatorExports() {
		return operatorExports;
	}
}
