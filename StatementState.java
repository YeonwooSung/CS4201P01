
public class StatementState implements LexerFSA {
	private boolean changeState;
	private boolean isEmptyStmt;
	private String nextState;

	private final String END_STATE = "Compound - END";
	private final String V_STATE = "Var";
	private final String PR_STATE = "Print";
	private final String W_STATE = "While";
	private final String I_STATE = "If";
	private final String A_STATE = "Assign";

	private SymbolTable table;
	private LexerFSA currentState;

	StatementState(SymbolTable table) {
		changeState = false;
		currentState = null;
		isEmptyStmt = true;
		this.table = table;
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		changeState = true;

		if (word.equals("end")) {
			if (isEmptyStmt) {
				changeState = false;
				System.out.println("SyntaxError::Statement cannot be empty!");
			}

			nextState = END_STATE;
			return;
		} else if (word.equals("var")) {
			nextState = V_STATE;
		} else if (word.equals("print") || word.equals("get") || word.equals("println")) {
			//TODO
		} else if (word.equals("while")) {
			//TODO
		} else if (word.equals("if")) {
			//TODO
		} else {

			if (table.contains(word)) {
				//TODO "a"(id := expr)
			} else {
				changeState = false;
				System.out.println("NameError::Cannot find function or variable called \'" + word + "\'");
				return;
			}
		}

		isEmptyStmt = false;
	}

	@Override
	public String getNextState() {
		return nextState;
	}

}
