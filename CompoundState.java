
public class CompoundState implements LexerFSA {
	private boolean isBeginFound;
	private boolean isEndFound;
	private boolean changeState;
	private String nextState;

	private final String END_OF_PROGRAM = "END";
	private final String STATEMENT_STATE = "Statement";

	CompoundState() {
		isBeginFound = false;
		isEndFound = false;
		changeState = false;
		nextState = STATEMENT_STATE;
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

	@Override
	public String getNextState() {
		this.changeState = false;
		return this.nextState;
	}

}
