import java.util.ArrayList;

public class Lexemes {
	private ArrayList<SymbolToken> lexemeList = new ArrayList<>();

	public void insertLexeme(String name) {
		lexemeList.add(new SymbolToken(name));
	}

	public void insertLexeme(String name, String value) {
		lexemeList.add(new SymbolToken(name, value));
	}

	/**
	 * Help the user to insert the given token to lexeme list.
	 * @param token - target token
	 */
	public void insertLexeme(SymbolToken token) {
		lexemeList.add(token);
	}

	/**
	 * Getter for lexemeList.
	 * @return lexemeList
	 */
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
	 * Getter for lexemeList.
	 * @return lexemeList
	 */
	public ArrayList<SymbolToken> getLexemeList() {
		return this.lexemeList;
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
