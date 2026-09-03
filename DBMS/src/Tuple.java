import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A tuple is an ordered collection of Objects and their associated types (Integer, Double or String)
 * Objects are stored in an array while types are stored in a map of (index, type)
 *
 */
public class Tuple implements ITuple {
    private Object[] values;
    private Map<Integer, Class<?>> typeMap;

    /**
     * The constructor receives a schema and creates the object array and typemap (representing the tuple)
     * The schema has the types of attributes stored as strings ("Integer", "Double", "String")
     * Based upon these types the constructor stores the actual class (Integer.class, Double.class, String.class) to the typemap
     * @param schema the schema to put into the tuple
     */
    public Tuple(ISchema schema) {
        this.values = new Object[schema.getAttributes().size()];
        this.typeMap = new HashMap<>();
        for (int i = 0; i < schema.getAttributes().size(); i++) {
            String type = schema.getType(i);
            Class<?> classType;
            if (type.equals("Integer")) {
                classType = Integer.class;
            } else if (type.equals("Double")) {
                classType = Double.class;
            } else {
                classType = String.class;
            }

            this.typeMap.put(i, classType);
        }
    }

    /**
     * Stores the value at the given index in the (tuple) object
     * The value is converted from the object to its actual class from the typemap
     * @param index the index in the tuple
     * @param value the value to store in the tuple
     */
    @Override
    public void setValue(int index, Object value) {
        Class<?> classType = this.typeMap.get(index);
        if (classType == Integer.class) {
            if (value instanceof String) {
                this.values[index] = Integer.parseInt((String) value);
            } else {
                this.values[index] = value;
            }
        } else if (classType == Double.class) {
            if (value instanceof String) {
                this.values[index] = Double.parseDouble((String) value);
            } else {
                this.values[index] = value;
            }
        } else {
            this.values[index] = value;
        }
    }

    /**
     * Returns the value at a given index from the tuple object
     * @param index the index used to determine which value from the tuple should be returned
     * @return the value at the given index
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(int index) {
        return (T) this.values[index];
    }

    /**
     * Returns the tuple as an array of Objects
     * @return the tuple as an array
     */
    @Override
    public Object[] getValues() {
        Object[] result = new Object[this.values.length];
        for (int i = 0; i < this.values.length; i++) {
            result[i] = this.values[i];
        }
        return result;
    }

    /**
     * Sets the tuple values to the provided ones
     * The values are converted from objects to their actual classes from the typemap
     * @param values the values to set in the tuple
     */
    @Override
    public void setValues(Object[] values) {
        for (int i = 0; i < values.length; i++) {
            setValue(i, values[i]);
        }
    }

    /**
     * Compare two tuples
     * Tuples are equal only if all of their attribute values and corresponding attribute types match
     * @param o the reference object with which to compare.
     * @return true if all attribute values and attribute types match, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple)) {
            return false;
        } else {
            Tuple other = (Tuple) o;
            Object[] otherVals = other.getValues();
            Object[] oVals = this.values;

            if (other.values.length != this.values.length) {
                return false;
            }

            for (int i = 0; i < oVals.length; i++) {
                if (!(oVals[i].equals(otherVals[i])) || !(this.typeMap.get(i).equals(other.typeMap.get(i)))) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Computes the hashcode of the tuple using its attribute values and types
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(this.values), this.typeMap);
    }
}