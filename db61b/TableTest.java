package db61b;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

/** Test for Table.
*  @author Randy Shi
*/
public class TableTest {

    /** Tests columns. */
    @Test
    public void testColumns() {
        Table t = new Table(new String[] {"a", "b", "c"});
        assertEquals(3, t.columns());
    }

    /** Tests getTitle. */
    @Test
    public void testGetTitle() {
        Table t = new Table(new String[] {"a", "b", "c"});
        assertEquals("b", t.getTitle(1));
        assertEquals("Error: Index out of bounds.", t.getTitle(4));
    }

    /** Tests findColumn. */
    @Test
    public void testFindColumn() {
        Table t = new Table(new String[] {"a", "b", "c"});
        assertEquals(1, t.findColumn("b"));
        assertEquals(-1, t.findColumn("d"));
    }

    /** Tests size and add. */
    @Test
    public void testSizeAndAdd() {
        Table t = new Table(new String[] {"a", "b", "c"});
        assertEquals(true, t.add(new Row(null)));
        assertEquals(1, t.size());
        assertEquals(false, t.add(new Row(null)));
        assertEquals(1, t.size());
    }

    /** Tests readTable. */
    @Test
    public void testReadTable() {
        Table t = Table.readTable(System.getProperty("user.dir")
                + "/testing/test");
        Table test = new Table(new String[] {"SID", "CCN", "Grade"});
        test.add(new Row(new String[] {"101", "21228", "B"}));
        assertEquals(t.getTitle(0), test.getTitle(0));
        assertEquals(t.getTitle(1), test.getTitle(1));
        assertEquals(t.getTitle(2), test.getTitle(2));
        assertEquals(t.size(), test.size());
    }

    /** Tests writeTable. */
    @Test
    public void testWriteTable() {
        Table test = new Table(new String[] {"SID", "CCN", "Grade"});
        test.add(new Row(new String[] {"101", "21228", "B"}));
        test.writeTable("happy");
        Table t = Table.readTable(System.getProperty("user.dir") + "/happy");
        assertEquals(t.getTitle(0), test.getTitle(0));
        assertEquals(t.getTitle(1), test.getTitle(1));
        assertEquals(t.getTitle(2), test.getTitle(2));
        assertEquals(t.size(), test.size());
    }

    /** Tests the first select method. */
    @Test
    public void testFirstSelect() {
        Table t = new Table(new String[] {"SID", "CCN", "Grade"});
        t.add(new Row(new String[] {"101", "21228", "B"}));
        t.add(new Row(new String[] {"102", "21229", "A"}));
        ArrayList<String> list = new ArrayList<String>();
        ArrayList<Condition> list2 = new ArrayList<Condition>();
        Column one = new Column("SID", t);
        Column two = new Column("CCN", t);
        list.add(one.getName());
        list.add(two.getName());
        Condition c = new Condition(one, "=", "101");
        Condition d = new Condition(two, "<", "30000");
        list2.add(c);
        list2.add(d);
        Table test = t.select(list, list2);
        Row compare = null;
        for (Row r : test.getRows()) {
            compare = r;
        }
        Row compareTo = new Row(new String[] {"101", "21228"});
        assertEquals(true, compare.equals(compareTo));
    }

    /** Tests the second select method. */
    @Test
    public void testSecondSelect() {
        Table t = new Table(new String[] {"SID", "CCN", "Grade"});
        Table t2 = new Table(new String[] {"SID", "First", "Last"});
        t.add(new Row(new String[] {"101", "21228", "B"}));
        t.add(new Row(new String[] {"102", "21229", "A"}));
        t2.add(new Row(new String[] {"101", "Joe", "Bee"}));
        t2.add(new Row(new String[] {"102", "Mark", "Ann"}));
        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<Condition> conds = new ArrayList<Condition>();
        Column one = new Column("SID", t, t2);
        Column two = new Column("First", t, t2);
        titles.add(one.getName());
        titles.add(two.getName());
        Condition c = new Condition(one, "=", "101");
        Condition d = new Condition(two, "<", "Zack");
        conds.add(c);
        conds.add(d);
        Table test = t.select(t2, titles, conds);
        Row compare = null;
        for (Row r : test.getRows()) {
            compare = r;
        }
        Row compareTo = new Row(new String[] {"101", "Joe"});
        assertEquals(true, compare.equals(compareTo));
    }

    /** Tests equijoin. */
    @Test
    public void testEquijoin() {
        Table test = new Table(new String[] {"SID", "Grade"});
        Table test2 = new Table(new String[] {"SID", "Grade"});
        Row row1 = new Row(new String[] {"102", "A"});
        Row row2 = new Row(new String[] {"102", "A"});
        test.add(new Row(new String[] {"102", "A"}));
        test2.add(new Row(new String[] {"102", "A"}));
        test.add(new Row(new String[] {"103", "B"}));
        test2.add(new Row(new String[] {"103", "B"}));
        Column tOne = new Column("SID", test);
        Column tTwo = new Column("Grade", test);
        Column t2One = new Column("SID", test2);
        Column t2Two = new Column("Grade", test2);
        ArrayList<Column> cols = new ArrayList<Column>();
        ArrayList<Column> cols2 = new ArrayList<Column>();
        cols.add(tOne);
        cols.add(tTwo);
        cols2.add(t2One);
        cols2.add(t2Two);
        assertEquals(true, Table.equijoin(cols, cols2, row1, row2));
    }

    /** Tests the methods of Table.
     */
    public static void main(String[] args) {
        Table t = Table.readTable(System.getProperty("user.dir")
                + "/testing/enrolled");
        t.print();
        System.exit(ucb.junit.textui.runClasses(TableTest.class));
    }

}
