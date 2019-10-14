import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionState implements LexerFSA {
	private boolean changeState;
	private boolean findFunctionName;
	private String nextState;
	private String functionName;
	private String functionStr;
	private SymbolTable table;
	private SymbolTable temp;
	private Lexer lexer;
	private ArrayList<Lexemes> lexemeList;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String STATEMENT_SUCCESS = "F - STMT SUCCESS";

	FunctionState(SymbolTable table) {
		this.table = table;

		this.changeState = false;
		this.nextState = null;
		this.lexer = new Lexer(table, true);
		this.lexer.parseLine("program SubProgramForFunction");
		temp = new SymbolTable();
	}

	@Override
	public boolean isAbleToChangeState() {
		return this.changeState;
	}

	@Override
	public void parseWord(String word) {
		// check if the lexer found the function's name from the character stream
		if (!this.findFunctionName) {
			// check if the name of the function is equal to the keyword
			if (word.equals("begin") || word.equals("end") || word.equals("program") || word.equals("procedure") || word.equals("print") || word.equals("println") || word.equals("get")) {
				this.generateErrorMessage("KeywordError::Cannot use the keyword string as a name of the program!");
				return;
			}

			functionName = word;
			findFunctionName = true;
		}
	}

	public void parseFunctionString(String functionString) {
		String words[] = functionString.split(" ");

		if (words[0].contains("(")) {
			words[0] = words[0].replace("(", "");

			if (words[0].endsWith("var")) {
				words[0] = words[0].substring(0, words[0].length() - 3).trim();
			}
		}

		this.parseWord(words[0]); // validate the function name

		// check if the function name is valid.
		if (this.isAbleToChangeState() && this.getNextState().equals(BACK_TO_STATEMENT)) {
			return;
		}

		functionStr = functionString.substring(words[0].length()).trim();

		// get the index of the argument string from the function string
		int argIndex = this.getIndexOfArgumentsFromFunctionString(functionStr);

		if (argIndex == -1) {
			return;
		}

		String argStr = functionStr.substring(0, argIndex).trim();
		functionStr = functionStr.substring(argIndex + 1);

		// check if the argument section of the function starts with the left parenthesis
		if (argStr.startsWith("(")) {

			// check the number of arguments
			if (argStr.contains(",")) {
				String[] args = argStr.substring(1, argStr.length() - 1).trim().split(",");

				// use for loop to validate all arguments
				for (String arg : args) {
					if (arg.equals("") || arg.matches("\\s+")) {
						this.generateErrorMessage("SyntaxError::Invalid number of \",\"!");
					}

					if (!this.validateArgumentString(arg)) {
						return;
					}
				}

				// check symbol table if there is duplicating function.
				if (table.checkIfFunctionExist(functionName, args.length)) {
					this.generateErrorMessage("NameError::The function " + functionName + " is already declared!");
				} else {
					// add the function information to the symbol table
					table.addFunction(functionName, args.length);
				}
			} else {
				// validate the argument name
				if (!this.validateArgumentString(argStr)) {
					return;
				}

				int argNum;

				// check if the function has no argument
				if (argStr.replace("(", "").replace(")", "").trim().equals("")) {
					argNum = 0;
				} else {
					argNum = 1;
				}

				// check symbol table if there is duplicating function.
				if (table.checkIfFunctionExist(functionName, argNum)) {
					this.generateErrorMessage("NameError::The function " + functionName + " is already declared!");
				} else {
					// add the function information to the symbol table
					table.addFunction(functionName, argNum);
				}
			}

		} else {
			this.generateErrorMessage("SyntaxError::Function should contain \"(\" for arguments!");
			return;
		}

		// check if the function contains the "return" statement
		if (functionStr.contains("return")) {
			String[] functionStrArr = functionStr.split("return");
			int endIndex = functionStrArr.length;

			this.lexer.parseLine(functionStrArr[0]);

			// use for loop to iterate the functionStrArr
			for (int i = 1; i < endIndex; i++) {
				if (!parseReturnStatement(functionStrArr[i])) {
					return;
				}
			}

		} else {
			// parse the function string
			this.lexer.parseLine(this.functionStr);
		}

		lexemeList = lexer.getLexemeList();
		Lexemes lexeme = new Lexemes();
		lexeme.insertLexeme("Function", functionName);

		lexemeList.add(0, lexeme);
		lexeme = new Lexemes();
		lexeme.insertLexeme("Function_END");
		lexemeList.add(lexeme);

		changeState = true;
		nextState = STATEMENT_SUCCESS;
	}

	/**
	 * Parses the "return" statement of the procedure.
	 * @param stmt - statement string
	 * @return If the given statement is valid, returns true. Otherwise, returns false.
	 */
	private boolean parseReturnStatement(String stmt) {
		String[] stmtArr = stmt.split(";");

		if (stmtArr.length > 1) {
			StringBuilder sb = new StringBuilder();

			int finalIndex = stmtArr.length - 1;

			for (int i = 1; i < finalIndex; i++) {
				sb.append(stmtArr[i]);
				sb.append(";");
			}

			sb.append(stmtArr[finalIndex]);

			Lexemes lexemeForReturnStatement = new Lexemes();
			lexemeForReturnStatement.insertLexeme("RETURN");

			// validate the return statement, and generate lexeme tokens
			ExpressionUtils.addTokensToLexemes(lexemeForReturnStatement, stmtArr[0].trim(), table);

			//insert the lexeme tokens of the return statement to Lexer's lexemeList
			lexer.insertLexeme(lexemeForReturnStatement);

			// parse the remaining lines.
			lexer.parseLine(sb.toString());
		} else {
			this.generateErrorMessage("SyntaxError::Invalid syntax near return statement of function \"" + functionName + "\"");
			return false;
		}

		return true;
	}

	/**
	 * Validate the argument string for the function.
	 * @param arg - argument string
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateArgumentString(String arg) {
		//check if the function has no argument
		if (arg.replace("(", "").replace(")", "").trim().equals("")) {
			return true;
		}

		if (arg.contains("var")) {
			String argStr = arg.replace("var", "").trim();

			// check if the argument name is valid
			if (argStr.contains(" ")) {
				this.generateErrorMessage("NameError::Invalid variable (argument) name : " + arg);
				return false;
			}

			// check if the function has duplicating arguments
			if (temp.contains(argStr)) {
				this.generateErrorMessage("SyntaxError::Function " + this.functionName + " has duplicating argument : \"" + argStr + "\"");
				return false;
			}

			temp.addSymbol(argStr, null);
			table.addSymbol(this.functionName + "@" + argStr.trim(), null);

			String regex = "\\W" + argStr + "\\W";
			replaceStringByPattern(regex, argStr);

			return true;
		} else {
			this.generateErrorMessage("SyntaxError::Missing \"var\" for the function argument - " + arg);
			return false;
		}
	}

	/**
	 * Check the pattern by using given regular expression string, and replace the argument name with suitable string.
	 * @param rx - regular expression string
	 * @param argName - argument variable name
	 */
	private void replaceStringByPattern(String rx, String argName) {
		StringBuffer sb = new StringBuffer();
		Pattern p = Pattern.compile(rx);
		Matcher m = p.matcher(functionStr);

		// iterate the matcher to find all substrings that match with the given regular expression
		while (m.find()) {
		    int startIndex = m.start();
		    int endIndex = m.end();
		    sb.append(functionStr.substring(0, startIndex));

		    try {
			    if (functionStr.substring(startIndex, endIndex).contains(";")) {
			    	sb.append(" " + functionName + "@" + argName + ";");
			    } else {
			    	sb.append(" " + functionName + "@" + argName + " ");
			    }
		    } catch (IndexOutOfBoundsException e) {
		    	if (functionStr.substring(startIndex, endIndex - 1).contains(";")) {
			    	sb.append(" " + functionName + "@" + argName + ";");
			    } else {
			    	sb.append(" " + functionName + "@" + argName + " ");
			    }
		    }

		    functionStr = functionStr.substring(endIndex);
		}

		sb.append(functionStr);

		functionStr = sb.toString();
	}

	/**
	 * Get the index of the argument section from the given function string.
	 * @param functionStr - character stream of function.
	 * @return If there is no error, returns the ending index of argument section. Otherwise, returns -1.
	 */
	private int getIndexOfArgumentsFromFunctionString(String functionStr) {
		int i;
		boolean checker = true;

		// use for loop to iterate the function string.
		for (i = 0; i < functionStr.length(); i++) {
			if (functionStr.charAt(i) == ')') {
				checker = false;
				break;
			}
		}

		// check if the functionStr contains the right parenthesis
		if (checker) {
			this.generateErrorMessage("SyntaxError::Cannot find \")\" for function!");
			return -1;
		}

		i += 1;

		return i;
	}

	/**
	 * Print out the error message, and change the state to error state.
	 * @param errorMsg - error message
	 */
	private void generateErrorMessage(String errorMsg) {
		System.out.println(errorMsg);
		changeState = true;
		nextState = BACK_TO_STATEMENT;
	}

	/**
	 * Getter for lexemeList.
	 * @return lexemeList
	 */
	public ArrayList<Lexemes> getLexemeList() {
		return this.lexemeList;
	}

	@Override
	public String getNextState() {
		return this.nextState;
	}

}
