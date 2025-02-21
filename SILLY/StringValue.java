import java.util.ArrayList;

/**
 * Class that represents a string value in the SILLY language. Extends ListValue
 * to allow string operations to be treated similarly to lists.
 * 
 * @author Lauren Nguyen
 */
public class StringValue extends ListValue {
	private String value;

	/**
	 * Constructs a StringValue object from a given string. Stores individual
	 * characters as CharValue elements in the inherited list.
	 * 
	 * @param str the string value to be stored
	 */
	public StringValue(String str) {
		super(stringToCharValueList(str)); // Pass list to ListValue constructor
		this.value = str;
	}

	/**
	 * Converts a string into a list of CharValues.
	 * 
	 * @param str the string to convert
	 * @return an ArrayList of CharValue elements
	 */
	private static ArrayList<DataValue> stringToCharValueList(String str) {
		ArrayList<DataValue> charList = new ArrayList<>();
		for (char c : str.toCharArray()) {
			charList.add(new CharValue(c));
		}
		return charList;
	}

	/**
	 * Returns the stored string value.
	 * 
	 * @return the string value
	 */
	@Override
	public Object getValue() {
		return this.value;
	}

	/**
	 * Identifies the actual type of the value.
	 * 
	 * @return DataValue.Type.STRING
	 */
	@Override
	public DataValue.Type getType() {
		return DataValue.Type.STRING;
	}

	/**
	 * Converts the string value to a String.
	 * 
	 * @return a string representation of this value
	 */
	@Override
	public String toString() {
		return this.value;
	}
}