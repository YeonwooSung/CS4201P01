
public class AssignmentState implements LexerFSA {
	private boolean changeState;
	private boolean findTarget;
	private boolean findEqualSign;
	private boolean useStringBuilder;
	private int id;
	private String nextState;
	private StringBuilder builder;
	private SymbolTable table;
	private Lexemes lexemes;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String STATEMENT_SUCCESS = "A - STMT SUCCESS";

	AssignmentState(SymbolTable table) {
		this.table = table;

		changeState = false;
		findEqualSign = false;
		findTarget = false;
		nextState = null;
		useStringBuilder = false;
		lexemes = new Lexemes();
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		if (findTarget) {
			if (findEqualSign) {

				// check if the word contains the semicolon.
				if (word.contains(";")) {
					String newWord = word.replace(";", "");

					if (useStringBuilder) {
						builder.append(newWord);
						this.successParsing(builder.toString());
					} else {
						this.successParsing(newWord);
					}
				} else {
					if (useStringBuilder) {
						builder.append(word);
					} else {
						useStringBuilder = true;
						builder = new StringBuilder(word);
					}
				}

			} else {
				if (word.contains(":=")) {
					findEqualSign = true;

					if (!word.equals(":=")) {
						String newWord = word.replace(":=", "");

						// check if the string contains the semicolon.
						if (word.contains(";")) {
							String expression = word.replace(";", "");
							this.successParsing(expression);
						} else {
							useStringBuilder = true;
							builder = new StringBuilder(newWord);
						}
					}
				} else {
					this.generateErrorMessage("SyntaxError::Failed to find \":=\"");
				}
			}

		} else {
			if (word.contains(":=")) {
				String[] words = word.split(":=");
				findEqualSign = true;

				// validate the variable name
				if (validateVarName(words[0])) {
					findTarget = true;

					// check if the assignment statement contains the semicolon
					if (words[1].contains(";")) {
						this.successParsing(words[1]);
					} else {
						useStringBuilder = true;
						builder = new StringBuilder(words[1]);
					}
				}
			} else {
				// validate the variable name
				if (validateVarName(word)) {
					findTarget = true;
				}
			}
		}
	}

	/**
	 * Validate the variable name.
	 * @param name - variable name
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateVarName(String name) {
		if (table.contains(name)) {
			id = table.getIdOfVariable(name);
			return true;
		} else {
			this.generateErrorMessage("NameError::Variable \"" + name + "\" is not declared");
			return false;
		}
	}

	/**
	 * Validate the given expression, and change the state.
	 * @param expression - expression that should be validated.
	 */
	private void successParsing(String expression) {
		changeState = true;
		lexemes.insertLexeme("ASSIGN");
		lexemes.insertLexeme("ID", ((Integer) id).toString());
		lexemes.insertLexeme("IS", "=");
		lexemes.insertLexeme("ASSIGN_END");

		// validate the expression, and add the lexeme tokens to the Lexemes object
		if (ExpressionUtils.addTokensToLexemes(lexemes, expression, table)) {
			nextState = STATEMENT_SUCCESS;
		} else {
			nextState = BACK_TO_STATEMENT;
		}
	}

	/**
	 * Generate error message, and change the state of the LexerFSA to StatementState.
	 * @param errorMessage - Error message to print out
	 */
	private void generateErrorMessage(String errorMessage) {
		changeState = true;
		nextState = BACK_TO_STATEMENT;
		System.out.println(errorMessage);
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
}
