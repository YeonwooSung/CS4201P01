
public class LexerToken {
	private String name;
	private String value;

	/**
	 * Default constructor
	 */
	LexerToken() {
		name = null;
		value = null;
	}

	/**
	 * This constructor sets the value of the token.
	 * @param value - The value of the lexer token
	 */
	LexerToken(String value) {
		name = null;
		this.value = value;
	}

	/**
	 * This constructor sets both name and value of the token.
	 * @param name - The name of the lexer token
	 * @param value - The value of the lexer token
	 */
	LexerToken(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getTokenString() {
		StringBuilder builder = new StringBuilder();

		if (value != null) {
			if (name != null) {
				builder.append(name);
				builder.append(" ");
			}
			builder.append(value);
		}

		return builder.toString();
	}

	public void printTokenString() {
		System.out.println(this.getTokenString());
	}
}
