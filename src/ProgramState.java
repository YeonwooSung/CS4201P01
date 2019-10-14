
public class ProgramState implements LexerFSA {
	private boolean isProgramFound;
	private boolean isProgramNameFound;
	private boolean changeState;
	private String programName;

	private final String NEXT_STATE = "Compound";

	ProgramState() {
		isProgramFound = false;
		isProgramNameFound = false;
		changeState = false;
		programName = null;
	}

	public String getProgramName() {
		return programName;
	}

	@Override
	public String getNextState() {
		return NEXT_STATE;
	}

	@Override
	public boolean isAbleToChangeState() {
		return this.changeState;
	}

	@Override
	public void parseWord(String word) {
		if (!isProgramFound) {
			if (word.equals("program")) {
				isProgramFound = true;
			} else {
				System.out.println("SyntaxError::Program should starts with keyword \"program\"!");
			}
		} else if (!isProgramNameFound) {
			if (validateName(word)) {
				programName = new String(word);
				isProgramNameFound = true;
				changeState = true;
			}
		} else {
			System.out.println("Error::something went wrong::ProgramState.parseWord()");
			changeState = true;
		}
	}

	/**
	 * Validate the program name.
	 * @param str - program name
	 * @return If valid, returns true. Otherwise, returns false.
	 */
	private boolean validateName(String str) {
		if (str.equals("program") || str.equals("begin") || str.equals("end") || str.equals("if") || str.equals("else") || str.equals("while") || str.equals("print") || str.equals("println") || str.equals("procedure") || str.equals("get") || str.equals("function")) {
			System.out.println("KeywordError::Cannot use the keyword string as a name of the program!");
			isProgramFound = false;
			return false;
		}

		return true;
	}
}
