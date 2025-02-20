import java.util.ArrayList;
import java.util.List;

/**
 * Class that represents an expression in the SILLY language.
 *   @author Dave Reed
 *   @version 1/20/25
 */
public class Expression {
    private Token tok;                        // used for simple expressions (no function)
    private ArrayList<Expression> exprs;     // used to store function inputs

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
        else if (this.tok.getType() == Token.Type.BOOL_FUNC) {
            if (this.tok.toString().equals("not")) {
                if (this.exprs.size() != 1) {
                    throw new Exception("RUNTIME ERROR: 'not' expects one Boolean argument.");
                }
                DataValue val = this.exprs.get(0).evaluate();
                if (val.getType() != DataValue.Type.BOOLEAN) {
                    throw new Exception("RUNTIME ERROR: Boolean expected in 'not' expression.");
                }
                return new BooleanValue(!((Boolean) val.getValue()));
            }
            else if (this.tok.toString().equals("and")) {
                boolean result = true;
                for (Expression expr : this.exprs) {
                    DataValue val = expr.evaluate();
                    if (val.getType() != DataValue.Type.BOOLEAN) {
                        throw new Exception("RUNTIME ERROR: Boolean expected in 'and' expression.");
                    }
                    result = result && (Boolean) val.getValue();
                }
                return new BooleanValue(result);
            }
            else if (this.tok.toString().equals("or")) {
                boolean result = false;
                for (Expression expr : this.exprs) {
                    DataValue val = expr.evaluate();
                    if (val.getType() != DataValue.Type.BOOLEAN) {
                        throw new Exception("RUNTIME ERROR: Boolean expected in 'or' expression.");
                    }
                    result = result || (Boolean) val.getValue();
                }
                return new BooleanValue(result);
            }
        }
        else if (this.tok.getType() == Token.Type.SEQ_FUNC) {
            if (this.tok.toString().equals("len")) {
                if (this.exprs.size() != 1) {
                    throw new Exception("RUNTIME ERROR: 'len' expects one argument.");
                }
                DataValue val = this.exprs.get(0).evaluate();
                if (val.getType() != DataValue.Type.LIST) {
                    throw new Exception("RUNTIME ERROR: List expected in 'len' expression.");
                }
                return new NumberValue(((List<DataValue>) val.getValue()).size());
            }
        }
        throw new Exception("RUNTIME ERROR: Unknown expression format.");
    }

    /**
     * Converts the current expression into a String.
     *   @return the String representation of this expression
     */
    public String toString() {
        if (this.exprs == null) {
            return this.tok.toString();
        }
        else if (this.tok.toString().equals("[")) {
            StringBuilder message = new StringBuilder("[");
            for (Expression e: this.exprs) {
                message.append(e).append(" ");
            }
            return message.toString().trim() + "]";
        }
        else {
            StringBuilder message = new StringBuilder("(" + this.tok);
            for (Expression e : this.exprs) {
                message.append(" ").append(e);
            }
            return message.append(")").toString();
        }
    }
}