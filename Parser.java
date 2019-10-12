import java.util.ArrayList;

public class Parser {
	private int currentIndex;
	private SymbolTable table;
	private Lexemes lexemes;
	private ArrayList<SymbolToken> lexemeList;

	Parser(SymbolTable table, Lexemes lexemes) {
		this.table = table;
		this.lexemes = lexemes;
		lexemeList = lexemes.getLexemeList();
		currentIndex = 0;
	}

	public AbstractSyntaxTreeNode parse(int startIndex) {
		ArrayList<AbstractSyntaxTreeNode> children = new ArrayList<AbstractSyntaxTreeNode>();

		// use for loop to iterate the ArrayList of SymbolTokens
		for (int i = startIndex; i < lexemeList.size(); i++) {
			SymbolToken token = lexemeList.get(i);
			String name = token.getName();
			String targetName = name + "_END";

			if (name.equals("While")) {
				//TODO boolean expression of while statement

			} else if (name.equals("Function")) {
				//TODO

			} else if (name.equals("If")) {
				//TODO

			} else {
				AbstractSyntaxTreeNode subTree = this.parse(i, targetName);
				i = currentIndex;

				// to avoid NullPointerException
				if (subTree != null) {
					children.add(subTree);
				}
			}
		}

		SymbolToken token = new SymbolToken();
		token.setName("Program");
		return new AbstractSyntaxTreeNode(token, children);
	}

	private AbstractSyntaxTreeNode parse(int startIndex, String endName) {
		ArrayList<AbstractSyntaxTreeNode> children = new ArrayList<AbstractSyntaxTreeNode>();

		// use for loop to iterate the ArrayList of SymbolTokens
		for (int i = startIndex; i < lexemeList.size(); i++) {
			currentIndex = i;
			SymbolToken token = lexemeList.get(i);
			String name = token.getName();

			if (this.checkIfNewProductionRuleStarted(name)) {
				String targetName = name + "_END";

				if (name.equals("While")) {
					//TODO parse while statement & update currentIndex

				} else if (name.equals("If")) {
					//TODO parse if statement & update currentIndex

				} else if (name.equals("VAR")) {
					i += 1;

					currentIndex = i;
					SymbolToken id_token = lexemeList.get(i);

					// check if next token is ID
					if (!id_token.isNameEqualTo("ID")) {
						return null;
					}

					AbstractSyntaxTreeNode varNode = new AbstractSyntaxTreeNode(token);
					AbstractSyntaxTreeNode idNode = new AbstractSyntaxTreeNode(id_token);

					i += 1;
					currentIndex = i;
					SymbolToken token_next = lexemeList.get(i);

					if (token_next.isNameEqualTo("IS")) {
						i += 1;
						currentIndex = i;

						AbstractSyntaxTreeNode isNode = new AbstractSyntaxTreeNode(token_next);
						AbstractSyntaxTreeNode subTree = this.parse(i, "VAR_END");

						if (subTree != null) {
							varNode.insertChildNode(isNode);
							isNode.insertChildNode(idNode);
							isNode.mergeChildren(subTree);

							currentIndex += 1;

							return varNode;
						} else {
							return null;
						}
					} else if (token_next.isNameEqualTo(targetName)) {
						varNode.insertChildNode(idNode);
						return varNode;
					} else {
						System.out.println("SyntaxError::Invalid token <" + token_next.getTokenString() + ">");
						return null;
					}

				} else if (name.equals("ASSIGN")) {
					i += 1;

					SymbolToken id_token = lexemeList.get(i);

					// check if next token is ID
					if (!id_token.isNameEqualTo("ID")) {
						return null;
					}

					AbstractSyntaxTreeNode assignNode = new AbstractSyntaxTreeNode(token);
					AbstractSyntaxTreeNode idNode = new AbstractSyntaxTreeNode(id_token);

					i += 1;
					SymbolToken token_next = lexemeList.get(i);

					if (token_next.isNameEqualTo("IS")) {
						i += 1;
						AbstractSyntaxTreeNode isNode = new AbstractSyntaxTreeNode(token_next);
						AbstractSyntaxTreeNode subTree = this.parse(i, "terminal_expression");

						i = currentIndex;

						if (subTree != null) {
							assignNode.insertChildNode(isNode);
							isNode.insertChildNode(idNode);
							isNode.mergeChildren(subTree);

							return assignNode;
						} else {
							return null;
						}
					} else {
						System.out.println("SyntaxError::Invalid token <" + token_next.getTokenString() + ">");
					}

				} else if (name.equals("PRINT") || name.equals("PRINTLN")) {
					AbstractSyntaxTreeNode subTree = this.parse(i + 1, targetName);

					i = currentIndex;

					// to avoid NullPointerException
					if (subTree != null) {
						AbstractSyntaxTreeNode printStatement = new AbstractSyntaxTreeNode(token);
						printStatement.mergeChildren(subTree);
						return printStatement;
					} else {
						return null;
					}

				} else if (name.equals("GET")) {
					i += 1;
					currentIndex = i;

					SymbolToken nextToken = lexemeList.get(i);

					if (!nextToken.getName().equals("ID")) {
						System.out.println("SyntaxError::\"get\" could only used with variable!");
						return null;
					}

					AbstractSyntaxTreeNode nextNode = new AbstractSyntaxTreeNode(nextToken);
					AbstractSyntaxTreeNode getStatement = new AbstractSyntaxTreeNode(token);

					getStatement.insertChildNode(nextNode);

					i += 1;
					currentIndex += 1;

					nextToken = lexemeList.get(i);

					// check if the statement ends
					if (nextToken.getName().equals("GET_END")) {
						return getStatement;
					} else {
						return null;
					}
				}

			} else if (name.equals(endName)) {
				currentIndex = i;
				break;
			} else if (name.equals("PRINT") || name.equals("PRINTLN")) {
				AbstractSyntaxTreeNode subTree = this.parse(i + 1, name + "_END");
				i = currentIndex;

				children.add(subTree);

			} else {
				ArrayList<SymbolToken> terminals = new ArrayList<SymbolToken>();

				for (int j = i; j < lexemeList.size(); j++) {
					SymbolToken tempToken = lexemeList.get(j);
					String tempName = tempToken.getName();

					// check if current token is terminal
					if (this.checkIfTerminal(tempName)) {
						//add terminal symbol to the array list
						terminals.add(tempToken);
					} else {
						break;
					}
				}

				// check if the list of terminals is empty
				if (terminals.size() == 0) {
					return null;
				}

				currentIndex += (terminals.size() - 1);
				i = currentIndex;

				AbstractSyntaxTreeNode subTree = new AbstractSyntaxTreeNode(new SymbolToken("TERMINALS"), children);
				this.generateSubTreeWithTerminals(subTree, terminals, 0);

				return subTree;
			}
		}

		return new AbstractSyntaxTreeNode(this.lexemeList.get(startIndex), children);
	}

	/**
	 * Generate the sub tree by parsing the list of terminals.
	 * @param root - root node
	 * @param terminals - list of terminals
	 * @param startIndex - start index
	 * @return The number of parsed terminals.
	 */
	private int generateSubTreeWithTerminals(AbstractSyntaxTreeNode root, ArrayList<SymbolToken> terminals, int startIndex) {
		AbstractSyntaxTreeNode topNode;
		int counter = 1;
		int i = startIndex;

		if (terminals.size() == 0) {
			return 0;
		}

		try {
			SymbolToken token1 = terminals.get(i);

			AbstractSyntaxTreeNode node1 = new AbstractSyntaxTreeNode(token1);

			// check if the current token is LPAREN
			if (token1.isNameEqualTo("LPAREN")) {
				// call this method recursively to parse the parenthesis
				int ret = generateSubTreeWithTerminals(node1, terminals, i + 1);
				i += ret;
				counter += ret;

			} else if (token1.isNameEqualTo("RPAREN")) {
				root.insertChildNode(node1);
				return counter;

			} else if (token1.isValueEqualTo("not")) {
				counter += 1;
				i += 1;

				SymbolToken token2 = terminals.get(i);

				if (this.checkIfOperator(token2.getName())) {
					System.out.println("SyntaxError::Operator \"not\" requries operand!");
					return counter;

				} else if (token2.getName().endsWith("LPAREN")) {
					AbstractSyntaxTreeNode tempNode = new AbstractSyntaxTreeNode(token2);
					node1.insertChildNode(tempNode);

				}
			}

			//check if the expression starts with an operator.
			if (token1.getName().endsWith("OP")) {
				System.out.println("SyntaxError::Expression cannot start with operator!");
				return counter;
			}

			i += 1;

			// check if the current token is the last terminal
			if (i == terminals.size()) {
				root.insertChildNode(node1);
				return counter;
			}

			counter += 1;
			SymbolToken token2 = terminals.get(i);

			AbstractSyntaxTreeNode node2 = new AbstractSyntaxTreeNode(token2);

			// check if the current token is an operand
			if (this.checkIfNotOperator(token2.getName())) {
				if (!token2.isNameEqualTo("RPAREN")) {
					System.out.println("SyntaxError::Expression cannot have multiple operands continuously!");
				} else {
					node2.insertChildNode(node1);
					root.insertChildNode(node2);
				}
				return counter;
			} else if (token2.isValueEqualTo("not")) {
				System.out.println("SyntaxError::Operator not is an unary operator!");
				return counter;

			}

			i += 1;
			counter += 1;

			SymbolToken token3 = terminals.get(i);
			AbstractSyntaxTreeNode node3 = new AbstractSyntaxTreeNode(token3);

			// check if the current token is LPAREN
			if (token3.isNameEqualTo("LPAREN")) {
				// call this method recursively to parse the parenthesis
				int ret = generateSubTreeWithTerminals(node3, terminals, i + 1);
				i += ret;
				counter += ret;

			} else if (token3.isNameEqualTo("RPAREN")) {
				System.out.println("SyntaxError::\"(\" cannot be followed by operator!");
				return counter;
			} else if (token3.isValueEqualTo("not")) {
				i += 1;
				counter += 1;
				SymbolToken token4 = terminals.get(i);

				if (this.checkIfOperator(token4.getName())) {
					System.out.println("SyntaxError::Operator \"not\" requries operand!");
					return counter;
				}

				AbstractSyntaxTreeNode node = new AbstractSyntaxTreeNode(token4);
				node3.insertChildNode(node);

			} else if (this.checkIfOperator(token3.getName())) {
				System.out.println("SyntaxError::Operator \"not\" requries operand!");
				return counter;
			}

			node2.insertChildNode(node1);
			node2.insertChildNode(node3);

			if (i != terminals.size()) {
				topNode = node2;
			} else {
				root.insertChildNode(node2);
				return counter;
			}

			i += 1;

			// use for loop to iterate list of terminals
			for (; i < terminals.size(); i += 1) {
				counter += 1;
				SymbolToken t1 = terminals.get(i);
				AbstractSyntaxTreeNode n1 = new AbstractSyntaxTreeNode(t1);

				// check if the current token is ")"
				if (t1.isNameEqualTo("RPAREN")) {
					root.insertChildNode(n1);
					n1.insertChildNode(topNode);
					return counter;
				}

				// check if current token is operator
				if (this.checkIfOperator(t1.getName())) {
					i += 1;
					counter += 1;
					SymbolToken t2 = terminals.get(i);

					AbstractSyntaxTreeNode n2 = new AbstractSyntaxTreeNode(t2);

					if (t2.isNameEqualTo("RPAREN")) {
						System.out.println("SyntaxError::Operator cannot be followed with operator!");
						return counter;

					} else if (t2.isValueEqualTo("not")) {
						i += 1;
						AbstractSyntaxTreeNode n3 = new AbstractSyntaxTreeNode(terminals.get(i));
						n2.insertChildNode(n3);

					} else if (checkIfOperator(t2.getName())) {
						System.out.println("SyntaxError::Operator cannot be followed with operator!");
						return counter;

					} else if (t2.isNameEqualTo("LPAREN")) {
						n1.insertChildNode(topNode);
						n1.insertChildNode(n2);
						topNode = n1;

						// call this method recursively to parse the parenthesis
						int ret = generateSubTreeWithTerminals(n2, terminals, i + 1);
						i += ret;
						counter += ret;

						continue;
					}

					n1.insertChildNode(topNode);
					n1.insertChildNode(n2);
					topNode = n1;
				}  else {
					System.out.println("SyntaxError::Operand cannot be followed with operand!");
					return counter;
				}
			}

			root.insertChildNode(topNode);
		} catch (IndexOutOfBoundsException e) {
			System.out.println(i);
			System.out.println("SyntaxError::Invalid number of operands");
		}

		return counter;
	}

	/**
	 * Checks if the given terminal is an operand.
	 * @param terminal
	 * @return If true, returns true. Otherwise, returns false.
	 */
	private boolean checkIfNotOperator(String terminal) {
		return !checkIfOperator(terminal);
	}

	/**
	 * Checks if the given terminal is an operator.
	 * @param terminal
	 * @return If true, returns true. Otherwise, returns false.
	 */
	private boolean checkIfOperator(String terminal) {
		if (terminal.equals("AROP") || terminal.equals("RELOP") || terminal.equals("LOGOP") || terminal.equals("IS")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check if the given string is a starting point of a production rule.
	 * @param s - string to check
	 * @return True or false.
	 */
	private boolean checkIfNewProductionRuleStarted(String s) {
		if (s.equals("While") || s.equals("Function") || s.equals("If") || s.equals("PRINT") || s.equals("PRINTLN") || s.equals("GET") || s.equals("ASSIGN") || s.equals("VAR")) {
			return true;
		}

		return false;
	}

	/**
	 * Check if the given name of token is terminal.
	 * @param name - name of the symbol token.
	 * @return If it is terminal, returns true. Otherwise, returns false.
	 */
	private boolean checkIfTerminal(String name) {
		if (name.equals("While") || name.equals("Function") || name.equals("If") || name.equals("then") || name.equals("Else") || name.equals("VAR") || name.equals("ASSIGN")) {
			return false;
		} else if (name.endsWith("_END") || name.equals("PRINT") || name.equals("PRINTLN") || name.equals("GET") || name.equals("WHILE_STMT_CONDITIONAL_END")) {
			return false;
		}

		return true;
	}
}
