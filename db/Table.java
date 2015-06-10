
package db61b;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static db61b.Utils.*;

/** A single table in a database.
 *  @author P. N. Hilfinger
 */
class Table implements Iterable<Row> {
    /** A new Table whose columns are given by COLUMNTITLES, which may
     *  not contain duplicate names. */
    Table(String[] columnTitles) {
        for (int i = columnTitles.length - 1; i >= 1; i -= 1) {
            for (int j = i - 1; j >= 0; j -= 1) {
                if (columnTitles[i].equals(columnTitles[j])) {
                    throw error("duplicate column name: %s",
                                columnTitles[i]);
                }
            }
        }
        _columnTitles = columnTitles;
    }

    /** A new Table whose columns are give by COLUMNTITLES. */
    Table(List<String> columnTitles) {
        this(columnTitles.toArray(new String[columnTitles.size()]));
    }

    /** Return the number of columns in this table. */
    public int columns() {
        return _columnTitles.length;
    }

    /** Return the title of the Kth column.  Requires 0 <= K < columns(). */
    public String getTitle(int k) {
        try {
            return _columnTitles[k];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Error: Index out of bounds.";
        }
    }

    /** Returns true if COL is in T. */
    public static boolean containsTitle(String col, Table t) {
        boolean containsTitle = false;
        for (String title : t.getColTitles()) {
            if (title.equals(col)) {
                containsTitle = true;
            }
        }
        return containsTitle;
    }

    /** Return the number of the column whose title is TITLE, or -1 if
     *  there isn't one. */
    public int findColumn(String title) {
        for (int i = 0; i < _columnTitles.length; i++) {
            if (_columnTitles[i].equals(title)) {
                return i;
            }
        }
        return -1;
    }

    /** Return the number of Rows in this table. */
    public int size() {
        return _rows.size();
    }

    /** Returns an iterator that returns my rows in an unspecified order. */
    @Override
    public Iterator<Row> iterator() {
        return _rows.iterator();
    }

    /** Add ROW to THIS if no equal row already exists.  Return true if anything
     *  was added, false otherwise. */
    public boolean add(Row row) {
        for (Row r : _rows) {
            if (r.equals(row)) {
                return false;
            }
        }
        _rows.add(row);
        return true;
    }

    /** Read the contents of the file NAME.db, and return as a Table.
     *  Format errors in the .db file cause a DBException. */
    static Table readTable(String name) {
        BufferedReader input;
        Table table;
        input = null;
        table = null;
        try {
            input = new BufferedReader(new FileReader(name + ".db"));
            String header = input.readLine();
            if (header == null) {
                throw error("missing header in DB file");
            }
            String[] columnNames = header.split(",");
            table = new Table(columnNames);
            String row = "";
            while (row != null) {
                row = input.readLine();
                if (row != null) {
                    table.add(new Row(row.split(",")));
                }
            }
        } catch (FileNotFoundException e) {
            throw error("could not find %s.db", name);
        } catch (IOException e) {
            throw error("problem reading from %s.db", name);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    /* Ignore IOException */
                }
            }
        }
        return table;
    }

    /** Write the contents of TABLE into the file NAME.db. Any I/O errors
     *  cause a DBException. */
    void writeTable(String name) {
        PrintStream output;
        output = null;
        try {
            String sep;
            sep = "";
            output = new PrintStream(name + ".db");
            for (String str : _columnTitles) {
                sep += str + ",";
            }
            sep = sep.substring(0, sep.length() - 1);
            sep += "\n";
            for (Row r : _rows) {
                for (String str : r.returnRow()) {
                    sep += str + ",";
                }
                sep = sep.substring(0, sep.length() - 1);
                sep += "\n";
            }
            output.append(sep);
        } catch (IOException e) {
            throw error("trouble writing to %s.db", name);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    /** Print my contents on the standard output. */
    void print() {
        for (Row r : _rows) {
            System.out.print("  ");
            for (String str: r.returnRow()) {
                System.out.print(str + " ");
            }
            System.out.println();
        }
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected from
     *  rows of this table that satisfy CONDITIONS. */
    Table select(List<String> columnNames, List<Condition> conditions) {
        Table result = new Table(columnNames);
        ArrayList<Column> columns = new ArrayList<Column>();
        for (String name : columnNames) {
            columns.add(new Column(name, this));
        }
        boolean add = true;
        for (Row r : _rows) {
            for (Condition c : conditions) {
                if (!c.test(r)) {
                    add = false;
                }
            }
            if (add) {
                result.add(new Row(columns, r));
            }
            add = true;
        }
        return result;
    }

    /** Return a new Table whose columns are COLUMNNAMES, selected
     *  from pairs of rows from this table and from TABLE2 that match
     *  on all columns with identical names and satisfy CONDITIONS. */
    Table select(Table table2, List<String> columnNames,
                 List<Condition> conditions) {
        Table result = new Table(columnNames);
        ArrayList<Column> columns = new ArrayList<Column>();
        for (String name : columnNames) {
            columns.add(new Column(name, this, table2));
        }
        boolean add = true;
        List<Column> colList1 = new ArrayList<Column>();
        List<Column> colList2 = new ArrayList<Column>();
        formEquicols(colList1, colList2, this, table2);
        for (Row r : _rows) {
            for (Row r2 : table2.getRows()) {
                if (equijoin(colList1, colList2, r, r2)) {
                    for (Condition c : conditions) {
                        if (!c.test(r, r2)) {
                            add = false;
                        }
                    }
                    if (add) {
                        result.add(new Row(columns, r, r2));
                    }
                    add = true;
                }
            }
        }
        return result;
    }

    /** Return true if the columns COMMON1 from ROW1 and COMMON2 from
     *  ROW2 all have identical values.  Assumes that COMMON1 and
     *  COMMON2 have the same number of elements and the same names,
     *  that the columns in COMMON1 apply to this table, those in
     *  COMMON2 to another, and that ROW1 and ROW2 come, respectively,
     *  from those tables. */
    static boolean equijoin(List<Column> common1, List<Column> common2,
                                    Row row1, Row row2) {
        Row rowToCheck1 = new Row(common1, row1);
        Row rowToCheck2 = new Row(common2, row2);
        return rowToCheck1.equals(rowToCheck2);
    }

    /** Returns a new Table formed from ORIGS, COLNAMES and COLUMNS. */
    static Table formTable(List<String> colNames,
            List<Column> columns, Table... origs) {
        Table toReturn = new Table(colNames);
        if (origs.length < 2) {
            for (Row r : origs[0].getRows()) {
                toReturn.add(new Row(columns, r));
            }
        } else {
            boolean hasEqualCol = hasEqualCol(origs[0], origs[1]);
            if (!hasEqualCol) {
                for (Row r : origs[0].getRows()) {
                    for (Row r2 : origs[1].getRows()) {
                        toReturn.add(new Row(columns, r, r2));
                    }
                }
            } else {
                List<Column> colList1 = new ArrayList<Column>();
                List<Column> colList2 = new ArrayList<Column>();
                formEquicols(colList1, colList2, origs[0], origs[1]);
                for (Row r : origs[0].getRows()) {
                    for (Row r2 : origs[1].getRows()) {
                        if (equijoin(colList1, colList2, r, r2)) {
                            toReturn.add(new Row(columns, r, r2));
                        }
                    }
                }
            }
        }
        return toReturn;
    }

    /** Forms COLLIST1 and COLLIST2 for equijoin from TABLES. */
    static void formEquicols(List<Column> colList1,
            List<Column> colList2, Table...tables) {
        for (String title : tables[0].getColTitles()) {
            for (String title2 : tables[1].getColTitles()) {
                if (title2.equals(title)) {
                    colList1.add(new Column(title2, tables[0]));
                    colList2.add(new Column(title2, tables[1]));
                }
            }
        }
    }

    /** Returns true if there is at least one column
     *  contained in T and T2. */
    static boolean hasEqualCol(Table t, Table t2) {
        boolean hasEqualCol = false;
        for (String colName : t.getColTitles()) {
            if (containsTitle(colName, t2)) {
                hasEqualCol = true;
            }
        }
        return hasEqualCol;
    }

    /** Returns _rows. */
    public HashSet<Row> getRows() {
        return _rows;
    }

    /** Returns _columnTitles. */
    public String[] getColTitles() {
        return _columnTitles;
    }

    /** My rows. */
    private HashSet<Row> _rows = new HashSet<>();
    /** My columnTitles. */
    private String[] _columnTitles;
}

