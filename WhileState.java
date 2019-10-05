
public class WhileState implements LexerFSA {
	private boolean changeState;
	private boolean parenL;
	private boolean parenR;
	private boolean foundWhile;
	private int parenthesisLevel;
	private String nextState;
	private Lexemes lexemes;
	SymbolTable table;
	private StringBuilder expression;

	private final String COMPOUND_STATE = "WHILE_COMPOUND";
	private final String BACK_TO_STATEMENT = "Back To Statement";

	WhileState(SymbolTable table) {
		this.table = table;
		lexemes = new Lexemes();
		changeState = false;
		expression = new StringBuilder();
		parenthesisLevel = 0;
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		if (foundWhile) { // check if the lexer found the word "while"

			// check if the lexer found the left parenthesis
			if (parenL) {
				int numOfL = this.countTheNumberOf(word, "\\(");
				int numOfR = this.countTheNumberOf(word, "\\)");
				int diff = numOfL - numOfR;
				this.parenthesisLevel += diff;

				// check if the lexer found the right parenthesis
				if (parenR) {
					changeState = true;

					// compare the number of right and left parentheses to check the syntax error
					if (parenthesisLevel > 0) {
						this.generateErrorMessage("SyntaxError::Parenthesis not closed");
					} else if (parenthesisLevel < 0) {
						generateErrorMessage("SyntaxError::Too many \")\"");
					} else {
						// check if the compound starts with the word "begin"
						if (!word.equals("begin")) {
							this.generateErrorMessage("SyntaxError::The compound of the while statement should start with \"begin\"!");
							return;
						}

						// validate the syntax of the boolean expression
						this.validateBooleanStr();
					}
				} else {
					if (word.contains("begin")) {
						if (this.parenthesisLevel > 0) {
							this.generateErrorMessage("SyntaxError::Parenthesis not closed");
						} else if (this.parenthesisLevel < 0) {
							this.generateErrorMessage("SyntaxError::Too many \")\"");
						} else {
							// validate the syntax of the boolean expression
							this.validateBooleanStr();
						}

					} if (word.endsWith(")")) {
						// check differences between number of left parentheses and right parentheses
						if (this.parenthesisLevel < 0) {
							generateErrorMessage("SyntaxError::Too many \")\"");
						} else if (this.parenthesisLevel > 0) {
							expression.append(word);
						} else {
							parenR = true;
							expression.append(word);
						}
					} else {
						expression.append(word);
					}
				}
			} else {
				// check if the word starts with the left parenthesis
				if (word.startsWith("(")) {
					String newWord = word.substring(1);
					parenL = true;

					int numOfL = this.countTheNumberOf(newWord, "\\(") + 1;
					int numOfR = this.countTheNumberOf(newWord, "\\)");
					int diff = numOfL - numOfR;
					this.parenthesisLevel += diff;

					// compare the number of right and left parentheses to check the syntax error
					if (this.parenthesisLevel > 0) {
						expression.append(newWord);
					} else if (this.parenthesisLevel < 0) {
						generateErrorMessage("SyntaxError::Too many \")\"");
					} else {
						parenR = true;
						expression.append(newWord);
					}

				} else {
					generateErrorMessage("SyntaxError::Failed to found \"(\"!");
				}
			}
		} else {
			if (word.startsWith("while")) {
				String newWord = word.replace("while", "").trim();
				foundWhile = true;

				// check if the word contains parenthesis
				if (newWord.startsWith("(")) {
					parenL = true;
					newWord = newWord.substring(1);

					int numOfL = this.countTheNumberOf(newWord, "\\(") + 1;
					int numOfR = this.countTheNumberOf(newWord, "\\)");
					int diff = numOfL - numOfR;

					//check if the character stream ends with right parenthesis
					if (newWord.endsWith(")")) {

						// check differences between number of left parentheses and right parentheses
						if (diff < 0) {
							generateErrorMessage("SyntaxError::Too many \")\"");
						} else if (diff > 0) {
							expression.append(newWord);
							parenthesisLevel += diff;
						} else {
							parenR = true;
							expression.append(newWord);
						}
					} else {
						if (diff < 0) {
							generateErrorMessage("SyntaxError::Too many \")\"");
						} else {
							parenthesisLevel += diff;
							expression.append(newWord);
						}
					}
				}
			} else {
				generateErrorMessage("SyntaxError::while statement should start with \"while\"");
			}
		}
	}

	/**
	 * Validate the boolean expression of the while statement.
	 * If the lexer finds some syntax error, it will print out the error message.
	 */
	private void validateBooleanStr() {
		String expressionStr = expression.toString().trim();
		String booleanStr = expressionStr.substring(0, expressionStr.length() - 1);
		changeState = true;

		lexemes.insertLexeme("WHILE");

		// validate booleanStr
		if (ExpressionUtils.addTokensToLexemes(lexemes, booleanStr, table)) {
			nextState = COMPOUND_STATE;
		} else {
			nextState = BACK_TO_STATEMENT;
		}
	}

	private int countTheNumberOf(String from, String target) {
		int counter = from.length() - from.replaceAll(target, "").length();
		return counter;
	}

	/**
	 * Generate error message, and change the state of FSA to StatementState.
	 *
	 * @param errorMsg - Error message to print out.
	 */
	private void generateErrorMessage(String errorMsg) {
		System.out.println(errorMsg);
		changeState = true;
		nextState = BACK_TO_STATEMENT;
	}

	@Override
	public String getNextState() {
		return nextState;
	}

	public Lexemes getLexemes() {
		return lexemes;
	}
}
