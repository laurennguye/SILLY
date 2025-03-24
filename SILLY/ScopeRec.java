import java.util.HashMap;

/**
 * Class that represents a scope record in the program execution.
 * 
 * @author Dave Reed
 * @version 1/20/25
 */
public class ScopeRec {
	private HashMap<Token, DataValue> map;
	private HashMap<Token, DataValue> variables;
	private HashMap<Token, FunctionDecl> functions;
	private ScopeRec parentScope;

	/**
	 * Constructs an empty scope record.
	 * 
	 * @param parent a reference to the parent scope record (null if no parent)
	 */
	public ScopeRec(ScopeRec parent) {
		this.map = new HashMap<Token, DataValue>();
		this.parentScope = parent;
		this.variables = new HashMap<>();
		this.functions = new HashMap<>();
	}

	/**
	 * Determines if a variable is declared in this scope record.
	 * 
	 * @param variable the variable to check
	 * @return true if variable is declared (i.e., a key in map); else, false
	 */
	public boolean declaredInScope(Token variable) {
		return this.map.containsKey(variable);
	}

	/**
	 * Determines the value associated with a variable in this scope record.
	 * 
	 * @param variable the variable to look up
	 * @return the value associated with that variable (null if not found)
	 */
	public DataValue lookupInScope(Token variable) {
		return this.map.get(variable);
	}

	/**
	 * Stores a variable & value in this scope record.
	 * 
	 * @param variable the variable name
	 * @param val      the value to be stored under that name
	 */
	public void storeInScope(Token variable, DataValue val) {
		this.map.put(variable, val);
	}

	/**
	 * Accesses the parent scope record.
	 * 
	 * @return the parent scope record (or null if no parent)
	 */
	public ScopeRec getParentScope() {
		return this.parentScope;
	}

	public void storeFunction(Token name, FunctionDecl func) {
		functions.put(name, func);
	}

	public FunctionDecl lookupFunction(Token name) {
		return functions.get(name);
	}

	public boolean hasFunction(Token name) {
		return functions.containsKey(name);
	}
}
