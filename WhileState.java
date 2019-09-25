
public class WhileState implements LexerFSA {
	private boolean changeState;
	private String nextState;

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		// TODO Auto-generated method stub
	}

	@Override
	public String getNextState() {
		return nextState;
	}

}
