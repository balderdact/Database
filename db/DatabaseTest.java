package db61b;

import org.junit.Test;

import static org.junit.Assert.*;

/** Test for Database.
*  @author Randy Shi
*/
public class DatabaseTest {

    /** Tests get and put. */
    @Test
    public void testGetAndPut() {
        Database d = new Database();
        Table t = new Table(new String[] {});
        Table f = new Table(new String[] {"e"});
        assertEquals(null, d.get("stuff"));
        d.put("stuff", t);
        assertEquals(t, d.get("stuff"));
        d.put("stuff", f);
        assertEquals(f, d.get("stuff"));
    }

    /** Tests the methods of Database. */
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(DatabaseTest.class));
    }

}
