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
		
		for (SymbolToken token : list) {
			if (token.isNameEqualTo(name)) break;
			index += 1;
		}

		return index;
	}

	public int addSymbol(String key, String value) {
		int id = symbolCounter++;
		list.add(new SymbolToken(key, value));
		hm.put(key, value);

		return id;
	}
}
