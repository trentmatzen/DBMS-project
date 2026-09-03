import java.util.Scanner;

public class Main {

    /**
     * The main program should run as interactive command line tool
     *
     * Implements the following algorithm
     *
     * Loop
     *   Wait for user to type a query (on a single line)
     *   Exit if input is "exit"
     *   Run the query
     *   Print the result (if any) or modify the data in the database
     *
     * @param args
     */
    public static void main(String[] args) {
        Database db = new Database("DBMS/db", "schema");
        Scanner sc = new Scanner(System.in);

        while (true) {
            String query = sc.nextLine();
            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                runQuery(query, db);
            } catch (InvalidQueryException ex) {
                System.err.println("Invalid query.");
            }
        }
    }

    /**
     * Runs the given query on the database
     *
     * Implements the following algorithm
     *
     * Determine the type of query (from select, insert or delete)
     * If select query
     *   Select data
     *   Print results
     * Else if insert query
     *   Insert data
     * Else if delete is given
     *   Delete data
     *
     * @param query the query to perform (insert, select, delete)
     * @param db the database of .csv or .txt files
     */
    public static void runQuery(String query, Database db) throws InvalidQueryException{
        String lower = query.toLowerCase();
        if (lower.startsWith("select")) {
            ITable result = db.selectData(query);
            IO.printTable(result, result.getSchema());
        } else if (lower.startsWith("insert")) {
            db.insertData(query);
        } else if (lower.startsWith("delete")) {
            db.deleteData(query);
        } else {
            throw new InvalidQueryException();
        }
    }
}