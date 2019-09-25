
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
	private final String STATEMENT_SUCCESS = "STMT SUCCESS";

	VariableState(SymbolTable table, Lexemes lexemes) {
		foundID = false;
		needToWaitForAssign = false;
		changeState = false;
		varValue = null;
		expressionMode = false;

		this.table = table;
		this.lexemes = lexemes;
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

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

					ExpressionUtils.addTokensToLexemes(lexemes, expression.toString(), table);
				} else if (word.endsWith(";")) {
					expression.append(word.replace(";", ""));
					changeState = true;
					nextState = STATEMENT_SUCCESS;

					// add variable to the symbol table
					id = table.addSymbol(varName, varValue);

					ExpressionUtils.addTokensToLexemes(lexemes, expression.toString(), table);
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

					//validate the value - type checking (Type should be one of Number, String, and Boolean.
					if (validateValue(str)) {
						varValue = str;

						//add variable data to symbol table
						id = table.addSymbol(varName, varValue);

						changeState = true;
						nextState = STATEMENT_SUCCESS;

						// add lexeme token
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
					changeState = true;
					nextState = STATEMENT_SUCCESS;

					// add variable to the symbol table
					id = table.addSymbol(varName, varValue);

					// add lexemes
					lexemes.insertLexeme("ID", ((Integer) id).toString());

				} else if (word.trim().equals(":=")) {
					needToWaitForAssign = true;
				} else if (word.startsWith(":=")) {

					String newWord = word.replace(":=", "").trim();

					if (newWord.contains(";")) {
						validateVarValue(newWord);
					} else {
						expressionMode = true;
						expression = new StringBuilder(newWord);
					}

				} else {
					backToStatementState("SyntaxError::Invalid syntax!");
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
							id = table.addSymbol(varName, varValue);
							nextState = STATEMENT_SUCCESS;
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

	private void backToStatementState(String errorMessage) {
		System.out.println(errorMessage);
		changeState = true;
		nextState = BACK_TO_STATEMENT;
	}

	private boolean validateVarName(String word) {
		if (validateName(word)) {
			varName = word;
			return true;
		} else {
			nextState = BACK_TO_STATEMENT;
			return false;
		}
	}

	private boolean validateVarValue(String word) {
		if (validateValue(word)) {
			varValue = word;
			id = table.addSymbol(varName, varValue); //add variable data to symbol table

			//add lexeme token
			lexemes.insertLexeme("ID", ((Integer) id).toString());

			nextState = STATEMENT_SUCCESS;
			return true;
		} else {
			backToStatementState("TypeError::Variable type should be one of [Number, String, Boolean]");
			return false;
		}
	}

	private void validateVarNameAndValue(String word) {
		String[] splitted = word.split(":=");

		if (validateVarName(splitted[0])) {
			//check whether the right side of the ':=' is expression or not
			if (splitted[1].contains(";")) {
				validateVarValue(splitted[1].replace(";", ""));
			} else {
				expressionMode = true;
				expression = new StringBuilder(splitted[1]);
			}
		}
	}

	private boolean validateValue(String str) {
		String word = str.replace(";", "");
		boolean isValid = true;

		if (!table.contains(word)) {
			if (!(word.startsWith("\"") && word.endsWith("\""))) {
				try {
					Double.parseDouble(word);
				} catch (NumberFormatException e) {
					isValid = false;
				}
			}
		} else {
			int variableID = table.getIdOfVariable(word);
			//TODO id of the variable -> ???
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

}
