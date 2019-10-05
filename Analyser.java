import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Analyser {
	public static boolean checkIfStatementEnds(String line, StringBuilder sb) {
		if (line.contains("end;")) {
			sb.append(line);
			return true;
		} else if (line.contains("end")) {
			Pattern pattern = Pattern.compile("end\\s+;");
			Matcher matcher = pattern.matcher(line);

			// check if the target regular expression matches the read line contains
			if (matcher.find()) {
				int startIndex = matcher.start() - 1;

				String targetStr = line.substring(0, startIndex) + " end;";

				sb.append(targetStr);
				return true;
			}
		}

		sb.append(line);
		sb.append(" ");

		return false;
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		SymbolTable table = new SymbolTable();
		Lexer lex = new Lexer(table);

		while (!lex.isFinished()) {
			String line = sc.nextLine();

			// check if the read line is whitespace
			if (line.matches("\\s+") || line.equals("")) {
				continue;
			}

			if (line.contains("if")) {
				String str = line.replace("if", "");

				StringBuilder sb = new StringBuilder();
				boolean checker = checkIfStatementEnds(str, sb);

				while (!checker) {
					line = sc.nextLine();
					checker = checkIfStatementEnds(line.trim(), sb);
				}

				lex.parseLoop("if");
				lex.parseIfStatement(sb.toString());
				continue;
			}

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
