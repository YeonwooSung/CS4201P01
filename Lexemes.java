import java.util.ArrayList;

public class Lexemes {
	private ArrayList<SymbolToken> lexemeList = new ArrayList<>();

	public void insertLexeme(String name) {
		lexemeList.add(new SymbolToken(name));
	}

	public void insertLexeme(String name, String value) {
		lexemeList.add(new SymbolToken(name, value));
	}

	private ArrayList<SymbolToken> getList() {
		return lexemeList;
	}

	/**
	 * Print all lexemes.
	 */
	public void printAll() {
		for (SymbolToken token : lexemeList) {
			token.printTokenString();
		}
	}

	/**
	 * Merge 2 lexeme array lists.
	 *
	 * @param other - target Lexemes object.
	 */
	public void mergeLexemes(Lexemes other) {
		ArrayList<SymbolToken> list = other.getList();

		for (SymbolToken token : list) {
			lexemeList.add(token);
		}
	}
}
