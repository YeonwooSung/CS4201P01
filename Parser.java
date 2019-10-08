import java.util.ArrayList;


public class Parser {
	private SymbolTable table;
	private Lexemes lexemes;

	Parser(SymbolTable table, Lexemes lexemes) {
		this.table = table;
		this.lexemes = lexemes;
	}

	public void runParser() {
		ArrayList<SymbolToken> lexemeList = lexemes.getLexemeList();
		for (SymbolToken token : lexemeList) {
			//
		}
	}

	public void parse() {
		//
	}

	public static void main(String[] args) {
		//
	}
}
