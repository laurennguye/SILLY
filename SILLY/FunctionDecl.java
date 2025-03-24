public class FunctionDecl extends Statement {
    private Token name;
    private Compound body;

    public FunctionDecl(Token name, Compound body) {
        this.name = name;
        this.body = body;
    }

    public void execute() throws Exception {
        if (Interpreter.MEMORY.existsInCurrentScope(this.name)) {
            throw new RuntimeException("RUNTIME ERROR: Name '" + this.name + "' already exists");
        }
        Interpreter.MEMORY.storeFunction(this.name, this);
    }

    public Compound getBody() { return body; }

    public String toString() {
        return "func " + name + "() " + body;
    }
}