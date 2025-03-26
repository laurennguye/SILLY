import java.util.ArrayList;

/**
 * Class that represents an expression in the SILLY language.
 * 
 * @author Dave Reed
 * @version 1/20/25
 */
public class Expression {
	private Token tok; // used for simple expressions (no function)
	private ArrayList<Expression> exprs; // used to store function inputs

	/**
	 * Creates an expression from the specified TokenStream.
	 * 
	 * @param input the TokenStream from which the program is read
	 */
	public Expression(TokenStream input) throws Exception {
		this.tok = input.next();
		if (this.tok.toString().equals("(")) {
			if (input.lookAhead().getType() != Token.Type.IDENTIFIER
					&& input.lookAhead().getType() != Token.Type.MATH_FUNC
					&& input.lookAhead().getType() != Token.Type.BOOL_FUNC
					&& input.lookAhead().getType() != Token.Type.SEQ_FUNC) {
				throw new Exception("SYNTAX ERROR: Identifier or function expected in expression.");
			}
			this.tok = input.next();
			this.exprs = new ArrayList<Expression>();
			while (!input.lookAhead().toString().equals(")")) {
				this.exprs.add(new Expression(input));
			}
			input.next();
		} else if (this.tok.toString().equals("[")) {
			this.exprs = new ArrayList<Expression>();
			while (!input.lookAhead().toString().equals("]")) {
				this.exprs.add(new Expression(input));
			}
			input.next();
		} else if (this.tok.getType() != Token.Type.IDENTIFIER && this.tok.getType() != Token.Type.NUM_LITERAL
				&& this.tok.getType() != Token.Type.BOOL_LITERAL && this.tok.getType() != Token.Type.CHAR_LITERAL
				&& this.tok.getType() != Token.Type.STR_LITERAL) {
			throw new Exception("SYNTAX ERROR: Unknown value (" + this.tok + ").");
		}
	}

	/**
	 * Evaluates the current expression.
	 * 
	 * @return the value represented by the expression
	 */
	@SuppressWarnings("unchecked")
	public DataValue evaluate() throws Exception {
		if (this.exprs == null) {
			if (this.tok.getType() == Token.Type.IDENTIFIER) {
				if (!Interpreter.MEMORY.isDeclared(this.tok)) {
					throw new Exception("RUNTIME ERROR: variable " + this.tok + " is undeclared.");
				}
				return Interpreter.MEMORY.lookupValue(this.tok);
			} else if (this.tok.getType() == Token.Type.NUM_LITERAL) {
				return new NumberValue(Double.parseDouble(this.tok.toString()));
			} else if (this.tok.getType() == Token.Type.BOOL_LITERAL) {
				return new BooleanValue(Boolean.valueOf(this.tok.toString()));
			} else if (this.tok.getType() == Token.Type.CHAR_LITERAL) {
				return new CharValue(this.tok.toString().charAt(1));
			} else if (this.tok.getType() == Token.Type.STR_LITERAL) {
				String str = this.tok.toString();
				if (str.length() == 2) {
					return new StringValue("");
				}
				return new StringValue(str.substring(1, str.length() - 1));
			}
		} else if (this.tok.toString().equals("[")) {
			ArrayList<DataValue> vals = new ArrayList<>();
			for (Expression e : this.exprs) {
				vals.add(e.evaluate());
			}
			return new ListValue(vals);
		} else {
			if (this.tok.getType() == Token.Type.MATH_FUNC) {
				if (this.exprs.size() < 2) {
					throw new Exception("RUNTIME ERROR: Incorrect arity in math expression.");
				}

				DataValue first = this.exprs.get(0).evaluate();
				if (!(first.getValue() instanceof Number)) {
					throw new Exception("RUNTIME ERROR: Non-numeric type in math expression.");
				}

				double result = ((Number) first.getValue()).doubleValue();

				for (int i = 1; i < this.exprs.size(); i++) {
					DataValue val = this.exprs.get(i).evaluate();
					if (!(val.getValue() instanceof Number)) {
						throw new Exception("RUNTIME ERROR: Non-numeric type in math expression.");
					}

					double num = ((Number) val.getValue()).doubleValue();

					switch (this.tok.toString()) {
					case "+":
						result += num;
						break;
					case "-":
						result -= num;
						break;
					case "*":
						result *= num;
						break;
					case "/":
						if (num == 0)
							throw new Exception("RUNTIME ERROR: Division by zero.");
						result /= num;
						break;
					default:
						throw new Exception("RUNTIME ERROR: Unknown mathematical operator.");
					}
				}
				return new NumberValue(result);
			} else if (this.tok.getType() == Token.Type.BOOL_FUNC) {
				if (this.tok.toString().equals("not")) {
					if (this.exprs.size() != 1) {
						throw new Exception("RUNTIME ERROR: 'not' expects exactly one Boolean argument.");
					}
					DataValue val = this.exprs.get(0).evaluate();
					if (!(val instanceof BooleanValue)) {
						throw new Exception("RUNTIME ERROR: 'not' function requires a Boolean argument.");
					}
					return new BooleanValue(!((Boolean) val.getValue()));
				} else if (this.tok.toString().equals("and")) {
					if (this.exprs.size() < 2) {
						throw new Exception("RUNTIME ERROR: 'and' expects at least two Boolean arguments.");
					}
					boolean result = true;
					for (Expression expr : this.exprs) {
						DataValue val = expr.evaluate();
						if (!(val instanceof BooleanValue)) {
							throw new Exception("RUNTIME ERROR: 'and' function requires Boolean arguments.");
						}
						result = result && (Boolean) val.getValue();
						if (!result)
							break; // Short-circuit if false
					}
					return new BooleanValue(result);
				} else if (this.tok.toString().equals("or")) {
					if (this.exprs.size() < 2) {
						throw new Exception("RUNTIME ERROR: 'or' expects at least two Boolean arguments.");
					}
					boolean result = false;
					for (Expression expr : this.exprs) {
						DataValue val = expr.evaluate();
						if (!(val instanceof BooleanValue)) {
							throw new Exception("RUNTIME ERROR: 'or' function requires Boolean arguments.");
						}
						result = result || (Boolean) val.getValue();
						if (result)
							break; // Short-circuit if true
					}
					return new BooleanValue(result);
				}

				DataValue first = this.exprs.get(0).evaluate();

				for (int i = 1; i < this.exprs.size(); i++) {
					DataValue val = this.exprs.get(i).evaluate();

					if (first.getType() != val.getType()) {
						throw new Exception("RUNTIME ERROR: Type mismatch in comparison expression.");
					}

					Comparable<Object> firstValue = (Comparable<Object>) first.getValue();
					Object secondValue = val.getValue();

					switch (this.tok.toString()) {
					case "==":
						if (!firstValue.equals(secondValue))
							return new BooleanValue(false);
						break;
					case "!=":
						if (firstValue.equals(secondValue))
							return new BooleanValue(false);
						break;
					case ">":
						if (firstValue.compareTo(secondValue) <= 0)
							return new BooleanValue(false);
						break;
					case ">=":
						if (firstValue.compareTo(secondValue) < 0)
							return new BooleanValue(false);
						break;
					case "<":
						if (firstValue.compareTo(secondValue) >= 0)
							return new BooleanValue(false);
						break;
					case "<=":
						if (firstValue.compareTo(secondValue) > 0)
							return new BooleanValue(false);
						break;
					default:
						throw new Exception("RUNTIME ERROR: Unknown comparison operator: " + this.tok.toString());
					}

					first = val;
				}
				return new BooleanValue(true);
			}
			if (this.tok.getType() == Token.Type.SEQ_FUNC) {
				if (this.exprs.isEmpty()) {
					throw new Exception("RUNTIME ERROR: Sequence function requires at least one argument.");
				}
				DataValue first = this.exprs.get(0).evaluate();

				switch (this.tok.toString()) {
				case "len":
					if (!(first instanceof ListValue)) {
						throw new Exception("RUNTIME ERROR: 'len' function requires a list or string argument.");
					}
					return new NumberValue(((ListValue) first).size());

				case "get":
					if (!(first instanceof ListValue)) {
						throw new Exception("RUNTIME ERROR: 'get' function requires a list or string argument.");
					}
					if (this.exprs.size() != 2) {
						throw new Exception("RUNTIME ERROR: 'get' function requires exactly two arguments.");
					}
					DataValue indexVal = this.exprs.get(1).evaluate();
					if (!(indexVal.getValue() instanceof Number)) {
						throw new Exception("RUNTIME ERROR: 'get' function index must be a number.");
					}
					int index = ((Number) indexVal.getValue()).intValue();
					if (index < 0 || index >= ((ListValue) first).size()) {
						throw new Exception("RUNTIME ERROR: List index out of bounds.");
					}
					return ((ListValue) first).get(index);

				case "cat":
					boolean allStrings = true;
					StringBuilder strConcat = new StringBuilder();
					ArrayList<DataValue> concatenated = new ArrayList<>();

					for (Expression expr : this.exprs) {
						DataValue val = expr.evaluate();
						if (val == null) {
							throw new Exception("RUNTIME ERROR: 'cat' argument is null");
						}

						if (val instanceof StringValue) {
							strConcat.append(val.toString());
						} else if (val instanceof ListValue) {
							concatenated.addAll(((ListValue) val).getList());
							allStrings = false;
						} else {
							throw new Exception("RUNTIME ERROR: 'cat' requires lists or strings");
						}
					}

					return allStrings ? new StringValue(strConcat.toString()) : new ListValue(concatenated);

				case "str":
					return new StringValue(first.toString());

				default:
					throw new Exception("RUNTIME ERROR: Unknown sequence function '" + this.tok.toString() + "'.");
				}
			} else if (this.tok.getType() == Token.Type.IDENTIFIER) {
				FunctionDecl func = Interpreter.MEMORY.lookupFunction(this.tok);
				Interpreter.MEMORY.beginNestedScope();
				try {
					ArrayList<DataValue> evaluatedArgs = new ArrayList<>();
					for (Expression argExpr : this.exprs) {
						evaluatedArgs.add(argExpr.evaluate());
					}

					for (int i = 0; i < func.getParams().size(); i++) {
						Token param = func.getParams().get(i);
						Interpreter.MEMORY.declareVariable(param);
						Interpreter.MEMORY.storeValue(param, evaluatedArgs.get(i));
					}

					func.getBody().execute();
					return new BooleanValue(true);
				} catch (Return.ReturnSignal ret) {
					return ret.getValue();
				} finally {
					Interpreter.MEMORY.endCurrentScope();
				}
			}
		}
		throw new Exception("RUNTIME ERROR: Unknown expression format.");
	}

	/**
	 * Converts the current expression into a String.
	 * 
	 * @return the String representation of this expression
	 */
	public String toString() {
		if (this.exprs == null) {
			return this.tok.toString();
		} else if (this.tok.toString().equals("[")) {
			StringBuilder message = new StringBuilder("[");
			for (Expression e : this.exprs) {
				message.append(e).append(" ");
			}
			return message.toString().trim() + "]";
		} else {
			StringBuilder message = new StringBuilder("(" + this.tok);
			for (Expression e : this.exprs) {
				message.append(" ").append(e);
			}
			return message.append(")").toString();
		}
	}
}
