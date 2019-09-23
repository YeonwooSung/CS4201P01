import java.util.ArrayList;
import java.util.HashMap;


public class SymbolTable {
	private HashMap<String, String> hm = new HashMap<>();
	private ArrayList<LexerToken> list = new ArrayList<>();

	public boolean contains(String key) {
		return hm.containsKey(key);
	}

	public int addSymbol(String key, String value) {
		int id = list.size();
		list.add(new LexerToken(key, value)); //TODO
		hm.put(key, value);

		return id;
	}
}
