import java.util.Scanner;

public class Analyser {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		SymbolTable table = new SymbolTable();
		Lexer lex = new Lexer(table);

		while (!lex.isFinished()) {
			String line = sc.nextLine();

			// check if the read line is whitespace
			if (line.matches("\\s+") || line.equals("")) {
				System.out.println("whitespace!");
				continue;
			}

			//TODO System.out.println("!!" + line + "!!");

			// check if the read line contains double quote character
			if (line.contains("\"")) {
				StringBuilder builder = new StringBuilder(line);
				int count = line.length() - line.replace("\"", "").length();

				// if the number of double quote characters is odd number, we need to read more lines until we get remaining double quote character
				if (count % 2 != 0) {
					while (true) {
						String newLine = sc.nextLine();
						builder.append(newLine);

						int c = newLine.length() - newLine.replace("\"", "").length();

						if (c % 2 != 0) {
							break;
						}
					}
				}

				lex.parseLine(builder.toString().trim());
			} else {
				lex.parseLine(line.trim());
			}
		}

		sc.close();
	}

}
