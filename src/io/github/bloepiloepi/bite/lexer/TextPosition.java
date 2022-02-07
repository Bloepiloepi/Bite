package io.github.bloepiloepi.bite.lexer;

public record TextPosition(String file, int line, int column) {
	public String format() {
		return "[" + file + ", " + line + ", " + column + "]";
	}
}
