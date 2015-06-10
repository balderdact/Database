
package db61b;

import java.util.HashMap;

/** A collection of Tables, indexed by name.
 *  @author Randy Shi
 */
class Database {
    /** An empty database. */
    public Database() {
        _tables = new HashMap<String, Table>();
    }

    /** Return the Table whose name is NAME stored in this database, or null
     *  if there is no such table. */
    public Table get(String name) {
        if (_tables.containsKey(name)) {
            return _tables.get(name);
        }
        return null;
    }

    /** Set or replace the table named NAME in THIS to TABLE.  TABLE and
     *  NAME must not be null, and NAME must be a valid name for a table. */
    public void put(String name, Table table) {
        if (name == null || table == null) {
            throw new IllegalArgumentException("null argument");
        }
        _tables.put(name, table);
    }

    /** Returns _tables. */
    public HashMap<String, Table> getTables() {
        return _tables;
    }

    /** My tables. */
    private HashMap<String, Table> _tables;
}
