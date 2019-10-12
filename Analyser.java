import java.util.ArrayList;
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

	private static boolean checkNumberOfSemiColons(String line) {
		//check if the variable declaring line has multiple semicolons
		int counter = line.length() - line.replaceAll(";", "").length();
		if (counter > 1) {
			if (line.contains(";;")) {
				System.out.println("SyntaxError::Too many semicolons");
				return false;
			} else {
				Pattern pattern = Pattern.compile(";\\s+;");
				Matcher matcher = pattern.matcher(line);

				if (matcher.find()) {
					System.out.println("SyntaxError::Too many semicolons");
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Generate a one Lexeme object that contains all lexeme tokens that the lexer generated
	 * @param lexemeList - list of Lexemes object
	 * @return Lexemes object
	 */
	private static Lexemes generateFinalLexemes(ArrayList<Lexemes> lexemeList) {
		Lexemes lexemes = new Lexemes();

		// use for-each loop to iterate lexemeList
		for (Lexemes l : lexemeList) {
			ArrayList<SymbolToken> list = l.getLexemeList();

			for (SymbolToken token : list) {
				lexemes.insertLexeme(token);
			}
		}

		return lexemes;
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		SymbolTable table = new SymbolTable();
		Lexer lex = new Lexer(table);

		while (!lex.isFinished()) {
			String line = sc.nextLine().trim();

			if (!checkNumberOfSemiColons(line)) {
				continue;
			}

			// check if the read line is whitespace
			if (line.matches("\\s+") || line.equals("")) {
				continue;
			}

			// check if the line starts with if statement
			if (line.startsWith("if")) {
				String str = line.replace("if", "").trim();

				StringBuilder sb = new StringBuilder();
				boolean checker = checkIfStatementEnds(str, sb);

				while (!checker) {
					line = sc.nextLine();

					if (!checkNumberOfSemiColons(line)) {
						continue;
					}

					checker = checkIfStatementEnds(line.trim(), sb);
				}

				lex.parseLoop("if");
				lex.parseIfStatement(sb.toString());
				continue;
			}

			// check if the read line is for function declaration
			if (line.startsWith("procedure")) {
				String str = line.replace("procedure", "").trim();

				StringBuilder sb = new StringBuilder(str);

				// check if the string contains the word "end", which is the end of the function body.
				if (!str.contains("end")) {
					// use while loop to read line until the function body ends.
					while (!(line = sc.nextLine()).contains("end")) {
						if (!checkNumberOfSemiColons(line)) {
							continue;
						}

						sb.append(" ");
						sb.append(line.trim());
					}

					sb.append(" ");
					sb.append(line.trim());
				}

				String functionStr = sb.toString();

				if (!functionStr.contains("begin")) {
					System.out.println("SyntaxError::Function body should start with \"begin\"!");
					continue;
				}

				lex.parseLoop("procedure");
				lex.parseFunctionString(functionStr);
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

		ArrayList<Lexemes> lexemeList = lex.getLexemeList();

		for (Lexemes l : lexemeList) {
			//l.printAll();//TODO
		}

		// generate the Lexeme object that contians all lexeme tokens that the lexer generated
		Lexemes lexeme = generateFinalLexemes(lexemeList);

		// generate the Parser that will generate the AST
		Parser parser = new Parser(table, lexeme);
		AbstractSyntaxTreeNode node = parser.parse(0);
		node.printOutChildren(0);
	}

}
