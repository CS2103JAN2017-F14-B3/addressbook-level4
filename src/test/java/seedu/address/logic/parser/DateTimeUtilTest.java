package seedu.address.logic.parser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import seedu.address.commons.exceptions.IllegalValueException;

//@@author A0140023E
// TODO improve test
public class DateTimeUtilTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void parseDateTimeString_validDateTimes_noExceptionThrown() throws IllegalValueException {
        //org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

        DateTimeUtil.parseDateTimeString("Sat");
        DateTimeUtil.parseDateTimeString("Fri");
        DateTimeUtil.parseDateTimeString("25 Apr");
        DateTimeUtil.parseDateTimeString("Jan 2017");
        DateTimeUtil.parseDateTimeString("2 days after");
        DateTimeUtil.parseDateTimeString("2016-05-02");
    }

    @Test
    public void parseDateTimeString_invalidDateTime_throwsException() throws IllegalValueException {
        assertInvalidDateTime("Not a date");
    }
    @Test
    public void parseDateTimeString_invalidDateTimeWithSymbols_throwsException() throws IllegalValueException {
        assertInvalidDateTime("We.d");
    }

    @Test
    public void parseDateTimeString_multipleDateTimes_throwsException() throws IllegalValueException {
        assertMultipleDateTimesFound("Wed ~ Thur");
    }

    @Test
    public void parseDateTimeString_multipleDateTimeAlternatives_throwsException() throws IllegalValueException {
        assertMultipleDateTimeAlternativesFound("Wed or Thur");
    }
    @Test
    public void parseDateTimeString_recurringDateTime_throwsException() throws IllegalValueException {
        assertRecurringDateTimesFound("every Friday");
    }

    public void assertInvalidDateTime(String dateTime) throws IllegalValueException {
        exception.expect(IllegalValueException.class);
        exception.expectMessage(String.format(DateTimeUtil.MESSAGE_NOT_VALID_DATE_TIME, dateTime));
        DateTimeUtil.parseDateTimeString(dateTime);
    }

    public void assertMultipleDateTimesFound(String dateTime) throws IllegalValueException {
        exception.expect(IllegalValueException.class);
        exception.expectMessage(String.format(DateTimeUtil.MESSAGE_MULTIPLE_DATE_TIMES_FOUND, dateTime));
        DateTimeUtil.parseDateTimeString(dateTime);
    }

    public void assertMultipleDateTimeAlternativesFound(String dateTime) throws IllegalValueException {
        exception.expect(IllegalValueException.class);
        exception.expectMessage(String.format(DateTimeUtil.MESSAGE_MULTIPLE_DATE_TIME_ALTERNATIVES_FOUND, dateTime));
        DateTimeUtil.parseDateTimeString(dateTime);
    }

    public void assertRecurringDateTimesFound(String dateTime) throws IllegalValueException {
        exception.expect(IllegalValueException.class);
        exception.expectMessage(String.format(DateTimeUtil.MESSAGE_RECURRING_DATE_TIME_FOUND, dateTime));
        DateTimeUtil.parseDateTimeString(dateTime);
    }

    @Test
    public void isSingleDateTimeString() {
        // Not dates
        assertFalse(DateTimeUtil.isSingleDateTimeString("Hello World"));
        assertFalse(DateTimeUtil.isSingleDateTimeString("Not a date"));
        assertFalse(DateTimeUtil.isSingleDateTimeString("We.d"));

        // multiple date groups separated by unknown tokens
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed ~ Thursday"));
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed ` Thursday"));
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed plus Thursday"));

        // recurring dates
        assertFalse(DateTimeUtil.isSingleDateTimeString("every Friday"));

        // multiple date alternatives
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed or Thur"));
        assertFalse(DateTimeUtil.isSingleDateTimeString("Wed and Thur"));


        // valid single dates
        assertTrue(DateTimeUtil.isSingleDateTimeString("Sat"));
    }

}
