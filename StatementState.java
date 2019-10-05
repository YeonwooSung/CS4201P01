
public class StatementState implements LexerFSA {
	private boolean changeState;
	private boolean isEmptyStmt;
	private String nextState;
	private String mode;

	private final String END_STATE = "Compound - END";
	private final String V_STATE = "Var";
	private final String PR_STATE = "Print";
	private final String W_STATE = "While";
	private final String I_STATE = "If";
	private final String A_STATE = "Assign";

	private SymbolTable table;

	StatementState(SymbolTable table) {
		changeState = false;
		isEmptyStmt = true;
		mode = null;

		this.table = table;
	}

	StatementState(SymbolTable table, String mode) {
		this(table);
		this.mode = mode;
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		changeState = true;

		// check if the string contains the word "end", which is a keyword that finishes the compound
		if (word.contains("end")) {
			if (mode != null) {

				// check the mode
				if (mode.equals("while")) {
					// validate the word
					if (word.equals("end;") || word.equals("end")) {
						if (isEmptyStmt) {
							changeState = false;
							System.out.println("SyntaxError::Statement cannot be empty!");
						}

						nextState = END_STATE;
						return;
					} else {
						System.out.println("SyntaxError::Unexpected word : " + word);
						return;
					}
				}

			} else if (word.equals("end")) { //check if the word is "end", which is a keyword that finishes the compound
				if (isEmptyStmt) {
					changeState = false;
					System.out.println("SyntaxError::Statement cannot be empty!");
				}

				nextState = END_STATE;
				return;
			} else {
				changeState = false;
				System.out.println("SyntaxError::The statement should end with \"end\"!");
				return;
			}
		} else if (word.equals("var")) {
			nextState = V_STATE;
		} else if (word.equals("print") || word.equals("get") || word.equals("println")) {
			nextState = PR_STATE;
		} else if (word.startsWith("while")) {
			nextState = W_STATE;
		} else if (word.startsWith("if")) {
			nextState = I_STATE;
		} else {

			// check if the word is a variable name
			if (table.contains(word)) {
				nextState = A_STATE;
			} else {
				changeState = false;
				System.out.println("NameError::Cannot find function or variable called \'" + word + "\'");
				return;
			}
		}

		isEmptyStmt = false;
	}

	public void init() {
		changeState = false;
		nextState = null;
	}

	@Override
	public String getNextState() {
		return nextState;
	}

}
