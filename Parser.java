import java.util.ArrayList;

public class Parser {
	private SymbolTable table;
	private Lexemes lexemes;
	ArrayList<SymbolToken> lexemeList;

	Parser(SymbolTable table, Lexemes lexemes) {
		this.table = table;
		this.lexemes = lexemes;
		lexemeList = lexemes.getLexemeList();
	}

	public AbstractSyntaxTreeNode parse(int startIndex) {
		String targetName = null;
		ArrayList<AbstractSyntaxTreeNode> children = new ArrayList<AbstractSyntaxTreeNode>();

		// use for loop to iterate the ArrayList of SymbolTokens
		for (int i = startIndex; i < lexemeList.size(); i++) {
			SymbolToken token = lexemeList.get(i);
			String name = token.getName();

			if (name.equals("While") || name.equals("Function")) {
				targetName = name + "_END";
				AbstractSyntaxTreeNode subTree = this.parse(i + 1, targetName);

				children.add(subTree);
			} else if (name.equals("If")) {
				//TODO

			} else {
				ArrayList<SymbolToken> terminals = new ArrayList<SymbolToken>();
				terminals.add(token);

				for (int j = i + 1; j < lexemeList.size(); j++) {
					SymbolToken tempToken = lexemeList.get(j);
					String tempName = tempToken.getName();

					// check if current token is terminal
					if (this.checkIfTerminal(tempName)) {
						//add terminal symbol to the array list
						terminals.add(tempToken);
					} else {
						break; //non terminal start
					}
				}

				//TODO use terminals to generate sub-tree
			}
		}

		SymbolToken token = new SymbolToken();
		token.setName("Program");
		return new AbstractSyntaxTreeNode(token, children);
	}


	private AbstractSyntaxTreeNode parse(int startIndex, String endName) {
		String targetName = null;

		ArrayList<AbstractSyntaxTreeNode> children = new ArrayList<AbstractSyntaxTreeNode>();

		// use for loop to iterate the ArrayList of SymbolTokens
		for (int i = startIndex; i < lexemeList.size(); i++) {
			SymbolToken token = lexemeList.get(i);
			String name = token.getName();

			if (this.checkIfNewProductionRuleStarted(name)) {
				targetName = name + "_END";

				// call itself recursively to parse the nested compound
				AbstractSyntaxTreeNode subTree = this.parse(i + 1, targetName);

				children.add(subTree);
			} else if (name.equals("If")) {
				//TODO

			} else if (name.equals(endName)) {
				//TODO

				break;
			} else {
				ArrayList<SymbolToken> terminals = new ArrayList<SymbolToken>();
				terminals.add(token);

				for (int j = i + 1; j < lexemeList.size(); j++) {
					SymbolToken tempToken = lexemeList.get(j);
					String tempName = tempToken.getName();

					// check if current token is terminal
					if (this.checkIfTerminal(tempName)) {
						//add terminal symbol to the array list
						terminals.add(tempToken);
					} else {
						break; //non terminal start
					}
				}

				//TODO use terminals to generate sub-tree
			}
		}

		return new AbstractSyntaxTreeNode(this.lexemeList.get(startIndex - 1), children);
	}

	/**
	 * Check if the given string is a starting point of a production rule.
	 * @param s - string to check
	 * @return True or false.
	 */
	private boolean checkIfNewProductionRuleStarted(String s) {
		if (s.equals("While") || s.equals("Function") || s.equals("If") || s.equals("Else") || s.equals("PRINT") || s.equals("PRINTLN") || s.equals("GET") || s.equals("ASSIGN")) {
			return true;
		}

		return false;
	}

	/**
	 * Check if the given name of token is terminal.
	 * @param name - name of the symbol token.
	 * @return If it is terminal, returns true. Otherwise, returns false.
	 */
	private boolean checkIfTerminal(String name) {
		if (name.equals("While") || name.equals("Function") || name.equals("If") || name.equals("then") || name.equals("Else")) {
			return false;
		} else if (name.endsWith("_END")) {
			return false;
		}

		return true;
	}
}
