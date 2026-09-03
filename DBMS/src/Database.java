import java.util.*;

/**
 * The main database class
 * Database as a list of tables, list of schemas and a folder name where the database is stored
 * Database is stored (on the disk) in the form of three csv files and schema text file
 */
class Database {
    private List<ITable> tables;
    private List<ISchema> schemas;
    private String folderName;

    /**
     * Constructor
     * Creates the empty tables and schema lists
     * Reads the schema file to add schemas to the database
     * Populates the database table (with the data read from the csv files)
     * @param folderName the name of the folder
     * @param schemaFileName the name of the file containing the schema
     */
    public Database(String folderName, String schemaFileName) {
        this.tables = new ArrayList<>();
        this.schemas = new ArrayList<>();
        this.folderName = folderName;
        IO.readSchema(schemaFileName + ".txt", folderName, this);
        populateDB();
    }

    /**
     * Adds a table to the database
     * @param table table to add to the database
     */
    public void addTable(ITable table) {
        this.tables.add(table);
    }

    /**
     * Adds a table schema to the database
     * @param schema the schema to add
     */
    public void addSchema(ISchema schema) {
        this.schemas.add(schema);
    }

    /**
     * The list of tables in the database is initialized with empty tables in the constructor
     * An empty table has a name and an empty list of tuples
     * This method sets the empty table in the list to the one provided as a parameter
     * @param table the updated table to set
     */
    public void updateTable(ITable table) {
        for (int i = 0; i < this.tables.size(); i++) {
            if(this.tables.get(i).getName().equals(table.getName())) {
                this.tables.set(i, table);
                break;
            }
        }
    }

    /**
     * Populates the database
     *
     * Implements the following algorithm
     *
     * For each table in the db (tables are initially empty)
     *   Get the table's data from the csv file (by calling the read table method)
     *   Update the table (by calling the update table method)
     */
    public void populateDB() {
        for (ITable table : this.tables) {
            String tableName = table.getName();
            ISchema tableSchema = table.getSchema();
            ITable updatedTable = IO.readTable(tableName, tableSchema, this.folderName);
            updateTable(updatedTable);
        }
    }

    /**
     * Insert data into a table based upon the insert query
     * If the query is invalid throws an InvalidQueryException
     *
     * A query is valid if
     *
     * 1.	It has an insert clause (insert into keywords) followed by a table name
     * 2.	All the attribute names in the insert clause are in the schema
     * 3.	The table name in the insert clause is in the schema
     *
     * Implements the following algorithm
     *
     * Parse the insert into clause to get the table name, attribute name(s) and value(s)
     * If the query in not valid
     *   Throw an invalid query exception
     *   Exit
     * Create a new tuple with the schema of the table
     * Set the tuple values to the values from the query
     * Open the file corresponding to the table name
     * Append the tuple values (as comma separated values) to the end of the file
     *
     * @param query the insert command given
     * @throws InvalidQueryException thrown if the insert clause is invalid
     */
    public void insertData(String query) throws InvalidQueryException {

        // example insert clause: INSERT INTO Student (sid, sname, major, byear) VALUES (‘s5’, ‘Mary’, ‘CS’, 2004)

        String lower = query.toLowerCase();
        if (!(lower.startsWith("insert into"))) {
            throw new InvalidQueryException();
        }

        // get table name
        int start = lower.indexOf("insert into") + "insert into".length();
        while (lower.charAt(start) == ' ') {
            start++;
        }
        int end = lower.indexOf("(");
        while (lower.charAt(end - 1) == ' ') {
            end--;
        }
        String tableName = lower.substring(start, end);

        // get attributes
        int startAtts = lower.indexOf("(");
        int endAtts = lower.indexOf(")");
        String insideAtts = lower.substring(startAtts + 1, endAtts);
        String insideCleaned = "";
        for (int i = 0; i < insideAtts.length(); i++) {
            if (insideAtts.charAt(i) != ' ') {
                insideCleaned += insideAtts.charAt(i);
            }
        }
        String[] attributes = insideCleaned.split(",");

        // get values
        int valuesIndex = lower.indexOf("values");
        int startVals = lower.indexOf("(", valuesIndex);
        int endVals = lower.indexOf(")", valuesIndex);
        String insideVals = query.substring(startVals + 1, endVals);
        String insideValsCleaned = "";
        for (int i = 0; i < insideVals.length(); i++) {
            String ch = insideVals.substring(i, i + 1);
            if (!(ch.equals(" ")) && !(ch.equals("'"))) {
                insideValsCleaned += ch;
            }
        }
        String[] values = insideValsCleaned.split(",");

        // find schema
        ISchema schema = null;
        ITable targetTable = null;

        for (ITable tab : this.tables) {
            if (tab.getName().equalsIgnoreCase(tableName)) {
                targetTable = tab;
                schema = tab.getSchema();
                break;
            }
        }
        if (schema == null) {
            throw new InvalidQueryException();
        }

        // check attributes
        Map<String, Integer> attributeIndexMap = new HashMap<>();
        for (int i = 0; i < schema.getAttributes().size(); i++) {
            String name = schema.getName(i);
            attributeIndexMap.put(name.toLowerCase(), i);
        }

        for (String att : attributes) {
            if (!attributeIndexMap.containsKey(att.toLowerCase())) {
                throw new InvalidQueryException();
            }
        }

        // create and add tuple to table
        Tuple tuple = new Tuple(schema);
        for (int i = 0; i < attributes.length; i++) {
            String att = attributes[i].toLowerCase();
            int index = attributeIndexMap.get(att);
            tuple.setValue(index, values[i]);
        }

        for (ITable table : this.tables) {
            if (table.getName().equalsIgnoreCase(tableName)) {
                table.addTuple(tuple);
                break;
            }
        }

        IO.writeTuple(tableName, values, this.folderName);
    }

    /**
     * Selects data from a table (and returns it in the form of a results table)
     * If the query in not valid, throws an InvalidQueryException
     *
     * A query is valid if
     *
     * 1.	It has a select clause (select keyword followed by at least one attribute name)
     * 2.	It has a from clause (from keyword followed by a table name)
     * 3.	All the attribute names in the select clause are in the schema
     * 4.	The table name in the from clause is in the schema
     * 5.	All the attribute names in the where clause (if present) are in the schema
     * 6.	The attribute name in the order by clause (if present) is in the schema
     *
     * Implements the following algorithm
     *
     * Parse the query to get the select, from, where and order by clauses and the attribute and table names and condition
     * If the query is not valid
     *   Throw an invalid query exception
     *   Exit
     * Create a new results schema based with the attributes from the select clause
     * Create a new result table
     * For each tuple in the table
     *   If the tuple matches the where clause condition(s)
     *     Create a new results tuple using the result schema
     *     Set the results tuple values to the current tuple corresponding values
     *     Add the results tuple to the result table
     * Return results table
     *
     *
     * @param query the delete command given
     * @return a sub-table with the requested values
     * @throws InvalidQueryException thrown if the select clause is invalid
     */
    public ITable selectData(String query) throws InvalidQueryException {

        // example select clause: SELECT sid, cno FROM enroll WHERE grade = ‘B’

        String lower = query.toLowerCase();
        if (!(lower.startsWith("select")) || !(lower.contains("from"))) {
            throw new InvalidQueryException();
        }

        // get attributes
        int startAtts = lower.indexOf("select") + 6;
        int endAtts = lower.indexOf("from");
        String insideAtts = lower.substring(startAtts, endAtts);
        String insideCleaned = "";
        for (int i = 0; i < insideAtts.length(); i++) {
            if (insideAtts.charAt(i) != ' ') {
                insideCleaned += insideAtts.charAt(i);
            }
        }
        String[] attributes = insideCleaned.split(",");

        // get table name
        int startTable = lower.indexOf("from") + 4;
        while (lower.charAt(startTable) == ' ') {
            startTable++;
        }
        int endTable;
        if (lower.contains("where")) {
            endTable = lower.indexOf("where");
        } else {
            endTable = lower.length();
        }
        while (lower.charAt(endTable - 1) == ' ') {
            endTable--;
        }
        String tableName = lower.substring(startTable, endTable);

        // build schema from attributes
        ISchema schema = null;
        ITable targetTable = null;

        for (ITable tab : this.tables) {
            if (tab.getName().equalsIgnoreCase(tableName)) {
                targetTable = tab;
                schema = tab.getSchema();
                break;
            }
        }

        if (schema == null) {
            throw new InvalidQueryException();
        }

        Map<Integer, String> resultAtts = new HashMap<>();
        for (int i = 0; i < attributes.length; i++) {
            boolean matchFound = false;
            for (int j = 0; j < schema.getAttributes().size(); j++) {
                if (schema.getName(j).equalsIgnoreCase(attributes[i])) {
                    resultAtts.put(i, schema.getName(j) + ":" + schema.getType(j));
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                throw new InvalidQueryException();
            }
        }
        ISchema resultSchema = new Schema(resultAtts);
        ITable resultTable = new Table("result", resultSchema);

        // copy values into the result tuple
        for (ITable tab : this.tables) {
            if (tab.getName().equalsIgnoreCase(tableName)) {

                String whereAtt = null;
                String whereValue = null;
                int whereIndex = -1;
                if (lower.contains("where")) {
                    // just uses the same logic for the deleteData method (checks if where exists, if it doesn't it handles it)
                    int startWhere = lower.indexOf("where") + 5;
                    while (query.charAt(startWhere) == ' ') {
                        startWhere++;
                    }
                    int equalsIndex = query.indexOf("=", startWhere);

                    int endAttsWhere = equalsIndex;
                    while (query.charAt(endAttsWhere - 1) == ' ') {
                        endAttsWhere--;
                    }
                    whereAtt = query.substring(startWhere, endAttsWhere);

                    int startVals = equalsIndex + 1;
                    while (query.charAt(startVals) == ' ') {
                        startVals++;
                    }

                    int endVals = startVals;
                    while (endVals < query.length() && query.charAt(endVals) != ' ') {
                        endVals++;
                    }

                    whereValue = query.substring(startVals, endVals);

                    // remove any quotes
                    if (whereValue.length() >= 2 && whereValue.charAt(0) == '\'' && whereValue.charAt(whereValue.length() - 1) == '\'') {
                        whereValue = whereValue.substring(1, whereValue.length() - 1);
                    }

                    for (int i = 0; i < schema.getAttributes().size(); i++) {
                        if (schema.getName(i).equalsIgnoreCase(whereAtt)) {
                            whereIndex = i;
                            break;
                        }
                    }

                    if (whereIndex == -1) {
                        throw new InvalidQueryException();
                    }

                }

                for (ITuple tup : tab.getTuples()) {
                    if (lower.contains("where")) {
                        Object tupleValue = tup.getValue(whereIndex);
                        if (!tupleValue.toString().equalsIgnoreCase(whereValue)) {
                            continue;
                        }
                    }

                    ITuple resultTuple = new Tuple(resultSchema);

                    for (int i = 0; i < attributes.length; i++) {
                        for (int j = 0; j < schema.getAttributes().size(); j++) {
                            if (schema.getName(j).equalsIgnoreCase(attributes[i])) {
                                Object value = tup.getValue(j);
                                resultTuple.setValue(i, value);
                                break;
                            }
                        }
                    }
                    resultTable.addTuple(resultTuple);
                }
            }
        }
        return resultTable;
    }

    /**
     * Delete data from a table
     * If the query in not valid, throws an InvalidQueryException
     *
     * Implements the following algorithm
     *
     * Parse the query to get the from and where clauses
     * Parse the from clause to get the table name
     * If the query in not valid
     *   Throw an invalid query exception
     *   Exit
     * If where clause is not empty
     *   Parse the where clause to get the condition
     *   For each tuple in the table
     *     If the where clause condition is true
     *       Remove the tuple from the table
     * Else
     *   For each tuple in the table
     *     Remove the tuple from the table
     * Write the table to the file
     *
     * @param query the delete command given
     * @throws InvalidQueryException thrown if the delete clause is invalid
     */
    public void deleteData(String query) throws InvalidQueryException {

        // example where clause: DELETE FROM Student WHERE sid = ’s4’

        String lower = query.toLowerCase();
        if (!(lower.startsWith("delete from"))) {
            throw new InvalidQueryException();
        }

        // get table name
        int start = lower.indexOf("delete from") + "delete from".length();
        while (lower.charAt(start) == ' ') {
            start++;
        }
        int end;
        if (lower.contains("where")) {
            end = lower.indexOf("where");
        } else {
            end = lower.length();
        }
        while (lower.charAt(end - 1) == ' ') {
            end--;
        }
        String tableName = lower.substring(start, end);

        // find table
        ITable targetTable = null;
        for (ITable tab : this.tables) {
            if (tab.getName().equalsIgnoreCase(tableName)) {
                targetTable = tab;
                break;
            }
        }

        if (targetTable == null) {
            throw new InvalidQueryException();
        }

        // if WHERE doesn't exist, delete everything
        if (!lower.contains("where")) {
            targetTable.getTuples().clear();
            IO.writeTable(targetTable, this.folderName);
            return;
        }

        // if WHERE does exist
        int whereIndex = lower.indexOf("where") + 5;
        while (query.charAt(whereIndex) == ' ') {
            whereIndex++;
        }

        // attribute name
        int equalsIndex = query.indexOf("=", whereIndex);
        int startAtts = whereIndex;
        int endAtts = equalsIndex;

        while (query.charAt(endAtts - 1) == ' ') {
            endAtts--;
        }
        String att = query.substring(startAtts, endAtts);

        // value part
        int startVals = equalsIndex + 1;
        while (query.charAt(startVals) == ' ') {
            startVals++;
        }
        int endVals = startVals;
        while (endVals < query.length() && query.charAt(endVals) != ' ') {
            endVals++;
        }
        String value = query.substring(startVals, endVals);

        // get rid of any quotes
        if (value.length() >= 2 && value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
            value = value.substring(1, value.length() - 1);
        }

        // find attribute index
        int attIndex = -1;
        ISchema schema = targetTable.getSchema();

        for (int i = 0; i < schema.getAttributes().size(); i++) {
            if (schema.getName(i).equalsIgnoreCase(att)) {
                attIndex = i;
                break;
            }
        }

        if (attIndex == -1) {
            throw new InvalidQueryException();
        }

        // remove matching tuples
        List<ITuple> tuples = targetTable.getTuples();
        for (int i = 0; i < tuples.size(); i++) {
            ITuple tuple = tuples.get(i);
            Object tupleValue = tuple.getValue(attIndex);
            if (tupleValue.toString().equalsIgnoreCase(value)) {
                tuples.remove(i);
                i--; // have to change the index after removing tuple
            }
        }

        IO.writeTable(targetTable, this.folderName);
    }
}