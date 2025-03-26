/**
 * Derived class that represents a return statement in the SILLY language.
 *   @version 3/26/25
 */
public class Return extends Statement {
    private Expression expr;

    /**
     * Custom exception to propagate return values.
     */
    public static class ReturnSignal extends RuntimeException {
        private DataValue value;

        public ReturnSignal(DataValue val) {
            this.value = val;
        }

        public DataValue getValue() {
            return value;
        }
    }

    /**
     * Reads in a return statement from the specified TokenStream.
     *   @param input the stream to be read from
     */
    public Return(TokenStream input) throws Exception {
        if (!input.next().toString().equals("return")) {
            throw new Exception("SYNTAX ERROR: Malformed return statement");
        }
        this.expr = new Expression(input);
    }

    /**
     * Executes the current return statement.
     */
    public void execute() throws Exception {
        DataValue retVal = this.expr.evaluate();
        throw new ReturnSignal(retVal); // Throw to exit function
    }

    /**
     * Converts the current return statement into a String.
     *   @return the String representation of this statement
     */
    public String toString() {
        return "return " + this.expr;
    }
}