package seedu.address.logic.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

//@@author A0140023E
public class DateTimeUtilTest {


    @Test
    public void isSingleDateTimeString() {
        // multiple date groups
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed ~ Thursday")); // separated by unknown token
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed ` Thursday")); // separated by unknown token
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed plus Thursday")); // separated by unknown token

        // ignored tokens
        assertFalse(DateTimeUtil.isSingleDateTimeString("We.d"));

        // multiple date alternatives
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed or Thur"));
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed and Thur"));

        // recurring dates
        assertFalse(DateTimeUtil.isSingleDateTimeString("every Friday"));

        // valid single dates
        assertTrue(DateTimeUtil.isSingleDateTimeString("Sat"));
    }

}
