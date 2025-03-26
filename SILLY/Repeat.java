/**
 * Derived class that represents a repeat statement in the SILLY language.
 * 
 * @author lauren nguyen
 * @version 3/26/2025
 */
public class Repeat extends Statement {
    private Expression count;
    private Compound body;

    /**
     * Reads in a repeat statement from the specified TokenStream.
     * 
     * @param input the stream to be read from
     */
    public Repeat(TokenStream input) throws Exception {
        if (!input.next().toString().equals("repeat")) {
            throw new Exception("SYNTAX ERROR: Malformed repeat statement");
        }
        this.count = new Expression(input);
        this.body = new Compound(input, false);
    }

    /**
     * Executes the current repeat statement.
     */
    public void execute() throws Exception {
        DataValue countVal = this.count.evaluate();
        if (countVal.getType() != DataValue.Type.NUMBER) {
            throw new Exception("RUNTIME ERROR: Repeat count must be a number.");
        }
        double countValue = (Double) countVal.getValue();
        if (countValue != Math.floor(countValue) || countValue < 0) {
            throw new Exception("RUNTIME ERROR: Repeat count must be a non-negative integer.");
        }
        int iterations = (int) countValue;

        for (int i = 0; i < iterations; i++) {
            for (Statement stmt : this.body.getStatements()) {
                stmt.execute();
            }
        }
    }

    public String toString() {
        return "repeat " + this.count + " " + this.body;
    }
}