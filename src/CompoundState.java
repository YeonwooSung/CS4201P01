
public class CompoundState implements LexerFSA {
	private boolean isBeginFound;
	private boolean isEndFound;
	private boolean isEndFound_while;
	private boolean changeState;
	private String nextState;
	private String mode;

	private final String END_OF_PROGRAM = "END";
	private final String STATEMENT_STATE = "Statement";

	CompoundState() {
		mode = null;
		isBeginFound = false;
		isEndFound = false;
		changeState = false;
		nextState = STATEMENT_STATE;
	}

	CompoundState(String mode) {
		this();
		this.mode = mode;
		isEndFound_while = false;
	}

	@Override
	public boolean isAbleToChangeState() {
		return this.changeState;
	}

	@Override
	public void parseWord(String word) {
		if (!isBeginFound) {

			// check if the given word is "begin"
			if (word.equals("begin")) {
				isBeginFound = true;
				changeState = true;
			} else {
				System.out.println("SyntaxError::The compound should starts with keyword \"begin\"!");
			}

		} else if (!isEndFound) {

			if (mode != null) {

				// check the mode
				if (mode.equals("while")) {
					if (this.isEndFound_while) {
						// check if the read word is ";"
						if (word.equals(";")) {
							changeState = true;
							nextState = END_OF_PROGRAM;
						} else {
							System.out.println("Failed to found \";\"");
						}

					} else if (word.equals("end;")) {
						changeState = true;
						isEndFound_while = true;
						nextState = END_OF_PROGRAM;
					} else if (word.equals("end")) {
						isEndFound_while = true;
					} else {
						System.out.println("SyntaxError::While statement should end with \"end;\"");
					}
				}

				return;
			}

			// check if the given word is "end"
			if (word.equals("end")) {
				isEndFound = true;
				changeState = true;

				nextState = END_OF_PROGRAM;
			} else {
				System.out.println("SyntaxError::The compound should ends with keyword \"end\"!");
			}

		}
	}

	public void init() {
		changeState = false;
		nextState = null;
	}

	@Override
	public String getNextState() {
		this.changeState = false;
		return this.nextState;
	}

	/**
	 * Getter for mode.
	 * @return mode
	 */
	public String getMode() {
		return mode;
	}
}
