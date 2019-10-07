import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {
	private HashMap<String, String> hm;
	private ArrayList<SymbolToken> list;
	private int symbolCounter;

	SymbolTable() {
		hm = new HashMap<>();
		list = new ArrayList<>();
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
}
