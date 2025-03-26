import java.util.ArrayList;

/**
 * Derived class that represents a compound statement in the SILLY language.
 * 
 * @author Dave Reed
 * @version 1/20/25
 */
public class Compound extends Statement {
	private ArrayList<Statement> stmts;
	private boolean useScope;

	/**
	 * Reads in a compound statement from the specified stream
	 * 
	 * @param input the stream to be read from
	 */
	public Compound(TokenStream input, boolean useScope) throws Exception {
        this.useScope = useScope;
        if (!input.next().toString().equals("{")) {
            throw new Exception("SYNTAX ERROR: Malformed compound statement");
        }

        this.stmts = new ArrayList<>();
        while (!input.lookAhead().toString().equals("}")) {
            this.stmts.add(Statement.getStatement(input));
        }
        input.next();
    }

	/**
	 * Executes the current compound statement.
	 */
    public void execute() throws Exception {
        if (this.useScope) {
            Interpreter.MEMORY.beginNestedScope();
        }
        for (Statement stmt : this.stmts) {
            stmt.execute();
        }
        if (this.useScope) {
            Interpreter.MEMORY.endCurrentScope();
        }
    }

    /**
     * Returns the list of statements in the compound block.
     */
    public ArrayList<Statement> getStatements() {
        return this.stmts;
    }
	
	/**
	 * Converts the current compound statement into a String.
	 * 
	 * @return the String representation of this statement
	 */
    public String toString() {
        String str = "{\n";
        for (Statement stmt : this.stmts) {
            str += "  " + stmt + "\n";
        }
        return str + "}";
    }
}
