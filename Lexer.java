
public class Lexer {
	private boolean finished;
	private boolean isCommented;
	private int compoundLevel; //to check nested compounds

	private SymbolTable table;
	private LexerFSA currentState;

	private final LexerFSA PROGRAM_STATE;
	private final LexerFSA COMPOUND_STATE;
	private final LexerFSA STATEMENT_STATE;

	private final String COMP_END_STATE = "Compound - END";
	private final String V_STATE = "Var";
	private final String PR_STATE = "Print";
	private final String W_STATE = "While";
	private final String I_STATE = "If";
	private final String A_STATE = "Assign";

	Lexer(SymbolTable table) {
		finished = false;
		isCommented = false;
		compoundLevel = 0;
		this.table = table;
		PROGRAM_STATE = new ProgramState();
		COMPOUND_STATE = new CompoundState();
		STATEMENT_STATE = new StatementState(table);
		currentState = PROGRAM_STATE;
	}

	public void parseLine(String line) {
		String[] words = line.split("\\s+");

		for (int i = 0; i < words.length; ++i) {
			String word = words[i];

			// check if it is a comment
			if (isCommented) {
				if (word.contains("-}")) {
					isCommented = false; //check if the comment ends

					if (!word.endsWith("-}")) {
						String[] splitted = word.split("-\\}");
						if (parse(splitted[1])) parse(splitted[1]);
					}
				}
			} else {

				//check comments
				if (word.contains("{-")) {
					isCommented = true;

					if (word.contains("-}")) {
						if (!word.endsWith("-}")) {
							String[] splitted = word.split("-\\}");
							if (parse(splitted[1])) parse(splitted[1]);
						}

						isCommented = false;
					} else {
						if (!word.startsWith("{-")) {
							String[] splitted = word.split("\\{-");
							if (parse(splitted[0])) parse(splitted[0]);
						}
					}

				} else {
					if (parse(word)) parse(word);
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
	public boolean parse(String word) {

		currentState.parseWord(word); //parse the word

		// check if it is able to change the state of the lexer's FSA
		if (currentState.isAbleToChangeState()) {
			String nextState = currentState.getNextState();

			//TODO compoundLevel

			if (nextState.equals("Compound")) {
				currentState = COMPOUND_STATE;
			} else if (nextState.equals("END")) {
				finished = true;
			} else if (nextState.equals("Statement")) {
				currentState = STATEMENT_STATE;
			} else if (nextState.equals(COMP_END_STATE)) {
				currentState = COMPOUND_STATE;
				return true;
			} else if (nextState.equals(V_STATE)) {
				//
			} else if (nextState.equals(PR_STATE)) {
				//
			} else if (nextState.equals(W_STATE)) {
				//
			} else if (nextState.equals(I_STATE)) {
				//
			} else if (nextState.equals(A_STATE)) {
				//
			} else {
				System.out.print("StateNameError::Invalid state name (");
				System.out.print(nextState);
				System.out.println(")");
			}

			//TODO increase compoundLevel if required
		}

		return false;
	}

	public boolean isFinished() {
		return finished;
	}
}
