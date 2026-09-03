import java.util.*;

/**
 * the schema is stored as a map of (index, name:type) pairs
 */
public class Schema implements ISchema {

    private Map<Integer, String> attributes;

    /**
     * constructor
     * @param attributes the attributes of the schema
     */
    public Schema(Map<Integer, String> attributes) {
        this.attributes = new LinkedHashMap<>(attributes);
    }

    /**
     * getter
     * @return returns the map of attributes
     */
    @Override
    public Map<Integer, String> getAttributes() {
        return this.attributes;
    }

    /**
     * Splits the name:type to return the attribute name
     * @param index the index of the schema to retrieve the name from
     * @return the attribute name
     */
    @Override
    public String getName(int index) {
        String part = this.attributes.get(index);
        String[] separatedPart = part.split(":");
        return separatedPart[0];
    }

    /**
     * Splits the name:type to return the attribute type
     * @param index the index of the schema to retrieve the type from
     * @return the attribute type
     */

    @Override
    public String getType(int index) {
        String part = this.attributes.get(index);
        String[] separatedPart = part.split(":");
        return separatedPart[1];
    }

    /**
     * Compare two schemas for equality
     * Two schemas are equal if they contain the same attributes in the same order
     * @param o the reference object with which to compare.
     * @return true if the table names match and the schemas contain the same attributes, otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Schema)) {
            return false;
        } else {
            Schema other = (Schema) o;
            return other.attributes.equals(this.attributes);
        }
    }

    /**
     * Computes the hashcode of the schema using its attributes
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.attributes);
    }
}