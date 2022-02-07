package io.github.bloepiloepi.bite.lexer;

public class TextPositionManager {
	
	private final String file;
	private int line = 1;
	private int column = 1;
	
	public TextPositionManager(String file) {
		this.file = file;
	}
	
	public TextPosition getCurrent() {
		return new TextPosition(this.file, this.line, this.column);
	}
	
	public void setLine(int line) {
		this.line = line;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
	
	public void advance(boolean tab) {
		this.column += tab ? 4 : 1;
	}
	
	public void newLine() {
		this.line += 1;
		this.column = 1;
	}
}
