import java.util.ArrayList;

public class IfState implements LexerFSA {
	private boolean changeState;
	private boolean findBooleanExpression;
	private boolean hasElse;
	private boolean foundElse;
	private String nextState;
	private StringBuilder booleanExpression;
	private SymbolTable table;
	private Lexemes lexemes;
	private Lexer lexer;
	private Lexer elseLexer;
	private ArrayList<Lexemes> lexemeList;

	private final String BACK_TO_STATEMENT = "Back To Statement";
	private final String STATEMENT_SUCCESS = "I - STMT SUCCESS";

	IfState(SymbolTable table) {
		this.table = table;

		changeState = false;
		findBooleanExpression = false;
		hasElse = false;
		nextState = null;
		booleanExpression = null;
		lexemes = new Lexemes();
		lexer = new Lexer(table);
		lexer.parseLine("program SubProgramForIf");
	}

	@Override
	public boolean isAbleToChangeState() {
		return changeState;
	}

	@Override
	public void parseWord(String word) {
		if (findBooleanExpression) {
			if (hasElse) {
				parseWord_IfElse(word);
			} else {
				parseWord_If(word);
			}

		} else {
			findBooleanExpressionOfIfStatement(word);
		}
	}

	private void parseWord_IfElse(String word) {
		// check if the lexer found the else for the if-else statement
		if (foundElse) {
			if (!word.equals("end;")) {
				elseLexer.parseLoop(word);
			}
		} else {
			if (word.equals("else")) {
				foundElse = true;
				elseLexer = new Lexer(table);
				elseLexer.parseLine("program SubProgramForElse");
			} else {
				lexer.parseLoop(word);
			}
		}
	}

	/**
	 * Parse the word for if statement.
	 * @param word - character stream
	 */
	private void parseWord_If(String word) {
		if (!word.equals("end;")) {
			lexer.parseLoop(word);
		}
	}

	/**
	 * Process the if statement by parsing the given line.
	 * @param line - string that contains the if statement
	 */
	public void processStatement(String line) {
		if (line.contains("else")) {
			hasElse = true;
		}

		String [] words = line.split("\\s+");

		// use for loop to iterate all words
		for (int i = 0; i < words.length; i++) {
			String word = words[i].trim();

			if (word.equals("") || word.equals("\\s+")) continue;

			this.parseWord(word);
		}

		lexemeList = lexer.getLexemeList();

		// check if the statement has "else"
		if (hasElse) {
			ArrayList<Lexemes> lexemeList2 = elseLexer.getLexemeList();
			for (Lexemes l : lexemeList2) {
				lexemeList.add(l);
			}
		}

		Lexemes lexeme = new Lexemes();
		lexeme.insertLexeme("If_FIN");
		lexemeList.add(lexeme);

		changeState = true;
		nextState = STATEMENT_SUCCESS;
	}

	/**
	 * Parse the character stream and find the boolean expression for the if statement.
	 * @param word - character stream
	 */
	private void findBooleanExpressionOfIfStatement(String word) {
		if (booleanExpression != null) {
			if (word.equals("then")) {
				lexemes.insertLexeme("If");

				String booleanExpressionStr = booleanExpression.toString();

				// validate the boolean expression, add lexeme tokens to Lexemes object
				if (ExpressionUtils.addTokensToLexemes(lexemes, booleanExpressionStr.substring(0, booleanExpressionStr.length() - 1), table)) {
					findBooleanExpression = true;
					lexemes.insertLexeme("then");

				} else {
					this.generateErrorMessage("SyntaxError::Cannot find \"then\" for the if statement");
				}
			} else {
				booleanExpression.append(word);
			}
		} else {
			if (word.startsWith("(")) {
				booleanExpression = new StringBuilder(word.substring(1));
			} else {
				this.generateErrorMessage("SyntaxError::Cannot find \"(\" for the if statement!");
			}
		}
	}

	/**
	 * Generate error message, and change the state of the LexerFSA to StatementState.
	 * @param errorMessage - Error message to print out
	 */
	private void generateErrorMessage(String errorMessage) {
		changeState = true;
		nextState = BACK_TO_STATEMENT;
		System.out.println(errorMessage);
	}

	@Override
	public String getNextState() {
		return nextState;
	}

	/**
	 * Getter for lexemes.
	 * @return lexemes
	 */
	public Lexemes getLexemes() {
		return this.lexemes;
	}

	/**
	 * Getter for lexemeList.
	 * @return ArrayList of Lexemes type objects.
	 */
	public ArrayList<Lexemes> getLexemeList() {
		return lexemeList;
	}
}
