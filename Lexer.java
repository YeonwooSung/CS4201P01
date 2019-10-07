import java.util.ArrayList;
import java.util.List;

public class Lexer {
	private boolean finished;
	private boolean isCommented;
	private boolean ifStatementMode;
	private boolean functionMode;
	private boolean isForFunctionBody;
	private int compoundLevel; //to check nested compounds

	//private SymbolTable table;
	private LexerFSA currentState;

	private ArrayList<CompoundState> compoundList;
	private ArrayList<StatementState> statementList;
	private ArrayList<Lexemes> lexemeList;

	private SymbolTable table;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String V_STATEMENT_SUCCESS = "V - STMT SUCCESS";
	private final String PR_STATEMENT_SUCCESS = "PR - STMT SUCCESS";
	private final String A_STATEMENT_SUCCESS = "A - STMT SUCCESS";
	private final String I_STATEMENT_SUCCESS = "I - STMT SUCCESS";
	private final String F_STATEMENT_SUCCESS = "F - STMT SUCCESS";
	private final String COMP_END_STATE = "Compound - END";
	private final String COMP_WHILE_STATE = "WHILE_COMPOUND";
	private final String V_STATE = "Var";
	private final String PR_STATE = "Print";
	private final String W_STATE = "While";
	private final String I_STATE = "If";
	private final String A_STATE = "Assign";
	private final String F_STATE = "Function";

	private final String PRINT_CMD = "print";
	private final String PRINTLN_CMD = "println";
	private final String GET_CMD = "get";

	private final LexerFSA PROGRAM_STATE;

	Lexer(SymbolTable table) {
		finished = false;
		isCommented = false;
		ifStatementMode = false;
		isForFunctionBody = false;

		compoundLevel = -1;
		PROGRAM_STATE = new ProgramState();
		currentState = PROGRAM_STATE;

		compoundList = new ArrayList<>();
		compoundList.add(new CompoundState());

		statementList = new ArrayList<>();
		lexemeList = new ArrayList<>();

		this.table = table;
	}

	Lexer(SymbolTable table, boolean isForFunctionBody) {
		this(table);
		this.isForFunctionBody = isForFunctionBody;
	}

	/**
	 * Parse one line of the source code.
	 * @param line - source code line
	 */
	public void parseLine(String line) {
		// validate the variable declaring statement.
		if (line.contains("var")) {
			// check if the variable declaring line does not have the semicolon
			if (!line.contains(";")) {
				System.out.println("SyntaxError::Missing \";\"");
				return;
			}

		} else if (line.contains(":=")) {
			if (!line.contains(";")) {
				System.out.println("SyntaxError::Missing \";\"");
				return;
			}
		}

		// validate the syntax of print statements
		if (line.contains(PRINTLN_CMD)) {
			if (!validateSyntaxOfPrintStatement(line, PRINTLN_CMD)) {
				return;
			}
		} else if (line.contains(PRINT_CMD)) {
			if (!validateSyntaxOfPrintStatement(line, PRINT_CMD)) {
				return;
			}
		} else if (line.contains(GET_CMD)) {
			if (!this.validateSyntaxOfGetCommand(line)) {
				return;
			}
		}

		String[] words = {};

		if (line.contains("\"")) {
			String[] sa = line.split("\\s+");
			List<String> list = new ArrayList<String>();
			appendStringsToList(sa, list);
			words = list.toArray(words);

		} else {
			words = line.split("\\s+");
		}

		// iterate the array of words in the read line
		for (int i = 0; i < words.length; ++i) {
			String word = words[i];

			// check if it is a comment
			if (isCommented) {
				if (word.contains("-}")) {
					isCommented = false; //check if the comment ends

					if (!word.endsWith("-}")) {
						String[] splitted = word.split("-\\}");
						parseLoop(splitted[1]);
					}
				}
			} else {

				//check comments
				if (word.contains("{-")) {
					isCommented = true;

					if (word.contains("-}")) {
						if (!word.endsWith("-}")) {
							String[] splitted = word.split("-\\}");
							parseLoop(splitted[1]);
						}

						isCommented = false;
					} else {
						if (!word.startsWith("{-")) {
							String[] splitted = word.split("\\{-");
							parseLoop(splitted[0]);
						}
					}

				} else {
					parseLoop(word);
				}

			}
		}
	}

	/**
	 * Parse the word, and change the state of FSA.
	 * @return Returns true if we need to re-parse the word. Otherwise, returns false.
	 */
	public boolean changeState() {
		// check if it is able to change the state of the lexer's FSA
		if (currentState.isAbleToChangeState()) {
			String nextState = currentState.getNextState();

			/*
			 * Use if-else statements to check the name of next state.
			 * Then, change to the corresponding state.
			 */
			if (nextState.equals("Compound")) {
				compoundLevel += 1;

				//TODO test
				if (compoundLevel != (compoundList.size() - 1)) {
					System.out.println("ERROR!!!! - TEST in parse() -> Compound");
				}

				currentState = compoundList.get(compoundLevel);
			} else if (nextState.equals("END")) {
				compoundList.remove(compoundLevel);
				statementList.remove(compoundLevel);
				compoundLevel -= 1;

				if (compoundLevel >= 0) {
					currentState = compoundList.get(compoundLevel);
				} else {
					finished = true;
				}

			} else if (nextState.equals("Statement")) {
				CompoundState state = (CompoundState) currentState;
				state.init();

				if (state.getMode() != null) {
					statementList.add(new StatementState(table, isForFunctionBody, state.getMode()));
				} else {
					statementList.add(new StatementState(table, isForFunctionBody));
				}

				//TODO test
				if (compoundLevel != (statementList.size() - 1)) {
					System.out.println("ERROR!!!! - TEST in parse() -> Statement");
				}

				currentState = statementList.get(compoundLevel);
			} else if (nextState.equals(COMP_END_STATE)) {
				currentState = compoundList.get(compoundLevel);

				//returns true to let the lexer know that the given word should be re-parsed with a new state.
				return true;

			} else if (nextState.equals(V_STATE)) {
				currentState = new VariableState(table);
			} else if (nextState.equals(PR_STATE)) {
				currentState = new PrintState(table);

				//returns true to let the lexer know that the given word should be re-parsed with a new state.
				return true;
			} else if (nextState.equals(W_STATE)) {
				currentState = new WhileState(table);

				//returns true to let the lexer know that the given word should be re-parsed with a new state.
				return true;
			} else if (nextState.equals(I_STATE)) {
				currentState = new IfState(table);
				ifStatementMode = true;

			} else if (nextState.equals(A_STATE)) {
				currentState = new AssignmentState(table);

				//returns true to let the lexer know that the given word should be re-parsed with a new state.
				return true;
			} else if (nextState.equals(F_STATE)) {
				currentState = new FunctionState(table);
				functionMode = true;

			} else if (nextState.equals(BACK_TO_STATEMENT)) {

				/*
				 * If the lexer found some syntax error, the lexer's fsa will go back to the latest statement state.
				 *
				 * For example, if the user input "var test;test;", the lexer will fail to parse the variable name.
				 * Then, the lexer will change it's state to the Statement state.
				 * By doing this, user would be able to input the valid code.
				 */
				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes

			} else if (nextState.contains(V_STATEMENT_SUCCESS)) {
				lexemeList.add(((VariableState)currentState).getLexemes());

				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes

			} else if (nextState.equals(PR_STATEMENT_SUCCESS)) {
				lexemeList.add(((PrintState)currentState).getLexemes());

				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes

			} else if (nextState.equals(A_STATEMENT_SUCCESS)) {
				lexemeList.add(((AssignmentState)currentState).getLexemes());

				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes

			} else if (nextState.equals(I_STATEMENT_SUCCESS)) {
				lexemeList.add(((IfState) currentState).getLexemes());

				//get array list of Lexemes objects from IfState instance.
				ArrayList<Lexemes> list = ((IfState) currentState).getLexemeList();
				this.mergeLexemeLists(list); //merge 2 ArrayList<Lexemes> objects

				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes

			} else if (nextState.equals(F_STATEMENT_SUCCESS)) {
				//TODO add lexemes to lexemeList

				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes

			} else if (nextState.equals(COMP_WHILE_STATE)) {
				compoundLevel += 1;
				this.compoundList.add(new CompoundState("while"));

				currentState = this.compoundList.get(compoundLevel);
				//TODO lexemes ??

				//returns true to let the lexer know that the given word should be re-parsed with a new state.
				return true;

			} else {
				System.out.print("StateNameError::Invalid state name (");
				System.out.print(nextState);
				System.out.println(")");
			}

		}

		return false;
	}

	/**
	 * Merge lexeme lists.
	 * @param list - ArrayList of Lexemes object.
	 */
	private void mergeLexemeLists(ArrayList<Lexemes> list) {
		for (Lexemes l : list) {
			this.lexemeList.add(l);
		}
	}

	/**
	 * Loop until the "parseAndchangeState()" method returns false.
	 * @param word - Target word that should be parsed.
	 */
	public void parseLoop(String word) {
		boolean checker = true;

		do {
			currentState.parseWord(word); //parse the word
			checker = changeState();
		} while (checker);
	}

	/**
	 * This method supports the lexer to validate the syntax of the print statement.
	 * @param line - line to check
	 * @param command - either "println" or "print"
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateSyntaxOfPrintStatement(String line, String command) {
		if (line.contains(command)) {
			String target = line.replace(command, "").trim();
			if (target.contains(";")) {
				return true;
			} else {
				System.out.println("SyntaxError::Cannot find semicolon");
			}
		}

		return false;
	}

	/**
	 * This method helps the lexer to validate the syntax of the "get" command.
	 * @param line - the target source code to check the syntax
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateSyntaxOfGetCommand(String line) {
		String target = line.replace(GET_CMD, "").trim();

		// check if the string contains the semicolon
		if (target.contains(";")) {
			String name = target.replace(";", "");

			if (table.contains(name)) {
				// check if the variable is declared and defined
				try {
					table.getValueOf(name);

					return true;
				} catch(NullPointerException e) {
					System.out.println("NameError::Variable " + name + " is not declared");
				}
			} else {
				System.out.println("NameError::Variable " + name + " is not declared");
			}
		} else {
			System.out.println("SyntaxError::Cannot find semicolon");
		}

		return false;
	}

	/**
	 * Check the double quoted strings, build them by using StringBuilder, and append string tokens to the List.
	 * @param words - array that contains strings from character stream.
	 * @param list - list to contain stings
	 */
	public void appendStringsToList(String[] words, List<String> list) {
		for (int i = 0; i < words.length; i++) {
			int count = words[i].length() - words[i].replace("\"", "").length();
			if (words[i].contains("\"") && count % 2 != 0) {
				StringBuilder builder = new StringBuilder(words[i]);
				for (int j = i + 1; j < words.length; j++) {
					builder.append(" ");
					builder.append(words[j]);
					if (words[j].contains("\"")) {
						i = j;
						break;
					}
				}
				list.add(builder.toString());
			} else {
				list.add(words[i]);
			}
		}
	}

	/**
	 * Parse the if statement.
	 * @param line - character stream for if statement
	 */
	public void parseIfStatement(String line) {
		if (this.ifStatementMode) {
			IfState stmt = (IfState) currentState;
			stmt.processStatement(line);

			ifStatementMode = false;

			this.changeState();
		}
	}

	public void parseFunctionString(String functionStr) {
		if (functionMode) {
			FunctionState stmt = (FunctionState) currentState;
			stmt.parseFunctionString(functionStr);

			functionMode = false;

			this.changeState();
		}
	}

	/**
	 * Check if the lexer is finished.
	 * @return True if finished. Otherwise, returns false.
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Insert lexeme object to the lexemeList.
	 * @param lexeme - target object
	 */
	public void insertLexeme(Lexemes lexeme) {
		this.lexemeList.add(lexeme);
	}

	/**
	 * Getter for lexemeList.
	 * @return lexemeList
	 */
	public ArrayList<Lexemes> getLexemeList() {
		return lexemeList;
	}
}
