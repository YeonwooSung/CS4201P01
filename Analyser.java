import java.util.Scanner;

public class Analyser {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		SymbolTable table = new SymbolTable();
		Lexer lex = new Lexer(table);

		while (!lex.isFinished()) {
			String line = sc.nextLine().trim();
			if (line.isEmpty()) continue;

			lex.parseLine(line);
		}
	}

}
