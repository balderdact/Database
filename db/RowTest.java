package db61b;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

/** Test for Row.
*  @author Randy Shi
*/
public class RowTest {

    /** Tests Row constructor. */
    @Test
    public void testConstructor() {
        Table test = new Table(new String[] {"SID", "CCN", "Grade"});
        Table test2 = new Table(new String[] {"Firstname", "Lastname"});
        Column one = new Column("SID", test, test2);
        Column two = new Column("CCN", test);
        Column four = new Column("Grade", test);
        Column three = new Column("Firstname", test, test2);
        ArrayList<Column> list = new ArrayList<Column>();
        list.add(one);
        list.add(three);
        ArrayList<Column> list2 = new ArrayList<Column>();
        list2.add(two);
        list2.add(four);
        Row a = new Row(new String[] {"101", "21228", "B"});
        Row c = new Row(new String[] {"Randy", "Shi"});
        Row tst = new Row(list, a, c);
        Row toCheck = new Row(new String[] {"101", "Randy"});
        assertEquals(true, toCheck.equals(tst));
        Row toCheck2 = new Row(new String[] {"21228", "B"});
        Row tst2 = new Row(list2, a);
        assertEquals(true, toCheck2.equals(tst2));
    }

    /** Tests size. */
    @Test
    public void testSize() {
        Row r = new Row(new String[] {"a", "b", "c"});
        assertEquals(3, r.size());
    }

    /** Tests get. */
    @Test
    public void testGet() {
        Row r = new Row(new String[] {"a", "b", "c"});
        assertEquals("Error: Index out of bounds.", r.get(3));
        assertEquals("c", r.get(2));
    }

    /** Tests equals. */
    @Test
    public void testEquals() {
        Row r = new Row(new String[] {"a", "b", "c"});
        assertEquals(true, r.equals(new Row(new String[] {"a", "b", "c"})));
        assertEquals(false, r.equals(new Row(new String[] {"b", "a", "c"})));
    }

    /** Tests the methods of Row. */
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(RowTest.class));
    }
}
