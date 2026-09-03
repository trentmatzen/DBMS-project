import java.util.*;

/**
 * A table has a name, a schema and a list of tuples
 */
public class Table implements ITable {
    private String name;
    private List<ITuple> tuples;
    private ISchema schema;

    /**
     * constructor
     * @param name the name of the table
     * @param schema the schema of the table
     */
    public Table(String name, ISchema schema) {
        this.name = name;
        this.schema = schema;
        this.tuples = new ArrayList<>();
    }

    /**
     * Returns the table name
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Adds a tuple to the table
     * @param tuple the tuple to add
     */
    @Override
    public void addTuple(ITuple tuple) {
        this.tuples.add(tuple);
    }

    /**
     * Returns the list of tuples
     * @return all the tuples
     */
    @Override
    public List<ITuple> getTuples() {
        return this.tuples;
    }

    /**
     * Returns the table schema
     * @return the schema
     */
    @Override
    public ISchema getSchema() {
        return this.schema;
    }

    /**
     * Returns the table name and tuples as string for debugging purposes (if needed)
     * @return stringified representation of the table's name and tuples
     */
    @Override
    public String toString() {
        return "Table name: " + this.name + ", Tuples: " + this.tuples.toString();
    }

    /**
     * Compares two tables for equality
     * Two tables are equal if (a) their schema match and (b) they contain the same tuples
     * Tables are considered as sets in database theory, so the comparison is set based and not array based, i.e. order doesn't matter
     * @param obj the reference object with which to compare.
     * @return true if the tables have the same schema and tuples, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Table)) {
            return false;
        } else {
            Table other = (Table) obj;
            Set<ITuple> thisTuples = new HashSet<>();
            Set<ITuple> otherTuples = new HashSet<>();

            for (ITuple tuple : this.tuples) {
                thisTuples.add(tuple);
            }
            for (ITuple tuple : other.tuples) {
                otherTuples.add(tuple);
            }

            return this.schema.equals(other.schema) && thisTuples.equals(otherTuples);
        }
    }

    /**
     * Computes the hashcode of the table using its tuples
     * @return the hashcode using the table's tuples
     */
    @Override
    public int hashCode() {
        Set<ITuple> thisTuples = new HashSet<>();
        for (ITuple tuple : this.tuples) {
            thisTuples.add(tuple);
        }
        return Objects.hash(thisTuples);
    }
}
