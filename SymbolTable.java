import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolTable {
	private HashMap<String, String> hm;
	private ArrayList<SymbolToken> list;
	private ArrayList<SymbolToken> functionList;
	private int symbolCounter;

	SymbolTable() {
		hm = new HashMap<>();
		list = new ArrayList<>();
		functionList = new ArrayList<>();
		symbolCounter = 0;
	}

	/**
	 * Gets the value of the variable with the given name.
	 * @param varName - name of the target variable.
	 * @return Returns the value of the variable. If the variable is not defined, returns null.
	 * @throws NullPointerException - If the variable is not declared, throws the NullPointerException.
	 */
	public String getValueOf(String varName) throws NullPointerException {
		if (hm.containsKey(varName)) {
			return hm.get(varName);
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * Check if the given variable name is already used.
	 * If the variable name is already used, that means that
	 * the user already declared this variable.
	 *
	 * As you cannot declare multiple variables with same name,
	 * thus, we need to check if this variable name is already used.
	 *
	 * @param key
	 * @return
	 */
	public boolean contains(String key) {
		return hm.containsKey(key);
	}

	/**
	 * Find and return the id of the given variable name.
	 * @param name
	 * @return
	 */
	public int getIdOfVariable(String name) {
		int index = 0;
		boolean checker = false;

		for (SymbolToken token : list) {
			if (token.isNameEqualTo(name)) {
				checker = true;
				break;
			}
			index += 1;
		}

		if (checker) {
			return index;
		} else {
			return -1;
		}
	}

	/**
	 * Remove the given name of variable from the symbol table.
	 * @param key - The key of the hash map, which is the name of the variable.
	 */
	public void removeSymbol(String key) {
		if (!hm.containsKey(key)) return;
		hm.remove(key);
		int index = 0;

		for (SymbolToken token : list) {
			if (token.isNameEqualTo(key)) break;
			index += 1;
		}

		list.remove(index);
	}

	/**
	 * Adds the variable to the symbol table.
	 * @param key - The key of the hash map, which is the name of the variable.
	 * @param value - The value of the variable.
	 * @return The symbolic id of the variable.
	 */
	public int addSymbol(String key, String value) {
		int id = symbolCounter++;
		list.add(new SymbolToken(key, value));
		hm.put(key, value);

		return id;
	}

	/**
	 * Add a function to the array list.
	 * @param functionName - name of the function
	 * @param numOfArgs - number of the arguments
	 */
	public void addFunction(String functionName, int numOfArgs) {
		SymbolToken functionToken = new SymbolToken();
		functionToken.setName(functionName);
		functionToken.setValue(((Integer)numOfArgs).toString());
		this.functionList.add(functionToken);
	}

	/**
	 * Check whether the given name of function already exists.
	 * @param functionName - name of the function
	 * @return If exists, returns true. Otherwise, returns false.
	 */
	public boolean checkIfFunctionExist(String functionName, int numOfArgs) {
		boolean checker = false;
		String numOfArguments = ((Integer)numOfArgs).toString();

		// iterate the array list of SymbolToken objects to check whether the given function is already declared
		for (SymbolToken token : functionList) {
			String str = token.getName();
			String val = token.getValue();

			if (str.equals(functionName) && val.equals(numOfArguments)) {
				checker = true;
				break;
			}
		}

		return checker;
	}

	public String checkFunction(String expression) {
		String newExpression = expression;

		// use for-each loop to iterate the list of functions to check the function call
		for (SymbolToken token : functionList) {
			String functionName = token.getName();
			int argNum = Integer.parseInt(token.getValue());

			// check if the expression contains the function name
			if (expression.contains(functionName)) {
				StringBuilder regexBuilder = new StringBuilder(functionName);
				regexBuilder.append("\\(");

				for (int a = 0; a < argNum; a++) {
					regexBuilder.append("\\s*\\w+\\s*");
					if (a != argNum - 1) {
						regexBuilder.append(",");
					}
				}

				regexBuilder.append("\\)");
				String regex = regexBuilder.toString();

				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(newExpression);

				boolean checker = false;

				if (expression.contains(functionName + "(")) {
					checker = true;
				}

				StringBuilder sb = new StringBuilder();

				// use while loop to get all regular expression strings
				while (m.find()) {
					checker = false;
					int startIndex = m.start();
				    int endIndex = m.end();

				    if (startIndex != 0) {
				    	sb.append(newExpression.substring(0, startIndex));

				    	String subStr = newExpression.substring(startIndex, endIndex);
				    	subStr = subStr.replace("(", "{").replace(")", "}");

				    	sb.append("FunctionCall@");
				    	sb.append(subStr);

				    	if (endIndex < newExpression.length() - 1) {
				    		sb.append(newExpression.substring(endIndex));
				    	}
				    } else {
				    	String subStr = newExpression.substring(startIndex, endIndex);
				    	subStr = subStr.replace("(", "{").replace(")", "}");

				    	sb.append("FunctionCall@");
				    	sb.append(subStr);

				    	if (endIndex < newExpression.length() - 1) {
				    		sb.append(newExpression.substring(endIndex));
				    	}
				    }

				    newExpression = sb.toString();

				    if (newExpression.contains(";")) newExpression.replace(";", "");
				}

				if (checker) {
					return null;
				} else {
					break;
				}
			}
		}

		return newExpression;
	}

}
