
public class PrintState implements LexerFSA {
	private boolean changeState;
	private boolean foundPrint;
	private int type;
	private String nextState;
	private StringBuilder builder;
	private SymbolTable table;
	private Lexemes lexemes;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String STATEMENT_SUCCESS = "PR - STMT SUCCESS";

	PrintState(SymbolTable table) {
		this.table = table;

		this.foundPrint = false;
		this.nextState = null;
		this.changeState = false;
		this.type = 0;
		this.builder = null;
		this.lexemes = new Lexemes();
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		if (foundPrint) {

			// check if the character stream contains the semicolon
			if (word.contains(";")) {
				String subStr = word.replace(";", "");
				changeState = true;

				if (builder != null) {
					builder.append(subStr);
					String expression = builder.toString();

					switch (type) {
						case 1:
							nextState = STATEMENT_SUCCESS;
							lexemes.insertLexeme("GET");
							lexemes.insertLexeme("ID", ((Integer)table.getIdOfVariable(subStr)).toString());
							lexemes.insertLexeme("GET_END");
							break;
						case 2:
							lexemes.insertLexeme("PRINTLN");
							checkPrintStatement(expression);
							lexemes.insertLexeme("PRINTLN_END");
							break;
						case 3:
							lexemes.insertLexeme("PRINT");
							checkPrintStatement(expression);
							lexemes.insertLexeme("PRINT_END");
							break;
						default:
							System.out.println("Error::System error - invalid type (PrintState)");
							nextState = BACK_TO_STATEMENT;
					}

				} else {

					switch (type) {
						case 1:
							nextState = STATEMENT_SUCCESS;
							changeState = true;

							lexemes.insertLexeme("GET");
							lexemes.insertLexeme("ID", ((Integer)table.getIdOfVariable(subStr)).toString());
							lexemes.insertLexeme("GET_END");
							break;
						case 2:
							lexemes.insertLexeme("PRINTLN");
							checkPrintStatement(subStr);
							lexemes.insertLexeme("PRINTLN_END");
							break;
						case 3:
							lexemes.insertLexeme("PRINT");
							checkPrintStatement(subStr);
							lexemes.insertLexeme("PRINT_END");
							break;
						default:
							System.out.println("Error::System error - invalid type (PrintState)");
							System.exit(1);
					}

				}
			} else {
				if (builder != null) {
					builder.append(word);
				} else {
					builder = new StringBuilder(word);
				}
			}
		} else {

			// check the command
			if (word.equals("get")) {
				foundPrint = true;
				type = 1;
			} else if (word.equals("println")) {
				foundPrint = true;
				type = 2;
			} else if (word.equals("print")) {
				foundPrint = true;
				type = 3;
			}
		}
	}

	@Override
	public String getNextState() {
		return nextState;
	}

	/**
	 * Getter for lexemes.
	 * @return lexemes
	 */
	public Lexemes getLexemes() {
		return lexemes;
	}

	/**
	 * Check if the syntax of the print statement is valid.
	 * @param expression - expression of the print statement
	 */
	private void checkPrintStatement(String expression) {
		// validate expression
		if (ExpressionUtils.addTokensToLexemes(lexemes, expression, table)) {
			nextState = STATEMENT_SUCCESS;
		} else {
			nextState = BACK_TO_STATEMENT;
		}
	}
}
