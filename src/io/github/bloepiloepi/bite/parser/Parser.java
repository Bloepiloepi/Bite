package io.github.bloepiloepi.bite.parser;

import io.github.bloepiloepi.bite.Main;
import io.github.bloepiloepi.bite.lexer.Lexer;
import io.github.bloepiloepi.bite.lexer.TextPosition;
import io.github.bloepiloepi.bite.lexer.Token;
import io.github.bloepiloepi.bite.lexer.TokenType;
import io.github.bloepiloepi.bite.parser.ast.StatementList;
import io.github.bloepiloepi.bite.parser.ast.definition.*;
import io.github.bloepiloepi.bite.parser.ast.description.OperatorDescription;
import io.github.bloepiloepi.bite.parser.ast.expression.*;
import io.github.bloepiloepi.bite.parser.ast.logic.ForStatement;
import io.github.bloepiloepi.bite.parser.ast.logic.IfStatement;
import io.github.bloepiloepi.bite.parser.ast.logic.LogicStatement;
import io.github.bloepiloepi.bite.parser.ast.logic.WhileStatement;
import io.github.bloepiloepi.bite.parser.ast.statements.*;
import io.github.bloepiloepi.bite.parser.ast.types.TypeSpecification;

import java.util.ArrayList;
import java.util.List;

public class Parser {
	private final Lexer lexer;
	private Token currentToken;
	private Token lastToken;
	
	public Parser(Lexer lexer) {
		this.lexer = lexer;
		this.currentToken = lexer.getNextToken();
		this.lastToken = currentToken;
	}
	
	private void eat(TokenType type) {
		if (currentToken.getType() == type) {
			lastToken = currentToken;
			currentToken = lexer.getNextToken();
		} else {
			Main.error("Unexpected token '" + currentToken.getValue() + "' at " + currentToken.getPosition().format());
		}
	}
	
	private Expression variable() {
		Token token = currentToken;
		eat(TokenType.IDENTIFIER);
		
		return new Variable(token, token.getValue());
	}
	
	private Expression value() {
		String value = currentToken.getValue();
		Token token = currentToken;
		
		if (currentToken.getType() == TokenType.INTEGER_CONST) {
			eat(TokenType.INTEGER_CONST);
			return new ExpressionConstant.IntegerConstant(token, Integer.parseInt(value));
		} else if (currentToken.getType() == TokenType.FLOAT_CONST) {
			eat(TokenType.FLOAT_CONST);
			return new ExpressionConstant.FloatConstant(token, Float.parseFloat(value));
		} else if (currentToken.getType() == TokenType.STRING) {
			eat(TokenType.STRING);
			return new ExpressionConstant.StringConstant(token, value);
		} else if (currentToken.getType() == TokenType.TRUE) {
			eat(TokenType.TRUE);
			return new ExpressionConstant.BooleanConstant(token, true);
		} else if (currentToken.getType() == TokenType.FALSE) {
			eat(TokenType.FALSE);
			return new ExpressionConstant.BooleanConstant(token, false);
		} else if (currentToken.getType() == TokenType.NULL) {
			eat(TokenType.NULL);
			return new ExpressionConstant.NullConstant(token);
		} else if (currentToken.getType() == TokenType.LPAREN) {
			eat(TokenType.LPAREN);
			Expression expression = expression();
			eat(TokenType.RPAREN);
			return expression;
		} else if (currentToken.getType() == TokenType.NEW) {
			return instantiation();
		} else if (currentToken.getType() == TokenType.IDENTIFIER) {
			if (lexer.peekToken(1).getType() == TokenType.GENERIC) {
				return typeSpecification(false);
			}
			
			return variable();
		} else if (currentToken.getType() == TokenType.SQUARE_BRACE_OPEN) {
			return list();
		} else {
			return function();
		}
	}
	
	private Expression factor() {
		Expression expression;
		
		Token token = currentToken;
		if (currentToken.getType() == TokenType.PLUS) {
			eat(TokenType.PLUS);
			expression = new UnaryOperator(token, factor(), Operator.PLUS);
		} else if (currentToken.getType() == TokenType.MINUS) {
			eat(TokenType.MINUS);
			expression = new UnaryOperator(token, factor(), Operator.MINUS);
		} else if (currentToken.getType() == TokenType.DOUBLE_PLUS) {
			eat(TokenType.DOUBLE_PLUS);
			expression = new IncrementOperator(token, factor(), false, true);
		} else if (currentToken.getType() == TokenType.DOUBLE_MINUS) {
			eat(TokenType.DOUBLE_MINUS);
			expression = new IncrementOperator(token, factor(), false, false);
		} else {
			expression = value();
		}
		
		if (currentToken.getType() == TokenType.DOUBLE_PLUS) {
			expression = new IncrementOperator(currentToken, expression, true, true);
			eat(TokenType.DOUBLE_PLUS);
		} else if (currentToken.getType() == TokenType.DOUBLE_MINUS) {
			expression = new IncrementOperator(currentToken, expression, true, false);
			eat(TokenType.DOUBLE_MINUS);
		}
		
		return expression;
	}
	
	private Expression dot() {
		Expression expression = factor();
		
		if (lastToken.getType() != TokenType.BRACE_CLOSE && currentToken.getType() == TokenType.IDENTIFIER) {
			Token token = currentToken;
			eat(TokenType.IDENTIFIER);
			
			if (expression instanceof Variable variable) {
				return new Declaration(new TypeSpecification(token, variable.getName()), token.getValue(), false);
			} else if (expression instanceof TypeSpecification) {
				return new Declaration((TypeSpecification) expression, token.getValue(), false);
			}
		}
		
		while (currentToken.getType() == TokenType.DOT || currentToken.getType() == TokenType.LPAREN || currentToken.getType() == TokenType.SQUARE_BRACE_OPEN) {
			Token token = currentToken;
			
			if (currentToken.getType() == TokenType.DOT) {
				eat(TokenType.DOT);
				expression = new BinaryOperator(token, expression, variable(), Operator.DOT);
			} else if (currentToken.getType() == TokenType.LPAREN) {
				expression = new Call(currentToken, expression, argumentList());
			} else {
				eat(TokenType.SQUARE_BRACE_OPEN);
				expression = new BinaryOperator(token, expression, expression(), Operator.LIST_ACCESS);
				eat(TokenType.SQUARE_BRACE_CLOSE);
			}
		}
		
		return expression;
	}
	
	private Expression term() {
		Expression expression = dot();
		
		while (currentToken.getType() == TokenType.DIVIDE || currentToken.getType() == TokenType.MULTIPLY) {
			Operator operator;
			Token token = currentToken;
			
			if (currentToken.getType() == TokenType.DIVIDE) {
				eat(TokenType.DIVIDE);
				operator = Operator.DIVIDE;
			} else {
				eat(TokenType.MULTIPLY);
				operator = Operator.MULTIPLY;
			}
			
			expression = new BinaryOperator(token, expression, dot(), operator);
		}
		
		return expression;
	}
	
	private Expression sum() {
		Expression expression = term();
		
		while (currentToken.getType() == TokenType.PLUS || currentToken.getType() == TokenType.MINUS) {
			Operator operator;
			Token token = currentToken;
			
			if (currentToken.getType() == TokenType.PLUS) {
				eat(TokenType.PLUS);
				operator = Operator.PLUS;
			} else {
				eat(TokenType.MINUS);
				operator = Operator.MINUS;
			}
			
			expression = new BinaryOperator(token, expression, term(), operator);
		}
		
		return expression;
	}
	
	private Expression boolExpression() {
		Expression expression = sum();
		
		while (currentToken.getType() == TokenType.DOUBLE_AND || currentToken.getType() == TokenType.DOUBLE_OR) {
			Operator operator;
			Token token = currentToken;
			
			if (currentToken.getType() == TokenType.DOUBLE_AND) {
				eat(TokenType.DOUBLE_AND);
				operator = Operator.DOUBLE_AND;
			} else {
				eat(TokenType.DOUBLE_OR);
				operator = Operator.DOUBLE_OR;
			}
			
			expression = new BinaryOperator(token, expression, sum(), operator);
		}
		
		return expression;
	}
	
	private Expression expression() {
		Expression expression = boolExpression();
		
		while (currentToken.getType() == TokenType.EQUALS || currentToken.getType() == TokenType.GREATER_THAN || currentToken.getType() == TokenType.LESS_THAN) {
			Operator operator;
			Token token = currentToken;
			
			if (currentToken.getType() == TokenType.EQUALS) {
				eat(TokenType.EQUALS);
				operator = Operator.EQUALS;
			} else if (currentToken.getType() == TokenType.GREATER_THAN) {
				eat(TokenType.GREATER_THAN);
				operator = Operator.GREATER_THAN;
			} else {
				eat(TokenType.LESS_THAN);
				operator = Operator.LESS_THAN;
			}
			
			expression = new BinaryOperator(token, expression, boolExpression(), operator);
		}
		
		return expression;
	}
	
	private Expression assignExpression() {
		Expression leftHand = expression();
		
		if (isAssignToken(currentToken.getType())) {
			Token token = currentToken;
			eat(currentToken.getType());
			Expression rightHand = expression();
			if (token.getType() != TokenType.ASSIGN) {
				rightHand = new BinaryOperator(token, leftHand, rightHand, getAssignOperator(token.getType()));
			}
			
			leftHand = new Assignment(token, leftHand, rightHand);
		}
		
		return leftHand;
	}
	
	private Statement declarationOrAssignment() {
		Token peekToken = lexer.peekToken(1);
		
		int secondPeek = 2;
		if (currentToken.getType() == TokenType.GLOBAL) {
			secondPeek = 3;
		}
		
		if (peekToken.getType() == TokenType.DOT || isAssignToken(peekToken.getType()) || lexer.peekToken(secondPeek).getType() == TokenType.ASSIGN) {
			return assignment();
		} else {
			return declaration();
		}
	}
	
	private IfStatement ifStatement() {
		Token token = currentToken;
		Token elseToken = null;
		
		eat(TokenType.IF);
		eat(TokenType.LPAREN);
		Expression expression = expression();
		eat(TokenType.RPAREN);
		
		StatementList block = block();
		StatementList elseBlock = null;
		IfStatement elseIf = null;
		
		if (currentToken.getType() == TokenType.ELSE) {
			elseToken = currentToken;
			eat(TokenType.ELSE);
			
			if (currentToken.getType() == TokenType.IF) {
				elseIf = ifStatement();
			} else {
				elseBlock = block();
			}
		}
		
		return new IfStatement(token, elseToken, expression, block, elseBlock, elseIf);
	}
	
	private WhileStatement whileStatement() {
		Token token = currentToken;
		
		eat(TokenType.WHILE);
		eat(TokenType.LPAREN);
		Expression expression = expression();
		eat(TokenType.RPAREN);
		return new WhileStatement(token, expression, block());
	}
	
	private ForStatement forStatement() {
		Token token = currentToken;
		
		eat(TokenType.FOR);
		eat(TokenType.LPAREN);
		
		TextPosition position = currentToken.getPosition();
		Statement initialize = statement();
		if (initialize instanceof LogicStatement) Main.error("Invalid statement at " + position.format());
		
		eat(TokenType.SEMI);
		Expression expression = expression();
		eat(TokenType.SEMI);
		
		position = currentToken.getPosition();
		Statement change = statement();
		if (change instanceof LogicStatement) Main.error("Invalid statement at " + position.format());
		
		if (currentToken.getType() == TokenType.SEMI) {
			eat(TokenType.SEMI);
		}
		
		eat(TokenType.RPAREN);
		
		return new ForStatement(token, initialize, expression, change, block());
	}
	
	private Declaration declaration() {
		boolean global;
		if (currentToken.getType() == TokenType.GLOBAL) {
			global = true;
			eat(TokenType.GLOBAL);
		} else {
			global = false;
		}
		
		TypeSpecification type = typeSpecification(false);
		
		String name = currentToken.getValue();
		eat(TokenType.IDENTIFIER);
		
		return new Declaration(type, name, global);
	}

	private Assignment assignment() {
		Expression leftHand;
		
		Token peekToken = lexer.peekToken(1);
		
		if (currentToken.getType() == TokenType.GLOBAL) {
			leftHand = declaration();
		} else if (isAssignToken(peekToken.getType()) || peekToken.getType() == TokenType.DOT) {
			leftHand = dot();
		} else {
			leftHand = declaration();
		}
		
		Token token = currentToken;
		if (isAssignToken(token.getType())) {
			eat(currentToken.getType());
		} else {
			eat(TokenType.ASSIGN);
		}
		Expression rightHand = expression();
		if (token.getType() != TokenType.ASSIGN) {
			rightHand = new BinaryOperator(token, leftHand, rightHand, getAssignOperator(token.getType()));
		}
		
		return new Assignment(token, leftHand, rightHand);
	}
	
	private boolean isAssignToken(TokenType type) {
		return type == TokenType.ASSIGN
				|| type == TokenType.PLUS_ASSIGN || type == TokenType.MINUS_ASSIGN
				|| type == TokenType.DIVIDE_ASSIGN || type == TokenType.MULTIPLY_ASSIGN;
	}
	
	private Operator getAssignOperator(TokenType type) {
		return switch (type) {
			case MINUS_ASSIGN -> Operator.MINUS;
			case DIVIDE_ASSIGN -> Operator.DIVIDE;
			case MULTIPLY_ASSIGN -> Operator.MULTIPLY;
			default -> Operator.PLUS;
		};
	}
	
	private OperatorDefinition operator() {
		Token token = currentToken;
		
		eat(TokenType.OPERATOR);
		eat(TokenType.LPAREN);
		Operator operator = operatorToken();
		eat(TokenType.RPAREN);
		
		Declaration declaration1 = null;
		Declaration declaration2 = null;
		ArrayList<Declaration> onlyCall_parameters = null;
		
		if (currentToken.getType() == TokenType.VOID) {
			eat(TokenType.VOID);
		} else {
			declaration1 = declaration();
		}
		eat(TokenType.COMMA);
		if (currentToken.getType() == TokenType.VOID) {
			eat(TokenType.VOID);
		} else if (currentToken.getType() == TokenType.LPAREN) {
			onlyCall_parameters = parameterList();
		} else {
			declaration2 = declaration();
		}
		
		eat(TokenType.COLON);
		TypeSpecification type = typeSpecification(false);
		
		StatementList block = block();
		
		ArrayList<String> generics = new ArrayList<>();
		if (currentToken.getType() == TokenType.GENERIC) {
			eat(TokenType.GENERIC);
			
			generics.add(currentToken.getValue());
			eat(TokenType.IDENTIFIER);
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				
				generics.add(currentToken.getValue());
				eat(TokenType.IDENTIFIER);
			}
		}
		
		return new OperatorDefinition(token, type, operator, declaration1, declaration2, onlyCall_parameters, generics, block);
	}
	
	private ReturnStatement returnStatement() {
		Token token = currentToken;
		eat(TokenType.RETURN);
		Expression expression = null;
		
		if (currentToken.getType() != TokenType.SEMI) {
			expression = expression();
		}
		
		return new ReturnStatement(token, expression);
	}
	
	private StructureDefinition structureDefinition() {
		Token token = currentToken;
		
		eat(TokenType.STRUCT);
		String name = currentToken.getValue();
		eat(TokenType.IDENTIFIER);
		eat(TokenType.BRACE_OPEN);
		
		ArrayList<Declaration> declarations = new ArrayList<>();
		if (currentToken.getType() != TokenType.BRACE_CLOSE) {
			declarations.add(declaration());
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				
				declarations.add(declaration());
			}
		}
		
		StatementList statements = statementList();
		
		eat(TokenType.BRACE_CLOSE);
		
		ArrayList<String> generics = new ArrayList<>();
		if (currentToken.getType() == TokenType.GENERIC) {
			eat(TokenType.GENERIC);
			
			generics.add(currentToken.getValue());
			eat(TokenType.IDENTIFIER);
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				
				generics.add(currentToken.getValue());
				eat(TokenType.IDENTIFIER);
			}
		}
		
		return new StructureDefinition(token, declarations, statements, name, generics);
	}
	
	private ExpressionFunctionDefinition function() {
		Token token = currentToken;
		
		eat(TokenType.AND);
		ArrayList<Declaration> parameters = parameterList();
		eat(TokenType.COLON);
		
		TypeSpecification type;
		if (currentToken.getType() == TokenType.VOID) { //Needs explicit void, not typeSpecification allowVoid, because that returns null
			Token token2 = currentToken;
			eat(TokenType.VOID);
			type = new TypeSpecification(token2, TypeSpecification.VOID);
		} else {
			type = typeSpecification(false);
		}
		
		return new ExpressionFunctionDefinition(token, type, parameters, block());
	}
	
	private InstantiationStatement instantiation() {
		Token token = currentToken;
		eat(TokenType.NEW);
		
		return new InstantiationStatement(token, typeSpecification(false));
	}
	
	private ListDefinition list() {
		Token token = currentToken;
		eat(TokenType.SQUARE_BRACE_OPEN);
		
		ArrayList<Expression> expressions = new ArrayList<>();
		if (currentToken.getType() != TokenType.SQUARE_BRACE_CLOSE) {
			expressions.add(expression());
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				
				expressions.add(expression());
			}
		}
		
		eat(TokenType.SQUARE_BRACE_CLOSE);
		
		return new ListDefinition(token, expressions);
	}
	
	private ImportStatement importStatement() {
		Token token = currentToken;
		
		eat(TokenType.IMPORT);
		
		ArrayList<String> importedSymbols = new ArrayList<>();
		ArrayList<OperatorDescription> importedOperators = new ArrayList<>();
		
		boolean importAll = false;
		if (currentToken.getType() == TokenType.MULTIPLY) {
			importAll = true;
			eat(TokenType.MULTIPLY);
		} else if (currentToken.getType() == TokenType.OPERATOR) {
			importedOperators.add(operatorDescription());
		} else if (currentToken.getType() == TokenType.IDENTIFIER) {
			importedSymbols.add(currentToken.getValue());
			eat(TokenType.IDENTIFIER);
		} else {
			ImportDescription description = importList();
			importedSymbols.addAll(description.symbols());
			importedOperators.addAll(description.operators());
		}
		
		eat(TokenType.FROM);
		String file = currentToken.getValue();
		eat(TokenType.STRING);
		
		return new ImportStatement(token, importedSymbols, importedOperators, file, importAll);
	}
	
	private ImportDescription importList() {
		eat(TokenType.LPAREN);
		
		ArrayList<String> importedSymbols = new ArrayList<>();
		ArrayList<OperatorDescription> importedOperators = new ArrayList<>();
		
		if (currentToken.getType() != TokenType.RPAREN) {
			ImportDescription description = importDescription();
			importedSymbols.addAll(description.symbols());
			importedOperators.addAll(description.operators());
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				
				description = importDescription();
				importedSymbols.addAll(description.symbols());
				importedOperators.addAll(description.operators());
			}
		}
		
		eat(TokenType.RPAREN);
		
		return new ImportDescription(importedSymbols, importedOperators);
	}
	
	private ImportDescription importDescription() {
		ArrayList<String> symbols = new ArrayList<>();
		ArrayList<OperatorDescription> operators = new ArrayList<>();
		
		if (currentToken.getType() == TokenType.IDENTIFIER) {
			symbols.add(currentToken.getValue());
			eat(TokenType.IDENTIFIER);
		} else {
			operators.add(operatorDescription());
		}
		
		return new ImportDescription(symbols, operators);
	}
	
	private ExportStatement export() {
		Token token = currentToken;
		
		eat(TokenType.EXPORT);
		
		List<Expression> expressions = new ArrayList<>();
		List<String> exportNames = new ArrayList<>();
		
		while (true) {
			if (currentToken.getType() == TokenType.OPERATOR) {
				expressions.add(operatorDescription());
				exportNames.add(null);
			} else {
				Expression expression = expression();
				expressions.add(expression);
				
				if (currentToken.getType() == TokenType.AS) {
					eat(TokenType.AS);
					
					exportNames.add(currentToken.getValue());
					eat(TokenType.IDENTIFIER);
				} else if (expression instanceof Variable) {
					exportNames.add(((Variable) expression).getName());
				} else {
					Main.error("Export statement must specify name if the name is not clear from context (at " + currentToken.getPosition().format() + ")");
				}
			}
			
			if (currentToken.getType() != TokenType.COMMA)
				break;
			eat(TokenType.COMMA);
		}
		
		return new ExportStatement(token, expressions, exportNames);
	}
	
	private OperatorDescription operatorDescription() {
		Token token = currentToken;
		
		eat(TokenType.OPERATOR);
		eat(TokenType.LPAREN);
		Operator operator = operatorToken();
		eat(TokenType.COMMA);
		
		TypeSpecification type1;
		TypeSpecification type2 = null;
		ArrayList<TypeSpecification> onlyCall_types = null;
		
		type1 = typeSpecification(true);
		eat(TokenType.COMMA);
		
		if (currentToken.getType() == TokenType.LPAREN) {
			eat(TokenType.LPAREN);
			
			onlyCall_types = new ArrayList<>();
			onlyCall_types.add(typeSpecification(false));
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				onlyCall_types.add(typeSpecification(false));
			}
			
			eat(TokenType.RPAREN);
		} else {
			type2 = typeSpecification(true);
		}
		
		eat(TokenType.RPAREN);
		
		return new OperatorDescription(token, operator, type1, type2, onlyCall_types);
	}
	
	private Operator operatorToken() {
		Token token = currentToken;
		eat(currentToken.getType());
		
		Operator operator = null;
		try {
			if (token.getType() != TokenType.DOT) {
				operator = Operator.valueOf(token.getType().toString());
			} else {
				Main.error("Access operator cannot be overloaded: " + token.getPosition().format());
			}
		} catch (IllegalArgumentException e) {
			if (token.getType() == TokenType.SQUARE_BRACE_OPEN) {
				eat(TokenType.SQUARE_BRACE_CLOSE);
				operator = Operator.LIST_ACCESS;
			} else if (token.getType() == TokenType.LPAREN) {
				eat(TokenType.RPAREN);
				operator = Operator.CALL;
			} else {
				Main.error("Not a valid operator: '" + token.getValue() + "' at " + token.getPosition().format());
			}
		}
		
		return operator;
	}
	
	private ArrayList<Expression> argumentList() {
		eat(TokenType.LPAREN);
		
		ArrayList<Expression> arguments = new ArrayList<>();
		if (currentToken.getType() != TokenType.RPAREN) {
			arguments.add(expression());
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				arguments.add(expression());
			}
		}
		
		eat(TokenType.RPAREN);
		
		return arguments;
	}
	
	private ArrayList<Declaration> parameterList() {
		eat(TokenType.LPAREN);
		
		ArrayList<Declaration> arguments = new ArrayList<>();
		if (currentToken.getType() != TokenType.RPAREN) {
			arguments.add(declaration());
			
			while (currentToken.getType() == TokenType.COMMA) {
				eat(TokenType.COMMA);
				arguments.add(declaration());
			}
		}
		
		eat(TokenType.RPAREN);
		
		return arguments;
	}
	
	private TypeSpecification typeSpecification(boolean allowVoid) {
		Token token = currentToken;
		
		if (allowVoid && currentToken.getType() == TokenType.VOID) {
			eat(TokenType.VOID);
			return new TypeSpecification(token, TypeSpecification.VOID);
		}
		
		String name = currentToken.getValue();
		eat(TokenType.IDENTIFIER);
		
		ArrayList<TypeSpecification> generics = null;
		ArrayList<TypeSpecification> parameterTypes = null;
		if (currentToken.getType() == TokenType.GENERIC) {
			eat(TokenType.GENERIC);
			
			generics = new ArrayList<>();
			
			if (currentToken.getType() == TokenType.LPAREN) {
				eat(TokenType.LPAREN);
				if (currentToken.getType() != TokenType.UNKNOWN) {
					generics.add(typeSpecification(true));
				} else {
					Token unknownToken = currentToken;
					eat(TokenType.UNKNOWN);
					generics.add(new TypeSpecification(unknownToken, TypeSpecification.UNKNOWN, null, null));
				}
				
				if (currentToken.getType() == TokenType.DOT) {
					parameterTypes = new ArrayList<>();
					
					eat(TokenType.DOT);
					if (currentToken.getType() == TokenType.LPAREN) {
						eat(TokenType.LPAREN);
						
						if (currentToken.getType() == TokenType.IDENTIFIER) {
							parameterTypes.add(typeSpecification(false));
							while (currentToken.getType() == TokenType.COMMA) {
								eat(TokenType.COMMA);
								parameterTypes.add(typeSpecification(false));
							}
						}
						
						eat(TokenType.RPAREN);
					} else if (currentToken.getType() == TokenType.IDENTIFIER) {
						parameterTypes.add(typeSpecification(false));
					}
				} else while (currentToken.getType() == TokenType.COMMA) {
					eat(TokenType.COMMA);
					if (currentToken.getType() != TokenType.UNKNOWN) {
						generics.add(typeSpecification(true));
					} else {
						Token unknownToken = currentToken;
						eat(TokenType.UNKNOWN);
						generics.add(new TypeSpecification(unknownToken, TypeSpecification.UNKNOWN, null, null));
					}
				}
				
				eat(TokenType.RPAREN);
			} else {
				if (currentToken.getType() != TokenType.UNKNOWN) {
					generics.add(typeSpecification(true));
				} else {
					Token unknownToken = currentToken;
					eat(TokenType.UNKNOWN);
					generics.add(new TypeSpecification(unknownToken, TypeSpecification.UNKNOWN, null, null));
				}
			}
		}
		
		return new TypeSpecification(token, name, generics, parameterTypes);
	}
	
	private StatementList block() {
		eat(TokenType.BRACE_OPEN);
		StatementList statements = statementList();
		eat(TokenType.BRACE_CLOSE);
		
		return statements;
	}
	
	private StatementList statementList() {
		ArrayList<Statement> statements = new ArrayList<>();
		
		while (currentToken.getType() == TokenType.SEMI) {
			eat(TokenType.SEMI);
		}
		
		while (currentToken.getType() != TokenType.BRACE_CLOSE && currentToken.getType() != TokenType.EOF) {
			statements.add(statement());
			
			while (currentToken.getType() == TokenType.SEMI) {
				eat(TokenType.SEMI);
			}
		}
		
		return new StatementList(statements);
	}
	
	private Statement statement() {
		if (currentToken.getType() == TokenType.IF) {
			return ifStatement();
		} else if (currentToken.getType() == TokenType.WHILE) {
			return whileStatement();
		} else if (currentToken.getType() == TokenType.FOR) {
			return forStatement();
		} else if (currentToken.getType() == TokenType.RETURN) {
			return returnStatement();
		} else if (currentToken.getType() == TokenType.IMPORT) {
			return importStatement();
		} else if (currentToken.getType() == TokenType.EXPORT) {
			return export();
		} else if (currentToken.getType() == TokenType.STRUCT) {
			return structureDefinition();
		} else if (currentToken.getType() == TokenType.OPERATOR) {
			return operator();
		} else if (currentToken.getType() == TokenType.GLOBAL) {
			return declarationOrAssignment();
		} else {
			Expression expression = assignExpression();
			
			if (expression instanceof Statement) {
				return (Statement) expression;
			} else {
				Main.error("Not a statement: " + currentToken.getPosition().format());
				return null;
			}
		}
	}
	
	public StatementList parse() {
		StatementList tree = statementList();
		this.eof = currentToken;
		eat(TokenType.EOF);
		return tree;
	}
	
	private Token eof;
	
	public Token getEof() {
		return eof;
	}
	
	private record ImportDescription(List<String> symbols,
	                                List<OperatorDescription> operators) {}
}
