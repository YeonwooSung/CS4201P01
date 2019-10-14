import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	/**
	 * Validates the if statement.
	 * @param line - read line
	 * @param sb - string builder to store the if statement.
	 * @return If valid, returns true. Otherwise, returns false.
	 */
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

	/**
	 * Validate the given line by checking the number of semicolons.
	 * @param line - read line
	 * @return If the line contains multiple semicolons like "var a := 1 ;;", false will be returned.
	 * 			Otherwise, returns true.
	 */
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
		BufferedReader br;

		SymbolTable table = new SymbolTable();
		Lexer lex = new Lexer(table);

		// use try-catch statement to handle IOExeption and NullPointerException
		try {
			if (args.length > 0) {
				br = new BufferedReader(new FileReader(new File(args[0])));
			} else {
				br = new BufferedReader(new InputStreamReader(System.in));
			}

			String line;

			while (!lex.isFinished()) {
				// check if the read line is null
				if ((line = br.readLine()) != null) {
					line = line.trim();
				} else{
					break;
				}

				if (!checkNumberOfSemiColons(line) || line.matches("\\s+") || line.equals("")) {
					continue;
				}

				// check if the line starts with if statement
				if (line.startsWith("if")) {
					String str = line.replace("if", "").trim();

					StringBuilder sb = new StringBuilder();
					boolean checker = checkIfStatementEnds(str, sb);

					// use while statement to read lines until the if statement ends
					while (!checker) {
						line = br.readLine().trim();

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
						while (!(line = br.readLine().trim()).contains("end")) {
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
					int count = line.length() - line.replace("\"", "").length();

					// check the number of
					if (count % 2 != 0) {
						System.out.println("SyntaxError::Invalid number of double quotation mark - expected = " + (count + 1) + " actual = " + count);
						continue;
					}

				}

				lex.parseLine(line.trim());
			}

			br.close(); //close buffered reader
		} catch (NullPointerException e) {
			System.out.println("Error::Source code did not end properly!");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// check the lexer state to check if the Oreo source code ends correctly
		if (!lex.isFinished()) {
			System.out.println("SyntaxError::Invalid number of \"end\"!");
			System.exit(1);
		}

		String programName = lex.getProgramName();
		ArrayList<Lexemes> lexemeList = lex.getLexemeList();

		// generate the Lexeme object that contians all lexeme tokens that the lexer generated
		Lexemes lexeme = generateFinalLexemes(lexemeList);

		// generate the Parser that will generate the AST
		SyntacticalParser parser = new SyntacticalParser(lexeme.getLexemeList(), programName);
		AbstractSyntaxTreeNode node = parser.parse(0);

		System.out.println("\n\nAST for program \"" + programName + "\"\n");
		node.printOutChildren();
	}

}
