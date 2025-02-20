import java.util.ArrayList;

/**
 * Class that represents an expression in the SILLY language.
 *   @author Dave Reed
 *   @version 1/20/25
 */
public class Expression {
    private Token tok;                         // used for simple expressions (no function)
    private ArrayList<Expression> exprs;      // used to store function inputs

    /**
     * Creates an expression from the specified TokenStream.
     *   @param input the TokenStream from which the program is read
     */
    public Expression(TokenStream input) throws Exception {
        this.tok = input.next();
        if (this.tok.toString().equals("(")) {
            if (input.lookAhead().getType() != Token.Type.IDENTIFIER &&    
                input.lookAhead().getType() != Token.Type.MATH_FUNC &&
                input.lookAhead().getType() != Token.Type.BOOL_FUNC &&
                input.lookAhead().getType() != Token.Type.SEQ_FUNC) {
                throw new Exception("SYNTAX ERROR: Identifier or function expected in expression.");
            }
            this.tok = input.next();
            this.exprs = new ArrayList<Expression>();
            while (!input.lookAhead().toString().equals(")")) {
                this.exprs.add(new Expression(input));
            } 
            input.next();
        }
        else if (this.tok.toString().equals("[")) {
            this.exprs = new ArrayList<Expression>();
            while (!input.lookAhead().toString().equals("]")) {
                this.exprs.add(new Expression(input));
            } 
            input.next();
        }
        else if (this.tok.getType() != Token.Type.IDENTIFIER &&
                this.tok.getType() != Token.Type.NUM_LITERAL &&    
                this.tok.getType() != Token.Type.BOOL_LITERAL &&
                this.tok.getType() != Token.Type.CHAR_LITERAL) {
            throw new Exception("SYNTAX ERROR: Unknown value (" + this.tok + ").");
        }
    }

    /**
     * Evaluates the current expression.
     *   @return the value represented by the expression
     */
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
            }
        }
        else if (this.tok.toString().equals("[")) {
            ArrayList<DataValue> vals = new ArrayList<DataValue>();
            for (Expression e : this.exprs) {
                vals.add(e.evaluate());
            }
            return new ListValue(vals);
        }
        else {
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
                        case "*":
                            result *= num;
                            break;
                        case "/":
                            if (num == 0) throw new Exception("RUNTIME ERROR: Division by zero.");
                            result /= num;
                            break;
                    }
                }
                return new NumberValue(result);
            }
            if (this.tok.getType() == Token.Type.BOOL_FUNC) {
                if (this.exprs.size() < 2) {
                    throw new Exception("RUNTIME ERROR: Incorrect arity in comparison expression.");
                }
                DataValue first = this.exprs.get(0).evaluate();
                for (int i = 1; i < this.exprs.size(); i++) {
                    DataValue val = this.exprs.get(i).evaluate();
                    if (first.getType() != val.getType()) {
                        throw new Exception("RUNTIME ERROR: Type mismatch in comparison expression.");
                    }
                    Comparable<Object> firstComparable = (Comparable<Object>) first.getValue();
                    Object secondValue = val.getValue();
                    String operator = this.tok.toString();
                    switch (operator) {
                        case "==":
                            if (!firstComparable.equals(secondValue)) return new BooleanValue(false);
                            break;
                        case "!=":
                            if (firstComparable.equals(secondValue)) return new BooleanValue(false);
                            break;
                        case ">":
                            if (firstComparable.compareTo(secondValue) <= 0) return new BooleanValue(false);
                            break;
                        case ">=":
                            if (firstComparable.compareTo(secondValue) < 0) return new BooleanValue(false);
                            break;
                        case "<":
                            if (firstComparable.compareTo(secondValue) >= 0) return new BooleanValue(false);
                            break;
                        case "<=":
                            if (firstComparable.compareTo(secondValue) > 0) return new BooleanValue(false);
                            break;
                        default:
                            throw new Exception("RUNTIME ERROR: Unknown comparison operator: " + operator);
                    }
                    first = val;
                }
                return new BooleanValue(true);
            }
        }
        throw new Exception("RUNTIME ERROR: Unknown expression format.");
    }
}
