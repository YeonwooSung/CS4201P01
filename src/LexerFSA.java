
public interface LexerFSA {
	public boolean isAbleToChangeState(); //check if it is okay to change the state to the next state

	public void parseWord(String word); //parse the word to token

	public String getNextState(); //get next state's name
}
