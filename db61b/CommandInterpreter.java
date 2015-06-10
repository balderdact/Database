
package db61b;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static db61b.Utils.*;
import static db61b.Tokenizer.*;

/** An object that reads and interprets a sequence of commands from an
 *  input source.
 *  @author Randy Shi
 *  (Discussed with Kenny Ma and Jae Lee). */
class CommandInterpreter {

    /** A new CommandInterpreter executing commands read from INP, writing
     *  prompts on PROMPTER, if it is non-null. */
    CommandInterpreter(Scanner inp, PrintStream prompter) {
        _input = new Tokenizer(inp, prompter);
        _database = new Database();
    }

    /** Parse and execute one statement from the token stream.  Return true
     *  iff the command is something other than quit or exit. */
    boolean statement() {
        switch (_input.peek()) {
        case "create":
            createStatement();
            break;
        case "load":
            loadStatement();
            break;
        case "exit": case "quit":
            exitStatement();
            return false;
        case "*EOF*":
            return false;
        case "insert":
            insertStatement();
            break;
        case "print":
            printStatement();
            break;
        case "select":
            selectStatement();
            break;
        case "store":
            storeStatement();
            break;
        default:
            throw error("unrecognizable command");
        }
        return true;
    }

    /** Parse and execute a create statement from the token stream. */
    void createStatement() {
        _input.next("create");
        _input.next("table");
        String name = name();
        Table table = tableDefinition();
        _database.put(name, table);
        _input.next(";");
    }

    /** Parse and execute an exit or quit statement. Actually does nothing
     *  except check syntax, since statement() handles the actual exiting. */
    void exitStatement() {
        if (!_input.nextIf("quit")) {
            _input.next("exit");
        }
        _input.next(";");
    }

    /** Parse and execute an insert statement from the token stream. */
    void insertStatement() {
        _input.next("insert");
        _input.next("into");
        Table table = tableName();
        _input.next("values");
        ArrayList<String> values = new ArrayList<>();
        values.add(literal());
        while (_input.nextIf(",")) {
            values.add(literal());
        }
        _input.next(";");
        if (values.size() != table.columns()) {
            System.out.println("Error: inserted row has wrong length");
            return;
        }
        table.add(new Row(values.toArray(new String[values.size()])));
    }

    /** Parse and execute a load statement from the token stream. */
    void loadStatement() {
        _input.next("load");
        String name = name();
        Table t = Table.readTable(System.getProperty("user.dir") + "/" + name);
        _input.next(";");
        _database.put(name, t);
        System.out.println("Loaded " + name + ".db");
    }

    /** Parse and execute a store statement from the token stream. */
    void storeStatement() {
        _input.next("store");
        String name = _input.peek();
        Table table = tableName();
        table.writeTable(name);
        System.out.printf("Stored %s.db%n", name);
        _input.next(";");
    }

    /** Parse and execute a print statement from the token stream. */
    void printStatement() {
        _input.next("print");
        String name = name();
        _input.next(";");
        if (_database.get(name) == null) {
            System.out.println("Error: " + name + ".db"
                    + " does not exist or is not loaded.");
        } else {
            System.out.println("Contents of " + name + ":");
            _database.get(name).print();
        }
    }

    /** Parse and execute a select statement from the token stream. */
    void selectStatement() {
        Table select = selectClause();
        _input.next(";");
        System.out.println("Search results: ");
        select.print();
    }

    /** Parse and execute a table definition, returning the specified
     *  table. */
    Table tableDefinition() {
        Table table;
        if (_input.nextIf("(")) {
            ArrayList<String> columnNames = new ArrayList<String>();
            columnNames.add(columnName());
            while (_input.nextIs(",")) {
                _input.next();
                columnNames.add(columnName());
            }
            _input.next(")");
            table = new Table(columnNames);
        } else {
            _input.next("as");
            table = selectClause();
        }
        return table;
    }

    /** Parse and execute a select clause from the token stream, returning the
     *  resulting table. */
    Table selectClause() {
        ArrayList<String> columnNames = new ArrayList<String>();
        ArrayList<Condition> conds = new ArrayList<Condition>();
        _input.next("select");
        columnNames.add(columnName());
        while (_input.nextIs(",")) {
            _input.next();
            columnNames.add(columnName());
        }
        _input.next("from");
        Table t = tableName(), toMake = null;
        if (_input.nextIs(",")) {
            _input.next();
            Table t2 = tableName();
            toMake = finalizeSelect(columnNames, conds, t, t2);
        } else {
            toMake = finalizeSelect(columnNames, conds, t);
        }
        return toMake;
    }

    /** Parse and return a valid name (identifier) from the token stream. */
    String name() {
        return _input.next(Tokenizer.IDENTIFIER);
    }

    /** Parse and return a valid column name from the token stream. Column
     *  names are simply names; we use a different method name to clarify
     *  the intent of the code. */
    String columnName() {
        return name();
    }

    /** Parse a valid table name from the token stream, and return the Table
     *  that it designates, which must be loaded. */
    Table tableName() {
        String name = name();
        Table table = _database.get(name);
        if (table == null) {
            throw error("unknown table: %s", name);
        }
        return table;
    }

    /** Parse a literal and return the string it represents (i.e., without
     *  single quotes). */
    String literal() {
        String lit = _input.next(Tokenizer.LITERAL);
        return lit.substring(1, lit.length() - 1).trim();
    }

    /** Builds a condition from TABLES and adds it to CONDS.
     *  If there are multiple 'and' clauses, then recurse
     *  for every 'and' clause. */
    void buildCondition(List<Condition> conds, Table... tables) {
        boolean twoTables = tables.length == 2;
        String check = "";
        _input.next();
        String nameCol = columnName();
        String relation = _input.next(Tokenizer.RELATION);
        Column c = twoTables
                ? new Column(nameCol, tables[0], tables[1])
                : new Column(nameCol, tables[0]);
        String peek = _input.peek();
        boolean containsTitle = twoTables
                ? Table.containsTitle(peek, tables[0])
                || Table.containsTitle(peek, tables[1])
                : Table.containsTitle(peek, tables[0]);
        if (!containsTitle) {
            check = literal();
            conds.add(new Condition(c, relation, check));
        } else {
            check = columnName();
            conds.add(new Condition(c, relation, twoTables
                    ? new Column(check, tables[0], tables[1])
                    : new Column(check, tables[0])));
        }
        if (!_input.nextIs("and")) {
            return;
        }
        buildCondition(conds, tables);
    }

    /** Return a finalized select table from TABLES.
     *  Takes in COLUMNNAMES and CONDS. */
    Table finalizeSelect(List<String> columnNames,
            List<Condition> conds, Table... tables) {
        ArrayList<Column> columns = new ArrayList<Column>();
        Table toMake = null;
        boolean twoTables = tables.length == 2;
        for (String name : columnNames) {
            columns.add(twoTables
                    ? new Column(name, tables[0], tables[1])
                    : new Column(name, tables[0]));
        }
        if (!_input.nextIs("where")) {
            toMake = twoTables
                    ? Table.formTable(columnNames, columns,
                            tables[0], tables[1])
                    : Table.formTable(columnNames, columns, tables[0]);
        } else {
            if (twoTables) {
                buildCondition(conds, tables[0], tables[1]);
            } else {
                buildCondition(conds, tables[0]);
            }
            toMake = twoTables
                    ? tables[0].select(tables[1], columnNames, conds)
                    : tables[0].select(columnNames, conds);
        }
        return toMake;
    }

    /** Advance the input past the next semicolon. */
    void skipCommand() {
        while (true) {
            try {
                while (!_input.nextIf(";") && !_input.nextIf("*EOF*")) {
                    _input.next();
                }
                return;
            } catch (DBException excp) {
                /* No action */
            }
        }
    }

    /** Returns _database. */
    public Database getDatabase() {
        return _database;
    }

    /** The command input source. */
    private Tokenizer _input;
    /** Database containing all tables. */
    private Database _database;
}
