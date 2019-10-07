
public class FunctionReturnState implements LexerFSA {
	private boolean changeState;
	private String nextState;
	private SymbolTable table;


	@Override
	public boolean isAbleToChangeState() {
		return this.changeState;
	}

	@Override
	public void parseWord(String word) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getNextState() {
		return this.nextState;
	}

}
