package db61b;

import org.junit.Test;

import static org.junit.Assert.*;

/** Test for Condition.
*  @author Randy Shi
*/
public class ConditionTest {

    /** Tests test. */
    @Test
    public void testTest() {
        Table test = new Table(new String[] {"SID", "CCN"});
        Row one = new Row(new String[] {"102", "12345"});
        Row two = new Row(new String[] {"103", "22345"});
        test.add(one);
        test.add(two);
        Column testCol = new Column("SID", test);
        Condition testCon1 = new Condition(testCol, "=", "102");
        Condition testCon2 = new Condition(testCol, ">", "CCN");
        Condition testCon3 = new Condition(testCol, "<=", "101");
        Condition testCon4 = new Condition(testCol, "!=", "174");
        assertEquals(true, testCon1.test(one, two));
        assertEquals(false, testCon2.test(one, two));
        assertEquals(false, testCon3.test(one, two));
        assertEquals(true, testCon4.test(one, two));
    }

    /** Tests the methods of Condition. */
    public static void main(String[] args) {
        System.exit(ucb.junit.textui.runClasses(ConditionTest.class));
    }
}
