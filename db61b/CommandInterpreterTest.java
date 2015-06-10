package db61b;

import java.util.Scanner;

import org.junit.Test;

import static org.junit.Assert.*;

/** Test for CommandInterpreter.
*  @author Randy Shi
*/
public class CommandInterpreterTest {

    /** Tests loadStatement. */
    @Test
    public void testLoadStatement() {
        Scanner input = new Scanner("load happy ;");
        CommandInterpreter interpreter =
            new CommandInterpreter(input, System.out);
        interpreter.statement();
        Table t = Table.readTable(System.getProperty("user.dir") + "/happy");
        assertEquals(t.size(), interpreter.getDatabase().get("happy").size());
    }

    /** Tests createStatement and selectStatement. */
    @Test
    public void testCreateSelectStatements() {
        Table t = Table.readTable(System.getProperty("user.dir") + "/happy");
        Scanner input =
                new Scanner("create table yes as select SID from happy;");
        CommandInterpreter interpreter =
            new CommandInterpreter(input, System.out);
        interpreter.getDatabase().put("happy", t);
        interpreter.statement();
        Table s = interpreter.getDatabase().get("yes");
        Row toCheck = null;
        for (Row r : s.getRows()) {
            toCheck = r;
        }
        assertEquals(true, toCheck.equals(new Row(new String[] {"101"})));
        s.writeTable("yes");
        Table t2 = Table.readTable(System.getProperty("user.dir") + "/yes");
        for (Row r : t2.getRows()) {
            toCheck = r;
        }
        assertEquals(true, toCheck.equals(new Row(new String[] {"101"})));
    }

    /** Tests the methods of CommandInterpreter. */
    public static void main(String[] args) {
        Table test = new Table(new String[] {"SID", "CCN", "Grade"});
        test.add(new Row(new String[] {"101", "21228", "B"}));
        test.writeTable("happy");
        System.exit(ucb.junit.textui.runClasses(CommandInterpreterTest.class));
    }

}
