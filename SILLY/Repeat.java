/**
 * Derived class that represents a repeat statement in the SILLY language.
 */
public class Repeat extends Statement {
    private Expression count;
    private Compound body;

    public Repeat(TokenStream input) throws Exception {
        if (!input.next().toString().equals("repeat")) {
            throw new Exception("SYNTAX ERROR: Malformed repeat statement");
        }
        this.count = new Expression(input);
        this.body = new Compound(input);
    }

    public void execute() throws Exception {
        DataValue countVal = this.count.evaluate();
        if (countVal.getType() != DataValue.Type.NUMBER) {
            throw new Exception("RUNTIME ERROR: Repeat count must be a number.");
        }
        double countValue = (Double) countVal.getValue();
        if (countValue != Math.floor(countValue)) {
            throw new Exception("RUNTIME ERROR: Repeat count must be an integer.");
        }
        int iterations = (int) countValue;
        if (iterations < 0) {
            throw new Exception("RUNTIME ERROR: Repeat count must be non-negative.");
        }
        for (int i = 0; i < iterations; i++) {
            this.body.execute();
        }
    }

    public String toString() {
        return "repeat " + this.count + " " + this.body;
    }
}