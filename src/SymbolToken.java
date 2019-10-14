
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
	SymbolToken(String name) {
		this.name = name;
		this.value = null;
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

		if (name != null) {
			builder.append(name);
			if (value != null) {
				builder.append(" ");
				builder.append(value);
			}
		}

		return builder.toString();
	}

	/**
	 * Check if the value is equal to given string.
	 * @param s - string
	 * @return If true, returns true. Otherwise, returns false.
	 */
	public boolean isValueEqualTo(String s) {
		if (value != null) {
			return value.equals(s);
		}

		return false;
	}

	/**
	 * Check if the name is equal to given string.
	 * @param s - string
	 * @return If true, returns true. Otherwise, returns false.
	 */
	public boolean isNameEqualTo(String s) {
		if (name != null) {
			return name.equals(s);
		}

		return false;
	}

	/**
	 * Getter for name
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for value.
	 * @return value
	 */
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
