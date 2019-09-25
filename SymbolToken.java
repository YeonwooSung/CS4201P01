
public class SymbolToken {
	private String name;
	private String value;

	/**
	 * Default constructor
	 */
	SymbolToken() {
		name = null;
		value = null;
	}

	/**
	 * This constructor sets the value of the token.
	 * @param value - The value of the lexer token
	 */
	SymbolToken(String value) {
		name = null;
		this.value = value;
	}

	/**
	 * This constructor sets both name and value of the token.
	 * @param name - The name of the lexer token
	 * @param value - The value of the lexer token
	 */
	SymbolToken(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * The setter for name.
	 * @param name - Name of the token.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The setter for value.
	 * @param value - Value of the token.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the token string.
	 * The format of the token string is "name_of_token value_of_token".
	 * @return The token string.
	 */
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

	public boolean isNameEqualTo(String s) {
		return name.equals(s);
	}

	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}

	/**
	 * Prints out the token string.
	 */
	public void printTokenString() {
		System.out.println(this.getTokenString());
	}
}
