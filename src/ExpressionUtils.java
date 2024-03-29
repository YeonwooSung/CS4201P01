public class ExpressionUtils {

	/**
	 * Add tokens to the lexeme list.
	 * @param lexemes - object that stores the lexeme tokens
	 * @param originalStr - expression string
	 * @param table - symbol table
	 * @param varID - id of the variable
	 * @return If the given expression is valid, returns true. Otherwise, returns false.
	 */
	public static boolean addTokensToLexemes(Lexemes lexemes, String expression, SymbolTable table, String varID) {		
		lexemes.insertLexeme("VAR");
		lexemes.insertLexeme("ID", varID);
		lexemes.insertLexeme("IS", "=");

		boolean checker = addTokensToLexemes(lexemes, expression, table);
		lexemes.insertLexeme("VAR_END");

		return checker;
	}

	/**
	 * Add tokens to the lexeme list.
	 * @param lexemes - object that stores the lexeme tokens
	 * @param originalStr - expression string
	 * @param table - symbol table
	 * @return If the given expression is valid, returns true. Otherwise, returns false.
	 */
	public static boolean addTokensToLexemes(Lexemes lexemes, String originalStr, SymbolTable table) {
		int counter_leftParen = originalStr.length() - originalStr.replaceAll("\\(", "").length();
		int counter_rightParen = originalStr.length() - originalStr.replaceAll("\\)", "").length();

		// check if the number of left parentheses and right parentheses are different
		if (counter_rightParen != counter_leftParen) {
			System.out.println("SyntaxError::Parenthesis not matching!");
			return false;
		}

		// check if the expression contains function call
		String expression = table.checkFunction(originalStr);

		/*
		 * SymbolTable.checkFunction(expression) method will return null
		 * if the expression string contains the function call with invalid number of arguments.
		 *
		 * So, by checking the return value of that method, we could validate the number of arguments
		 * that is required for the function call.
		 */
		if (expression == null) {
			System.out.println("SyntaxError::Function call failed");
			return false;
		}


		/* refine the expression string */

		if (expression.contains("(")) {
			expression = expression.replace("(", " (");
		}

		if (expression.contains(")")) {
			expression = expression.replace(")", " )");
		}

		Lexemes temp = new Lexemes();
		boolean checker = true;

		String[] leftParenArr = expression.split("\\(");
		int length1 = leftParenArr.length - 1;

		/* parse the expressions for the lexical analysis */

		outer:
		for (int a = 0; a <= length1; a++) {
			if (a != 0) {
				if (a != length1 || expression.endsWith("(" + leftParenArr[a])) {
					temp.insertLexeme("LPAREN");
				}
			}

			String expressionStr = leftParenArr[a].trim(); //remove whitespaces

			// check if the expressionStr is empty string.
			if (expressionStr.matches("\\s") || expressionStr.equals("")) continue;

			if (expressionStr.contains("+")) {

				String[] expressionArr = expressionStr.split("\\+");
				int finalIndex = expressionArr.length - 1;

				for (int i = 0; i <= finalIndex; i++) {
					String s = expressionArr[i];

					if (s.contains(")")) {
						String[] arr = s.split("\\)");
						int lengthOfArr = arr.length - 1;

						// use for loop to iterate String array "arr"
						for (int j = 0; j <= lengthOfArr; j++) {
							String str = arr[j].trim();

							if (str.equals("")) {
								temp.insertLexeme("RPAREN");
								continue;
							}

							// check if the expression string contains '-' character
							if (!splitBySubtractionAndAppendToLexemes(temp, str.trim(), table)) {
								checker = false;
								break outer;
							}

							if (j < lengthOfArr) {
								temp.insertLexeme("RPAREN");
							} else if (s.endsWith(")")) {
								temp.insertLexeme("RPAREN");
							}
						}

					} else {
						if (!splitBySubtractionAndAppendToLexemes(temp, s.trim(), table)) {
							checker = false;
							break outer;
						}
					}

					if (i < finalIndex) {
						temp.insertLexeme("AROP", "+");
					} else if (expressionStr.endsWith("+")) {
						temp.insertLexeme("AROP", "+");
					}
				}
			} else {
				String[] arr = expressionStr.split("\\)");
				int lengthOfArr = arr.length - 1;

				// check if the expression string has the ')' character
				if (expressionStr.contains(")")) {
					for (int j = 0; j <= lengthOfArr; j++) {
						String str = arr[j].trim();

						if (str.isEmpty()) {
							temp.insertLexeme("RPAREN");
							continue;
						}

						// check if the expression string contains the '-'
						if (!splitBySubtractionAndAppendToLexemes(temp, str.trim(), table)) {
							checker = false;
							break outer;
						}

						if (j < lengthOfArr) {
							temp.insertLexeme("RPAREN");
						} else if (expressionStr.endsWith(")")) {
							temp.insertLexeme("RPAREN");
						}
					}
				} else {
					for (int j = 0; j <= lengthOfArr; j++) {
						String str = arr[j].trim();

						if (str.isEmpty()) continue;

						// check if the expression string contains the '-'
						if (!splitBySubtractionAndAppendToLexemes(temp, str.trim(), table)) {
							checker = false;
							break outer;
						}
					}
				}
			}
		}

		// check if there was any error while parsing the expression
		if (checker) {
			lexemes.mergeLexemes(temp);
			return true;
		}

		return false;
	}

	/**
	 * Split the expression by operator '-', and append the tokens to the lexeme list.
	 * @param lexemes - the object contains the lexeme list.
	 * @param expression - expression string
	 * @param table - symbol table
	 * @return If the expression is valid, returns true. Otherwise, returns false.
	 */
	private static boolean splitBySubtractionAndAppendToLexemes(Lexemes lexemes, String expression, SymbolTable table) {
		if (expression.contains("-")) {
			String[] expressionArr = expression.split("-");
			int finalIndex = expressionArr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = expressionArr[i];
				if (!splitByMultiplicationAndAppendToLexemes(lexemes, s.trim(), table)) return false;

				if (i < finalIndex) {
					lexemes.insertLexeme("AROP", "-");
				} else if (expression.endsWith("-")) {
					lexemes.insertLexeme("AROP", "-");
				}
			}
		} else {
			if (!splitByMultiplicationAndAppendToLexemes(lexemes, expression.trim(), table)) return false;
		}

		return true;
	}

	/**
	 * Split the expression by operator '*', and append the tokens to the lexeme list.
	 * @param lexemes - the object contains the lexeme list.
	 * @param expression - expression string
	 * @param table - symbol table
	 * @return If the expression is valid, returns true. Otherwise, returns false.
	 */
	private static boolean splitByMultiplicationAndAppendToLexemes(Lexemes lexemes, String expression, SymbolTable table) {
		if (expression.contains("*")) {
			String[] expressionArr = expression.split("\\*");
			int finalIndex = expressionArr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = expressionArr[i];
				if (!splitByDivisionAndAppendToLexemes(lexemes, s.trim(), table)) return false;

				if (i < finalIndex) {
					lexemes.insertLexeme("AROP", "*");
				} else if (expression.endsWith("*")) {
					lexemes.insertLexeme("AROP", "*");
				}
			}
		} else {
			if (!splitByDivisionAndAppendToLexemes(lexemes, expression.trim(), table)) return false;
		}

		return true;
	}

	/**
	 * Split the expression by operator '/', and append the tokens to the lexeme list.
	 * @param lexemes - the object contains the lexeme list.
	 * @param expression - expression string
	 * @param table - symbol table
	 * @return If the expression is valid, returns true. Otherwise, returns false.
	 */
	private static boolean splitByDivisionAndAppendToLexemes(Lexemes lexemes, String expression, SymbolTable table) {
		if (expression.contains("/")) {
			String[] expressionArr = expression.split("/");
			int finalIndex = expressionArr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = expressionArr[i].trim();

				// check if the expression contains either relational operator or logical operator.
				if (s.contains(">") || s.contains("<") || s.contains("==") || s.contains("or") || s.contains("and") || s.contains("not")) {
					if (!checkNotOperations(s, lexemes, table)) return false;
				} else {
					// check if it is variable or constant
					if (table.contains(s)) {
						int id = table.getIdOfVariable(s);
						lexemes.insertLexeme("ID", ((Integer) id).toString());
					} else if (s.equals("ARG_START") || s.equals("ARG_END")) {
						lexemes.insertLexeme(s);
					} else if (s.startsWith("FunctionCall@")) {
						lexemes.insertLexeme(s);
					} else {
						try {
							// check if the current string is either empty or whitespace
							if (s.equals("") || s.matches("\\s+")) {
								if (expression.contains(s + "/")) {
									lexemes.insertLexeme("AROP", "/");
								}
								continue;
							} else if (s.contains(".")) {
								Double.parseDouble(s);
								lexemes.insertLexeme("CONST_NUM", s);
							} else {
								Integer.parseInt(s);
								lexemes.insertLexeme("CONST_NUM", s);
							}
						} catch (Exception e) {
							if (!checkNotOperations(s, lexemes, table)) return false;
						}
					}
				}

				if (i < finalIndex) {
					lexemes.insertLexeme("AROP", "/");
				} else if (expression.endsWith("/")) {
					lexemes.insertLexeme("AROP", "/");
				}
			}
		} else {

			/* check the type */

			if (expression.contains("\"")) {
				lexemes.insertLexeme("CONST_STR", expression);
			} else if (expression.trim().equals("true")) {
				lexemes.insertLexeme("CONST_BOOL", "true");
			} else if (expression.trim().equals("false")) {
				lexemes.insertLexeme("CONST_BOOL", "false");
			} else if (table.contains(expression)) {
				int id = table.getIdOfVariable(expression);
				lexemes.insertLexeme("ID", ((Integer) id).toString());

			} else if (expression.trim().equals("ARG_START") || expression.trim().equals("ARG_END")) {
				lexemes.insertLexeme(expression.trim());

			} else if (expression.trim().startsWith("FunctionCall@")) {
				lexemes.insertLexeme(expression.trim());

			} else {

				if (expression.contains(">") || expression.contains("<") || expression.contains("==") || expression.contains("or") || expression.contains("and") || expression.contains("not")) {
					if (!checkNotOperations(expression.trim(), lexemes, table)) return false;
				} else {

					// parse string to number to check if the expression is a number
					try {
						if (expression.equals("") || expression.matches("\\s+")) {
							return true;
						} else if (expression.contains(".")) {
							Double.parseDouble(expression);
							lexemes.insertLexeme("CONST_NUM", expression);
						} else {
							Integer.parseInt(expression);
							lexemes.insertLexeme("CONST_NUM", expression);
						}
					} catch (Exception e) {
						return checkNotOperations(expression, lexemes, table);
					}

				}
			}
		}

		return true;
	}

	/**
	 * Check if the expression contains the "not" operator.
	 * @param expression - target expression
	 * @param lexemes - Lexemes object
	 * @param table - Symbol table.
	 * @return If no error occurred, returns true. Otherwise, returns false.
	 */
	private static boolean checkNotOperations(String expression, Lexemes lexemes, SymbolTable table) {
		if (expression.contains("not")) {
			String[] arr = expression.split("not");
			int finalIndex = arr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = arr[i];

				if (table.contains(s)) {
					int id = table.getIdOfVariable(s);
					lexemes.insertLexeme("ID", ((Integer) id).toString());
				} else {

					if (s.equals("true")) {
						lexemes.insertLexeme("CONST_BOOL", "true");
					} else if (s.equals("false")) {
						lexemes.insertLexeme("CONST_BOOL", "false");
					} else {
						if (!checkAndOperations(s.trim(), lexemes, table)) return false;
					}
				}

				if (i < finalIndex) {
					lexemes.insertLexeme("LOGOP", "not");
				} else if (expression.endsWith("not")) {
					lexemes.insertLexeme("LOGOP", "not");
				}
			}

		} else {
			if (!checkAndOperations(expression.trim(), lexemes, table)) return false;
		}

		return true;
	}

	/**
	 * Check if the given expression contains "and" operator.
	 * @param expression - target expression
	 * @param lexemes - Lexemes object
	 * @param table - symbol table
	 * @return If no error occurred, returns true. Otherwise, returns false.
	 */
	private static boolean checkAndOperations(String expression, Lexemes lexemes, SymbolTable table) {
		if (expression.contains("and")) {
			String[] arr = expression.split("and");
			int finalIndex = arr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = arr[i];

				if (table.contains(s)) {
					int id = table.getIdOfVariable(s);
					lexemes.insertLexeme("ID", ((Integer) id).toString());
				} else {

					if (s.equals("true")) {
						lexemes.insertLexeme("CONST_BOOL", "true");
					} else if (s.equals("false")) {
						lexemes.insertLexeme("CONST_BOOL", "false");
					} else {
						if (!checkOrOperations(s.trim(), lexemes, table)) return false;
					}
				}

				if (i < finalIndex) {
					lexemes.insertLexeme("LOGOP", "and");
				} else if (expression.endsWith("and")) {
					lexemes.insertLexeme("LOGOP", "and");
				}
			}

		} else {
			if (!checkOrOperations(expression.trim(), lexemes, table)) return false;
		}

		return true;
	}

	/**
	 * Check if the given expression contains "or" operator.
	 * @param expression - target expression
	 * @param lexemes - Lexemes object
	 * @param table - symbol table.
	 * @return If no error occurred, returns true. Otherwise, returns false.
	 */
	private static boolean checkOrOperations(String expression, Lexemes lexemes, SymbolTable table) {
		if (expression.contains("or")) {
			String[] arr = expression.split("or");
			int finalIndex = arr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = arr[i];
				if (table.contains(s)) {
					int id = table.getIdOfVariable(s);
					lexemes.insertLexeme("ID", ((Integer) id).toString());
				} else {

					if (s.equals("true")) {
						lexemes.insertLexeme("CONST_BOOL", "true");
					} else if (s.equals("false")) {
						lexemes.insertLexeme("CONST_BOOL", "false");
					} else {
						if (!checkEqualsToOperations(s.trim(), lexemes, table)) return false;
					}

					// check if the current word is a variable
					if (i < finalIndex) {
						lexemes.insertLexeme("LOGOP", "or");
					} else if (expression.endsWith("or")) {
						lexemes.insertLexeme("LOGOP", "or");
					}
				}
			}

		} else {
			if (!checkEqualsToOperations(expression.trim(), lexemes, table)) return false;
		}

		return true;
	}

	/**
	 * Check if the given expression contains == operator.
	 * @param expression - target expression string
	 * @param lexemes - Lexemes object
	 * @param table - symbol table
	 * @return If no error occurred, returns true. Otherwise, returns false.
	 */
	private static boolean checkEqualsToOperations(String expression, Lexemes lexemes, SymbolTable table) {
		if (expression.contains("==")) {

			String[] arr = expression.split("==");
			int finalIndex = arr.length - 1;

			for (int i = 0; i <= finalIndex; i++) {
				String s = arr[i];

				// check if the current word is a variable
				if (table.contains(s)) {
					int id = table.getIdOfVariable(s);
					lexemes.insertLexeme("ID", ((Integer) id).toString());
				} else {

					if (s.equals("true")) {
						lexemes.insertLexeme("CONST_BOOL", "true");
					} else if (s.equals("false")) {
						lexemes.insertLexeme("CONST_BOOL", "false");
					} else {
						if (!checkRelationalOperations(s.trim(), lexemes, table))  return false;
					}
				}

				if (i < finalIndex) {
					lexemes.insertLexeme("RELOP", "==");
				} else if (expression.endsWith("==")) {
					lexemes.insertLexeme("RELOP", "==");
				}
			}

		} else {
			if (!checkRelationalOperations(expression.trim(), lexemes, table))  return false;
		}

		return true;
	}

	/**
	 * Check if the expression contains the relational operators.
	 * @param expression - target expression
	 * @param lexemes - Lexemes object
	 * @param table - Symbol table
	 * @return If no error occurred, returns true. Otherwise, returns false.
	 */
	private static boolean checkRelationalOperations(String expression, Lexemes lexemes, SymbolTable table) {
		String[] a1 = expression.split(">=");
		int length1 = a1.length - 1;

		for (int i = 0; i <= length1; i++) {
			String s1 = a1[i];
			String[] a2 = s1.split(">");
			int length2 = a2.length - 1;

			for (int j = 0; j <= length2; j++) {
				String s2 = a2[j];
				String[] a3 = s2.split("<=");
				int length3 = a3.length - 1;

				for (int k = 0; k <= length3; k++) {
					String s3 = a3[k];
					String[] a4 = s3.split("<");
					int length4 = a4.length - 1;
					
					for (int l = 0; l <= length4; l++) {
						String s4 = a4[l].trim();
						if (!s4.isEmpty()) {
							if (table.contains(s4)) {
								int id = table.getIdOfVariable(s4);
								lexemes.insertLexeme("ID", ((Integer) id).toString());
							} else {
								if (s4.contains("\"")) {
									lexemes.insertLexeme("CONST_STR", s4);
								} else if (s4.equals("true") || s4.equals("false")) {
									lexemes.insertLexeme("CONST_BOOL", s4);
								} else {

									try {
										if (expression.contains(".")) {
											Double.parseDouble(s4);
											lexemes.insertLexeme("CONST_NUM", s4);
										} else {
											Integer.parseInt(s4);
											lexemes.insertLexeme("CONST_NUM", s4);
										}
									} catch (Exception e) {
										System.out.println("NameError::Cannot find function or variable called " + expression);
										return false;
									}

								}
							}
						}

						if (l < length4) {
							lexemes.insertLexeme("RELOP", "<");
						} else if (s3.endsWith("<")) {
							lexemes.insertLexeme("RELOP", "<");
						}
					}

					if (k < length3) {
						lexemes.insertLexeme("RELOP", "<=");
					} else if (s2.endsWith("<=")) {
						lexemes.insertLexeme("RELOP", "<=");
					}
				}

				if (j < length2) {
					lexemes.insertLexeme("RELOP", ">");
				} else if (s1.endsWith(">")) {
					lexemes.insertLexeme("RELOP", ">");
				}
			}

			if (i < length1) {
				lexemes.insertLexeme("RELOP", ">=");
			} else if (expression.endsWith(">=")) {
				lexemes.insertLexeme("RELOP", ">=");
			}
		}

		return true;
	}
}
