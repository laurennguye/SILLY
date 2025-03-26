import java.util.Stack;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Class that defines the memory space for the SILLY interpreter.
 * 
 * @author Dave Reed
 * @version 1/20/25
 */
public class MemorySpace {
	private Stack<ScopeRec> runtimeStack;

	/**
	 * Constructs a memory space with a single (global) scope record.
	 */
	public MemorySpace() {
		this.runtimeStack = new Stack<ScopeRec>();
		this.runtimeStack.push(new ScopeRec(null));
	}

	/**
	 * Adds a new scope to the top of the runtime stack (linked to previous top).
	 */
	public void beginNestedScope() {
		this.runtimeStack.push(new ScopeRec(this.runtimeStack.peek()));
	}

	/**
	 * Removes the current scope from the top of the runtime stack.
	 */
	public void endCurrentScope() {
		this.runtimeStack.pop();
	}

	/**
	 * Declares a variable (without storing an actual value).
	 * 
	 * @param variable the variable to be declared
	 */
	public void declareVariable(Token variable) {
		this.runtimeStack.peek().storeInScope(variable, null);
	}

	/**
	 * Determines if a variable is already declared.
	 * 
	 * @param variable the variable to be found
	 * @return true if it is declared and/or assigned; else, false
	 */
	public boolean isDeclared(Token variable) {
		return (this.findScopeinStack(variable) != null);
	}

	/**
	 * Stores a variable/value in the runtime stack.
	 * 
	 * @param variable the variable name
	 * @param val      the value to be stored under that name
	 */
	public void storeValue(Token variable, DataValue val) {
	    if (this.isInNestedScope() && !this.runtimeStack.peek().declaredInScope(variable)) {
	        this.declareVariable(variable);
	    }
	    
	    ScopeRec scope = this.findScopeinStack(variable);
	    if (scope != null) {
	        scope.storeInScope(variable, val);
	    } else {
	        throw new RuntimeException("Variable '" + variable + "' not declared");
	    }
	}

	/**
	 * Determines the value associated with a variable in memory.
	 * 
	 * @param variable the variable to look up
	 * @return the value associated with that variable
	 */
	public DataValue lookupValue(Token variable) {
		return this.findScopeinStack(variable).lookupInScope(variable);
	}

	/////////////////////////////////////////////////////////////////////////////

	/**
	 * Locates the Scope in the stackSegment that contains the specified variable.
	 * 
	 * @param variable the variable being searched for
	 * @return the Scope containing that variable
	 */
	private ScopeRec findScopeinStack(Token variable) {
		ScopeRec stepper = this.runtimeStack.peek();
		while (stepper != null && !stepper.declaredInScope(variable)) {
			stepper = stepper.getParentScope();
		}
		return stepper;
	}

	/**
	 * Check if a name exists in CURRENT scope (variable or function)
	 */
	public boolean existsInCurrentScope(Token name) {
		ScopeRec current = runtimeStack.peek();
		return current.declaredInScope(name) || current.hasFunction(name);
	}

	/**
	 * Store a function in the CURRENT scope
	 */
	public void storeFunction(Token name, FunctionDecl func) {
		runtimeStack.peek().storeFunction(name, func);
	}

	/**
	 * Retrieve a function from nearest scope
	 */
	public FunctionDecl lookupFunction(Token name) {
		ScopeRec current = runtimeStack.peek();
		while (current != null) {
			if (current.hasFunction(name)) {
				return current.lookupFunction(name);
			}
			current = current.getParentScope();
		}
		return null;
	}
	
    public boolean isInNestedScope() {
        return this.runtimeStack.size() > 1;
    }
}
