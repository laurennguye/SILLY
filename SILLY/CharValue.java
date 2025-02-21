/**
 * Class representing character values in SILLY.
 * 
 * @author Lauren Nguyen
 */
public class CharValue implements DataValue {
    private char value;

    public CharValue(char value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public DataValue.Type getType() {
        return DataValue.Type.CHAR;
    }

    public String toString() {
        return Character.toString(value);
    }

    public int compareTo(DataValue other) {
        if (other.getType() != DataValue.Type.CHAR) {
            throw new ClassCastException("Cannot compare CharValue with non-char type.");
        }
        return Character.compare(this.value, ((CharValue) other).value);
    }
}
