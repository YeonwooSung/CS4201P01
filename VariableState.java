
public class VariableState implements LexerFSA {
	private boolean foundID;
	private boolean needToWaitForAssign;
	private boolean changeState;
	private boolean expressionMode;
	private String varName;
	private String varValue;
	private String nextState;
	private StringBuilder expression;
	private int id;
	private Lexemes lexemes;
	private SymbolTable table;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String STATEMENT_SUCCESS = "V - STMT SUCCESS";

	VariableState(SymbolTable table) {
		foundID = false;
		needToWaitForAssign = false;
		changeState = false;
		varValue = null;
		expressionMode = false;
		this.lexemes = new Lexemes(); 

		this.table = table;
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	/**
	 * Parse the given word.
	 * @param word - character stream
	 */
	@Override
	public void parseWord(String word) {
		if (expressionMode) {

			//check if expression ends
			if (word.contains(";")) {

				if (word.equals(";")) {
					changeState = true;
					nextState = STATEMENT_SUCCESS;

					// add variable to the symbol table
					id = table.addSymbol(varName, varValue);

					boolean checker = ExpressionUtils.addTokensToLexemes(lexemes, expression.toString(), table, ((Integer)id).toString());

					if (!checker) {
						table.removeSymbol(varName);
						nextState = BACK_TO_STATEMENT;
					}
				} else if (word.endsWith(";")) {
					expression.append(word.replace(";", ""));
					changeState = true;
					nextState = STATEMENT_SUCCESS;

					// add variable to the symbol table
					id = table.addSymbol(varName, varValue);

					boolean checker = ExpressionUtils.addTokensToLexemes(lexemes, expression.toString(), table, ((Integer)id).toString());

					if (!checker) {
						table.removeSymbol(varName);
						nextState = BACK_TO_STATEMENT;
					}
					lexemes.printAll();//TODO
				} else {
					backToStatementState("SyntaxError::Syntax error in : " + word);
				}
			} else {
				expression.append(word);
			}

		}else if (foundID) {

			if (needToWaitForAssign) {

				if (word.contains(";")) {
					String str = word.replace(";", "");

					//validate the value
					if (validateValue(str)) {
						//add variable data to symbol table
						id = table.addSymbol(varName, varValue);

						changeState = true;
						nextState = STATEMENT_SUCCESS;

						// add lexeme token
						lexemes.insertLexeme("VAR");
						lexemes.insertLexeme("ID", ((Integer) id).toString());
					} else {
						backToStatementState("TypeError::Variable type should be one of [Number, String, Boolean] - " + word);
					}
				} else {
					expressionMode = true;
					expression = new StringBuilder(word);
				}

			} else {

				if (word.equals(";")) {
					System.out.println("here");
					changeState = true;
					nextState = STATEMENT_SUCCESS;

					// add variable to the symbol table
					id = table.addSymbol(varName, varValue);

					// add lexemes
					lexemes.insertLexeme("VAR");
					lexemes.insertLexeme("ID", ((Integer) id).toString());

				} else if (word.trim().equals(":=")) {
					needToWaitForAssign = true;
				} else if (word.startsWith(":=")) {

					String newWord = word.replace(":=", "").trim();

					if (newWord.contains(";")) {
						validateVarValue(newWord.replace(";", ""));
					} else {
						expressionMode = true;
						expression = new StringBuilder(newWord);
					}

				} else {
					if (word.contains(";")) {
						varValue = word.replace(";", "");
						changeState = true;
						nextState = STATEMENT_SUCCESS;

						// add variable to the symbol table
						id = table.addSymbol(varName, varValue);

						// add lexemes
						lexemes.insertLexeme("VAR");
						lexemes.insertLexeme("ID", ((Integer) id).toString());
					} else {
						expressionMode = true;
						expression = new StringBuilder(word);
					}
				}

			}

		} else {
			if (table.contains(word)) {
				backToStatementState("NameError::There is already variable with same name");
			} else {

				// check if the statement ends here (i.e. var a;)
				if (word.contains(";")) {
					changeState = true;

					if (word.endsWith(";")) {

						// check if word contains ":="
						if (word.contains(":=")) {
							validateVarNameAndValue(word);
						} else {
							varName = word.replace(";", "");

							id = table.addSymbol(varName, varValue);
							nextState = STATEMENT_SUCCESS;
							lexemes.insertLexeme("VAR");
							lexemes.insertLexeme("ID", ((Integer) id).toString());
						}

					} else {
						backToStatementState("NameError::Invalid format of name - You can't use \';\' in the variable name");
					}
				} else {
					checkIfEndsWithAssign(word);
				}

				foundID = true;
			}
		}
	}

	/**
	 * Check if the given word ends with assignment.
	 * @param word - Character stream
	 */
	private void checkIfEndsWithAssign(String word) {
		if (word.endsWith(":=")) {
			String newWord = word.replace(":=", "");
			if (validateName(newWord)) {
				varName = newWord;
			} else {
				changeState = true;
				nextState = BACK_TO_STATEMENT;
			}
		} else {
			varName = word;
		}
	}

	/**
	 * Change the state of the FSA to StatementState.
	 * @param errorMessage - error message to print out.
	 */
	private void backToStatementState(String errorMessage) {
		System.out.println(errorMessage);
		changeState = true;
		nextState = BACK_TO_STATEMENT;
	}

	/**
	 * Check the symbol table to check if the given name of variable is declared.
	 * @param word - name of the variable.
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateVarName(String word) {
		if (validateName(word)) {
			varName = word;
			return true;
		} else {
			nextState = BACK_TO_STATEMENT;
			return false;
		}
	}

	/**
	 * Validate the variable's value.
	 * @param word - character stream
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateVarValue(String word) {
		if (validateValue(word)) {
			id = table.addSymbol(varName, varValue); //add variable data to symbol table

			//add lexeme token
			lexemes.insertLexeme("VAR");
			lexemes.insertLexeme("ID", ((Integer) id).toString());

			this.changeState = true;
			nextState = STATEMENT_SUCCESS;
			return true;
		} else {
			backToStatementState("TypeError::Variable type should be one of [Number, String, Boolean]");
			return false;
		}
	}

	/**
	 * Validates the variable declaration statement.
	 * @param word - stream of characters that should be validated.
	 */
	private void validateVarNameAndValue(String word) {
		String[] splitted = word.split(":=");

		if (validateVarName(splitted[0])) {
			//check whether the right side of the ':=' is expression or not
			if (splitted[1].contains(";")) {
				validateVarValue(splitted[1]);
			} else {
				expressionMode = true;
				expression = new StringBuilder(splitted[1]);
			}
		}
	}

	/**
	 * This function validates the value string.
	 * @param str - string to validate
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateValue(String str) {
		String word = str.replace(";", "");
		boolean isValid;

		if (!table.contains(word)) {
			if (word.equals("true") || word.equals("false")) {
				isValid = true;
				varValue = word;
			} else if (!(word.startsWith("\"") && word.endsWith("\""))) {
				try {
					Double.parseDouble(word);
					isValid = true;
					varValue = word;
				} catch (NumberFormatException e) {
					isValid = false;
				}
			} else {
				isValid = true;
				varValue = word;
			}
		} else {
			isValid = true;
		}

		return isValid;
	}

	/**
	 * Getter for id.
	 * @return id
	 */
	public int getID() {
		return id;
	}

	/**
	 * This function validates the variable name.
	 * It checks if the given name is a keyword.
	 * @param str - variable name
	 * @return If the name is valid, returns true. Otherwise, returns false.
	 */
	private boolean validateName(String str) {
		if (str.equals("program") || str.equals("begin") || str.equals("end") || str.equals("if") || str.equals("else") || str.equals("while") || str.equals("print") || str.equals("println") || str.equals("procedure") || str.equals("get") || str.equals("function")) {
			System.out.println("KeywordError::Cannot use the keyword string as a name of the program!");
			return false;
		}

		return true;
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
