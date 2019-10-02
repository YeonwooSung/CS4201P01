import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
	private boolean finished;
	private boolean isCommented;
	private int compoundLevel; //to check nested compounds

	//private SymbolTable table;
	private LexerFSA currentState;

	private ArrayList<CompoundState> compoundList;
	private ArrayList<StatementState> statementList;
	private ArrayList<Lexemes> lexemeList;

	private SymbolTable table;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String V_STATEMENT_SUCCESS = "V - STMT SUCCESS";
	private final String COMP_END_STATE = "Compound - END";
	private final String COMP_WHILE_STATE = "WHILE_COMPOUND";
	private final String V_STATE = "Var";
	private final String PR_STATE = "Print";
	private final String W_STATE = "While";
	private final String I_STATE = "If";
	private final String A_STATE = "Assign";

	private final LexerFSA PROGRAM_STATE;

	Lexer(SymbolTable table) {
		finished = false;
		isCommented = false;
		compoundLevel = -1;
		PROGRAM_STATE = new ProgramState();
		currentState = PROGRAM_STATE;

		compoundList = new ArrayList<>();
		compoundList.add(new CompoundState());

		statementList = new ArrayList<>();
		lexemeList = new ArrayList<>();

		this.table = table;
	}

	public void parseLine(String line) {
		String[] words = {};

		if (line.contains("\"")) {
			String[] sa = line.split("\\s+");
			List<String> list = new ArrayList<String>();
			appendStringsToList(sa, list);
			words = list.toArray(words);

		} else {
			words = line.split("\\s+");
		}

		if (line.contains("var")) {
			// check if the variable declaring line does not have the semi colon
			if (!line.contains(";")) {
				System.out.println("SyntaxError::Missing \";\"");
				return;
			} else {

				//check if the variable declaring line has multiple semi colons
				int counter = line.length() - line.replaceAll(";", "").length();
				if (counter != 1) {
					System.out.println("SyntaxError::Too much semi-colons -> expected = 1, actual = " + counter);
					return;
				}
			}
		}

		//TODO
		if (line.contains("print")) {
			String regex = "print \"(?:[^\"]|\\b\"\\b)+\"\\S+;";

			if (!line.contains(";")) {
				System.out.println("SyntaxError::Cannot find semi colon!");
				return;
			} else if (!line.matches(regex)) {
				System.out.println("SyntaxError::The format of the print statement = print \"message\"");
				return;
			}
		}

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
	 *
	 * @param word - The word to parse.
	 * @return Returns true if we need to re-parse the word. Otherwise, returns false.
	 */
	public boolean parseAndChangeState(String word) {

		currentState.parseWord(word); //parse the word

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
					statementList.add(new StatementState(table, state.getMode()));
				} else {
					statementList.add(new StatementState(table));
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
				//
			} else if (nextState.equals(W_STATE)) {
				currentState = new WhileState(table);

				//returns true to let the lexer know that the given word should be re-parsed with a new state.
				return true;
			} else if (nextState.equals(I_STATE)) {
				//
			} else if (nextState.equals(A_STATE)) {
				//
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

			} else if (nextState.equals(V_STATEMENT_SUCCESS)) {
				lexemeList.add(((VariableState)currentState).getLexemes());

				currentState = statementList.get(compoundLevel);
				((StatementState) currentState).init(); //init the attributes
				System.out.println(lexemeList.size());

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
	 * Loop until the "parseAndchangeState()" method returns false.
	 * @param word - Target word that should be parsed.
	 */
	public void parseLoop(String word) {
		boolean checker = true;

		do {
			checker = parseAndChangeState(word);
		} while (checker);
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
	 * Check if the lexer is finished.
	 * @return True if finished. Otherwise, returns false.
	 */
	public boolean isFinished() {
		return finished;
	}
}
