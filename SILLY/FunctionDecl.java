import java.util.ArrayList;
import java.util.List;

/**
 * Class representing a function declaration in the SILLY language.
 */
public class FunctionDecl extends Statement {
	private Token name;
	private List<Token> params;
	private Compound body;

	/**
	 * Constructs a function declaration.
	 * 
	 * @param name   the function name
	 * @param params the list of parameters
	 * @param body   the compound statement (function body)
	 */
	public FunctionDecl(Token name, List<Token> params, Compound body) {
		this.name = name;
		this.params = params;
		this.body = body;
	}

	/**
	 * Parses a function declaration from the TokenStream.
	 * 
	 * @param input the input token stream
	 * @return the parsed FunctionDecl object
	 */
	public static FunctionDecl parse(TokenStream input) throws Exception {
		input.next(); // Consume "func"

		Token funcName = input.next();
		if (funcName.getType() != Token.Type.IDENTIFIER) {
			throw new Exception("SYNTAX ERROR: Invalid function name");
		}

		if (!input.next().toString().equals("(")) {
			throw new Exception("SYNTAX ERROR: Missing '(' in function declaration");
		}

		List<Token> params = new ArrayList<>();
		while (!input.lookAhead().toString().equals(")")) {
			Token param = input.next();
			if (param.getType() != Token.Type.IDENTIFIER) {
				throw new Exception("SYNTAX ERROR: Invalid parameter name '" + param + "'");
			}
			params.add(param);
		}
		input.next(); 

		Compound body = new Compound(input, true); 

		return new FunctionDecl(funcName, params, body);
	}

	/**
	 * Executes the function declaration by storing it in memory.
	 */
	public void execute() throws Exception {
		if (Interpreter.MEMORY.existsInCurrentScope(this.name)) {
			throw new RuntimeException("RUNTIME ERROR: Name '" + name + "' already exists");
		}
		Interpreter.MEMORY.storeFunction(this.name, this);
	}

	public Token getName() {
		return name;
	}

	public List<Token> getParams() {
		return params;
	}

	public Compound getBody() {
		return body;
	}

	/**
	 * Converts the function declaration to a String.
	 */
	public String toString() {
		return "func " + name + "(" + String.join(" ", params.stream().map(Token::toString).toList()) + ")" + body;
	}
}