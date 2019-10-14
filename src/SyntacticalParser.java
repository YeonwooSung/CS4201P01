import java.util.ArrayList;

public class SyntacticalParser {
	private int currentIndex;
	private String programName;
	private ArrayList<SymbolToken> lexemeList;

	SyntacticalParser(ArrayList<SymbolToken> lexemeList, String programName) {
		this.programName = programName;
		this.lexemeList = lexemeList;
		currentIndex = 0;
	}

	/**
	 * Parse the lexemes to generate the AST.
	 * @param startIndex - start index
	 * @return The generated syntax tree
	 */
	public AbstractSyntaxTreeNode parse(int startIndex) {
		ArrayList<AbstractSyntaxTreeNode> children = new ArrayList<AbstractSyntaxTreeNode>();

		// use for loop to iterate the ArrayList of SymbolTokens
		for (int i = startIndex; i < lexemeList.size(); i++) {
			SymbolToken token = lexemeList.get(i);
			String name = token.getName();
			String targetName = name + "_END";

			/* use if-else statement to compare the token's name to generate a suitable sub tree*/

			if (name.equals("While")) {
				ArrayList<SymbolToken> terminals = new ArrayList<SymbolToken>();
				boolean checker = true;

				// use for loop to iterate list of tokens
				for (i = i + 1; i < lexemeList.size(); i++) {
					SymbolToken t = lexemeList.get(i);

					if (this.checkIfTerminal(t.getName())) {
						terminals.add(t);
					} else {
						if (t.isNameEqualTo("WHILE_STMT_CONDITIONAL_END")) {
							checker = true;
						} else {
							checker = false;
						}

						break;
					}
				}

				currentIndex = i;

				// check if error occurred while iterating the list of tokens
				if (checker) {
					AbstractSyntaxTreeNode whileTree = new AbstractSyntaxTreeNode(token);
					this.generateSubTreeWithTerminals(whileTree, terminals, 0);

					i += 1;
					currentIndex = i;

					// use for loop to iterate list of lexeme tokens
					for (; i < lexemeList.size(); i++) {
						SymbolToken t = lexemeList.get(i);

						if (t.isNameEqualTo("While_END")) {
							break;
						}

						AbstractSyntaxTreeNode subTree = this.parse(i, t.getName() + "_END");
						whileTree.insertChildNode(subTree);

						i = currentIndex;

						if (!lexemeList.get(i).getName().endsWith("_END")) {
							i += 1;
						}
					}

					currentIndex = i;

					children.add(whileTree);
				} else {
					continue;
				}

			} else if (name.equals("Function")) {
				i += 1;
				AbstractSyntaxTreeNode functionNode = new AbstractSyntaxTreeNode(token);

				// use for loop to iterate list of lexeme tokens
				for (; i < lexemeList.size(); i++) {
					SymbolToken t = lexemeList.get(i);

					if (t.isNameEqualTo(targetName)) {
						break;
					} else if (t.isNameEqualTo("RETURN")) {
						AbstractSyntaxTreeNode returnNode = new AbstractSyntaxTreeNode(t);
						i += 1;

						AbstractSyntaxTreeNode subTree = this.parse(i, targetName);
						returnNode.insertChildNode(subTree);

						functionNode.insertChildNode(returnNode);

						i = currentIndex;

						break;
					} else if (this.checkIfNewProductionRuleStarted(t.getName())) {
						AbstractSyntaxTreeNode subTree = this.parse(i, targetName);
						functionNode.insertChildNode(subTree);

						i = currentIndex;
					}
				}

				children.add(functionNode);
			} else if (name.equals("If")) {
				AbstractSyntaxTreeNode ifNode = parseIfStatement(i, token);
				children.add(ifNode);

				i = currentIndex;

			} else {
				AbstractSyntaxTreeNode subTree = this.parse(i, targetName);
				i = currentIndex;

				// to avoid NullPointerException
				if (subTree != null && !subTree.getTokenName().equals("TERMINALS")) {
					children.add(subTree);
				}
			}
		}

		return new AbstractSyntaxTreeNode(new SymbolToken("Program", programName), children);
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

				/* use if-else statement to compare the token's name to generate suitable sub tree*/

				if (name.equals("While")) {
					ArrayList<SymbolToken> terminals = new ArrayList<SymbolToken>();
					boolean checker = true;

					// use for loop to iterate list of tokens
					for (i = i + 1; i < lexemeList.size(); i++) {
						SymbolToken t = lexemeList.get(i);

						if (this.checkIfTerminal(t.getName())) {
							terminals.add(t);
						} else {
							if (t.isNameEqualTo("WHILE_STMT_CONDITIONAL_END")) {
								checker = true;
							} else {
								checker = false;
							}

							break;
						}
					}

					currentIndex = i;

					// check if error occurred while iterating the list of tokens
					if (checker) {
						AbstractSyntaxTreeNode whileTree = new AbstractSyntaxTreeNode(token);
						this.generateSubTreeWithTerminals(whileTree, terminals, 0);

						i += 1;
						currentIndex = i;

						// use for loop to parse all statements in the while statement
						for (; i < lexemeList.size(); i++) {
							SymbolToken t = lexemeList.get(i);

							// check if the while statement ends
							if (t.isNameEqualTo("While_END")) {
								break;
							}

							AbstractSyntaxTreeNode subTree = this.parse(i, t.getName() + "_END");
							whileTree.insertChildNode(subTree);

							i = currentIndex;

							if (!lexemeList.get(i).getName().endsWith("_END")) {
								i += 1;
							}
						}

						currentIndex = i;

						return whileTree;
					} else {
						return null;
					}
				} else if (name.equals("If")) {
					return parseIfStatement(i, token);

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

					// check if the next token is "=".
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

			} else if (name.equals("Function")) {
				String targetName = name + "_END";

				i += 1;
				AbstractSyntaxTreeNode functionNode = new AbstractSyntaxTreeNode(token);

				// use for loop to iterate list of lexeme tokens
				for (; i < lexemeList.size(); i++) {
					SymbolToken t = lexemeList.get(i);

					if (t.isNameEqualTo(targetName)) {
						break;
					} else if (t.isNameEqualTo("RETURN")) {
						AbstractSyntaxTreeNode returnNode = new AbstractSyntaxTreeNode(t);
						i += 1;

						AbstractSyntaxTreeNode subTree = this.parse(i, targetName);
						returnNode.insertChildNode(subTree);

						functionNode.insertChildNode(returnNode);

						i = currentIndex;

						break;
					} else if (this.checkIfNewProductionRuleStarted(t.getName())) {
						AbstractSyntaxTreeNode subTree = this.parse(i, targetName);
						functionNode.insertChildNode(subTree);

						i = currentIndex;
					}
				}
				
				currentIndex = i;

				return functionNode;

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
	 * Parse the if-statement, and generate the sub tree for the if-statement.
	 * @param i - start index
	 * @param token - token for if statement.
	 * @return If error occurs, returns null. Otherwise, returns the sub tree.
	 */
	private AbstractSyntaxTreeNode parseIfStatement(int i, SymbolToken token) {
		ArrayList<SymbolToken> terminals = new ArrayList<SymbolToken>();
		boolean checker = true;
		boolean boolExpressionEnd = false;
		boolean hasElse = false;
		int startIndex = i + 1;
		int endIndex = startIndex;

		// use for loop to iterate list of tokens
		for (i = startIndex; i < lexemeList.size(); i++) {
			SymbolToken t = lexemeList.get(i);

			if (boolExpressionEnd) {
				if (t.isNameEqualTo("If_END")) {
					break;
				} else if (t.isNameEqualTo("Else")) {
					hasElse = true;
				}

			} else {
				if (this.checkIfTerminal(t.getName())) {
					terminals.add(t);
				} else {
					if (t.isNameEqualTo("then")) {
						checker = true;
					} else {
						checker = false;
					}

					boolExpressionEnd = true;
					endIndex = i;
				}
			}
		}

		currentIndex = endIndex;

		// check if error occurred while iterating the list of tokens
		if (checker) {
			AbstractSyntaxTreeNode ifNode = new AbstractSyntaxTreeNode(token);
			this.generateSubTreeWithTerminals(ifNode, terminals, 0);

			// check if this if statement has "else" statement
			if (hasElse) {
				AbstractSyntaxTreeNode thenNode = new AbstractSyntaxTreeNode(lexemeList.get(endIndex));

				for (i = endIndex + 1; i < lexemeList.size(); i++) {
					SymbolToken t = lexemeList.get(i);

					if (t.isNameEqualTo("Else")) {
						break;
					}

					AbstractSyntaxTreeNode subTree = this.parse(i, t.getName() + "_END");
					thenNode.insertChildNode(subTree);

					i = currentIndex + 1;
				}

				ifNode.insertChildNode(thenNode);

				/* parse the else-statement */

				AbstractSyntaxTreeNode elseNode = new AbstractSyntaxTreeNode(lexemeList.get(i));
				
				for (i = i + 1; i < lexemeList.size(); i++) {
					SymbolToken t = lexemeList.get(i);

					if (t.isNameEqualTo("If_END")) {
						break;
					}

					AbstractSyntaxTreeNode subTree = this.parse(i, t.getName() + "_END");
					elseNode.insertChildNode(subTree);

					i = currentIndex + 1;
				}

				ifNode.insertChildNode(elseNode);

				currentIndex = i;
			} else {
				/* parse the then-statement of the if-statement */

				AbstractSyntaxTreeNode thenNode = new AbstractSyntaxTreeNode(lexemeList.get(endIndex));

				for (i = endIndex + 1; i < lexemeList.size(); i++) {
					SymbolToken t = lexemeList.get(i);

					if (t.isNameEqualTo("If_END")) {
						break;
					}

					AbstractSyntaxTreeNode subTree = this.parse(i, t.getName() + "_END");
					thenNode.insertChildNode(subTree);

					i = currentIndex + 1;
				}

				ifNode.insertChildNode(thenNode);
				currentIndex = i;
			}

			return ifNode;
		} else {
			return null;
		}
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

		// check if the list is an empty list
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

			} else if (token1.isNameEqualTo("RPAREN")) { // check if the current token is right parenthesis
				root.insertChildNode(node1);
				return counter;

			} else if (token1.isValueEqualTo("not")) { // check if the current token is a "not" operator
				counter += 1;
				i += 1;

				SymbolToken token2 = terminals.get(i);

				// check if the current token is an operator
				if (this.checkIfOperator(token2.getName())) {

					// check if the next token is "not" operator
					if (token2.getValue().equals("not")) {
						AbstractSyntaxTreeNode node2 = new AbstractSyntaxTreeNode(token2);
						node1.insertChildNode(node2);

						// use for loop to iterate list of terminals until it founds the terminals that is not "not" operator
						for (i = i + 1; i < terminals.size(); i++) {
							counter += 1;
							SymbolToken token = terminals.get(i);

							// check if the current token is a not operator
							if (token.getValue() != null && token.getValue().equals("not")) {
								AbstractSyntaxTreeNode notOperatorNode = new AbstractSyntaxTreeNode(token);
								node2.insertChildNode(notOperatorNode);
								node2 = notOperatorNode;

							} else {

								// check if the current token is an operand
								if (this.checkIfNotOperator(token.getName())) {
									AbstractSyntaxTreeNode operandNode = new AbstractSyntaxTreeNode(token);
									node2.insertChildNode(operandNode);
									break;
								} else {
									System.out.println("SyntaxError::Operator \"not\" requries operand!");
									return counter;
								}
							}
						}

					} else {
						System.out.println("SyntaxError::Operator \"not\" requries operand!");
						return counter;
					}

					//check if the current token is '('
				} else if (token2.getName().endsWith("LPAREN")) {
					AbstractSyntaxTreeNode tempNode = new AbstractSyntaxTreeNode(token2);
					node1.insertChildNode(tempNode);

					// check if the current token is an operand
				} else if (!this.checkIfOperator(token2.getName())) {
					AbstractSyntaxTreeNode node2 = new AbstractSyntaxTreeNode(token2);
					node1.insertChildNode(node2);

				}

			} else if (token1.getName().endsWith("OP")) { //check if the expression starts with an operator.
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

				// check if the current token is "not" operator
			} else if (token2.isValueEqualTo("not")) {
				System.out.println("SyntaxError::Operator \"not\" is an unary operator!");
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

				// check if current token is ')'
			} else if (token3.isNameEqualTo("RPAREN")) {
				System.out.println("SyntaxError::\"(\" cannot be followed by operator!");
				return counter;

				// check if the current token is the "not" operator
			} else if (token3.isValueEqualTo("not")) {
				i += 1;
				counter += 1;
				SymbolToken token4 = terminals.get(i);

				// check if the current token is an operator
				if (this.checkIfOperator(token4.getName())) {

					// check if the current token is a not operator
					if (token4.getValue().equals("not")) {
						AbstractSyntaxTreeNode node = new AbstractSyntaxTreeNode(token4);
						node3.insertChildNode(node);

						for (i = i + 1; i < terminals.size(); i++) {
							SymbolToken token = terminals.get(i);
							counter += 1;

							// check if the current token is an operator
							if (this.checkIfOperator(token.getName())) {
								// check if the current token is a not operator
								if (token.getValue().equals("not")) {
									AbstractSyntaxTreeNode tempNode = new AbstractSyntaxTreeNode(token);
									node.insertChildNode(tempNode);
									node = tempNode;

								} else {
									System.out.println("SyntaxError::Operator \"not\" requries operand!");
									return counter;
								}

							} else { // current token is an operand
								AbstractSyntaxTreeNode operandNode = new AbstractSyntaxTreeNode(token);
								node.insertChildNode(operandNode);
								break;
							}
						}

					} else {
						System.out.println("SyntaxError::Operator \"not\" requries operand!");
						return counter;
					}
				} else {
					AbstractSyntaxTreeNode node = new AbstractSyntaxTreeNode(token4);
					node3.insertChildNode(node);
				}

			} else if (this.checkIfOperator(token3.getName())) {
				System.out.println("SyntaxError::Operator \"not\" requries operand!");
				return counter;
			}

			node2.insertChildNode(node1);
			node2.insertChildNode(node3);

			// check if the current token is the last terminal in the list
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

				// Check if current token is operator, because the operand cannot be followed by other operand
				if (this.checkIfOperator(t1.getName())) {
					i += 1;
					counter += 1;
					SymbolToken t2 = terminals.get(i);

					AbstractSyntaxTreeNode n2 = new AbstractSyntaxTreeNode(t2);

					if (t2.isNameEqualTo("RPAREN")) {
						System.out.println("SyntaxError::Operator cannot be followed by operator!");
						return counter;

					} else if (t2.isValueEqualTo("not")) {
						i += 1;
						SymbolToken token = terminals.get(i);

						if (this.checkIfNotOperator(token.getName())) {
							AbstractSyntaxTreeNode n3 = new AbstractSyntaxTreeNode(token);
							n2.insertChildNode(n3);

						} else if (token.getValue() != null && token.getValue().equals("not")) {
							AbstractSyntaxTreeNode n3 = new AbstractSyntaxTreeNode(token);

							for (i = i + 1; i < terminals.size(); i++) {
								token = terminals.get(i);
								counter += 1;

								// check if next token is an operand
								if (this.checkIfNotOperator(token.getName())) {
									AbstractSyntaxTreeNode nextNode = new AbstractSyntaxTreeNode(token);
									n3.insertChildNode(nextNode);

									break;
								} else {
									if (token.getValue() != null && token.getValue().equals("not")) {
										AbstractSyntaxTreeNode nextNode = new AbstractSyntaxTreeNode(token);
										n3.insertChildNode(nextNode);
										n3 = nextNode;

									} else {
										System.out.println("SyntaxError::Operator \"not\" requries operand!");
										return counter;
									}
								}
							}

						} else{
							System.out.println("SyntaxError::Operator \"not\" requries operand!");
							return counter;
						}

					} else if (checkIfOperator(t2.getName())) {
						System.out.println("SyntaxError::Operator cannot be followed by operator!");
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
					System.out.println("SyntaxError::Operand cannot be followed by operand!");
					return counter;
				}
			}

			root.insertChildNode(topNode);
		} catch (IndexOutOfBoundsException e) {
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
