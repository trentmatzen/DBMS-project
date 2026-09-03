import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * This is the IO utility class
 */
public class IO {

    /**
     * Reads the table's data from a csv file
     *
     * Implements the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     *   For each line in the csv file
     *     Parse the line to get attribute values
     *     Create a new tuple with the schema of the table
     *     Set the tuple values to the attribute values
     *     Add the tuple to the table
     * Close file
     *
     * Return table
     * @param tablename the name of the table
     * @param schema the table's schema
     * @param folder the folder containing the file to read
     * @return the table's data
     */
    public static ITable readTable(String tablename, ISchema schema, String folder) {
        ITable table;
        try (BufferedReader br = new BufferedReader(new FileReader(folder + "/" + tablename + ".csv"))) {
            String line = null;
            table = new Table(tablename, schema);
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                ITuple tuple = new Tuple(schema);
                tuple.setValues(values);
                table.addTuple(tuple);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return table;
    }

    /**
     * Writes the tables' data to a csv file
     *
     * Implements the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     * Clear all file content
     * For each tuple in table
     *   Write the tuple values to the file in csv format
     *
     * @param table the table to write
     * @param folder the folder to write the file to
     */
    public static void writeTable(ITable table, String folder) {
        String tableName = table.getName();
        List<ITuple> tuples = table.getTuples();
        try (PrintWriter pw = new PrintWriter(new FileWriter(folder + "/" + tableName + ".csv"))){
            boolean firstValue;
            for (ITuple tuple : tuples) {
                firstValue = true;
                Object[] values = tuple.getValues();
                for (Object value : values) {
                    if (!firstValue) {
                        pw.print(",");
                    }
                    pw.print(value);
                    firstValue = false;
                }
                pw.println();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Prints the table to console (mainly used to print the output of the select query)
     *
     * Implements the following algorithm
     *
     * Print the attribute names from the schema as tab separated values
     * For each tuple in the table
     *   Print the values in tab separated format
     *
     *
     * @param table the table to print
     * @param schema the schema with the attributes to print
     */
    public static void printTable(ITable table, ISchema schema) {
        Map<Integer, String> attributes = schema.getAttributes();

        for (int i = 0; i < attributes.size(); i++) {
            System.out.print(schema.getName(i) + "\t");
        }

        System.out.println();

        List<ITuple> tuples = table.getTuples();
        boolean firstValue;
        for (ITuple tuple : tuples) {
            firstValue = true;
            Object[] values = tuple.getValues();
            for (Object value : values) {
                if (!firstValue) {
                    System.out.print("\t");
                }
                System.out.print(value);
                firstValue = false;
            }
            System.out.println();
        }
    }


    /**
     * Writes a tuple to a csv file
     *
     * Implements the following algorithm
     *
     * Open the csv file from the folder (corresponding to the tablename)
     * Append the tuple (as array of strings) in the csv format to the file
     *
     * @param tableName the name of the table
     * @param values the values to append to the file
     * @param folder the folder to write the file to
     */
    public static void writeTuple(String tableName, Object[] values, String folder) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(folder + "/" + tableName + ".csv", true))){
            String lineToAdd = "";
            for (int i = 0; i < values.length; i++) {
                lineToAdd += values[i] + ",";
            }
            String result = lineToAdd.substring(0, lineToAdd.length() - 1);
            pw.println(result);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Reads and parses the schema, creates schema objects and (empty) tables and adds them to the provided database
     * The schema is stored in a text file:
     *
     * Implements the following algorithm
     *
     * Open the schema file
     * For each line
     *   Parse the line to get the table name, attribute names and attribute types
     *   Create an attribute map of (index, att_name:att_type) pairs
     *   For each attribute
     *     Store the index and name:type pair in the map (index represents the position of attribute in the schema)
     *   Create a new schema object with this attribute map
     *   Add the schema object to the database
     *   Create a new table object with the table name and the schema object
     *   Add the table to the database
     *
     * @param schemaFileName name of the file containing the schema
     * @param folderName name of the folder
     * @param db the database to read schema from
     */
    public static void readSchema(String schemaFileName, String folderName, Database db) {
        try (BufferedReader br = new BufferedReader(new FileReader(folderName + "/" + schemaFileName))) {
            String lineToRead;
            Map<Integer, String> attributeMap = new HashMap<>();
            while ((lineToRead = br.readLine()) != null)
            {
                if (lineToRead.equals("")){
                    continue;
                }
                String tableName = lineToRead.substring(0, lineToRead.indexOf("("));
                String subLineToRead = lineToRead.substring(lineToRead.indexOf("(") + 1, lineToRead.indexOf(")"));
                String[] parts = subLineToRead.split(",");

                // Example line: student(sid:String, sname:String, major:String, byear:Integer)
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].charAt(0) == ' ') {
                        parts[i] = parts[i].substring(1);
                    }
                }
                for (int i = 0; i < parts.length; i++) {
                    attributeMap.put(i, parts[i]);
                }

                ISchema schema = new Schema(attributeMap);
                db.addSchema(schema);

                ITable table = new Table(tableName, schema);
                db.addTable(table);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}