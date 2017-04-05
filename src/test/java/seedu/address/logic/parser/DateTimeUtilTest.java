package seedu.address.logic.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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

    // TODO all these also
    @Test
    public void testingOnly() throws IllegalValueException {
        assertFuzzyNotEquals(
                ZonedDateTime.now().withMonth(4).withDayOfMonth(25).withHour(20).withMinute(0).withSecond(0)
                        .plusDays(2),
                DateTimeUtil.parseDateTimeString("2 days after 8pm 25 Apr"));

        assertFuzzyNotEquals(
                ZonedDateTime.now().withMonth(4).withDayOfMonth(25).withHour(20).withMinute(0).withSecond(0)
                        .plusHours(2),
                DateTimeUtil.parseDateTimeString("2 hours after 8pm 25 Apr"));

    }

    @Test
    public void parseEditedDateTimeString() throws IllegalValueException {
        ZonedDateTime previousDateTime = ZonedDateTime.now();
        ZoneId fixedRandomZoneId = ZoneId.of("Asia/Tokyo");
        ZonedDateTime fixedRandomDateTime = ZonedDateTime.of(2015, 4, 1, 3, 4, 5, 2, fixedRandomZoneId);

        // relative date
        assertFuzzyEquals(ZonedDateTime.now().plusDays(2),
                DateTimeUtil.parseEditedDateTimeString("2 days later", fixedRandomDateTime));
        // relative time
        assertFuzzyEquals(ZonedDateTime.now().plusHours(24),
                DateTimeUtil.parseEditedDateTimeString("24 hours later", fixedRandomDateTime));

        // relative date respective to another date
        assertFuzzyEquals(ZonedDateTime.now().withMonth(4).withDayOfMonth(25).plusDays(2),
                DateTimeUtil.parseEditedDateTimeString("2 days after 25 Apr", fixedRandomDateTime));

        // relative date respective to another date-time
        assertFuzzyEquals(
                ZonedDateTime.now().withMonth(4).withDayOfMonth(25).withHour(20).withMinute(0).withSecond(0)
                        .plusDays(2),
                DateTimeUtil.parseEditedDateTimeString("2 days after 25 Apr 8pm", fixedRandomDateTime));

        // relative date respective to another date-time with time-zone
        // PST and America/Los_Angeles is equivalent but Natty supports only certain time-zone suffixes
        // PST is an example of daylights saving time
        assertFuzzyEquals(
                ZonedDateTime.of(Year.now().getValue(), 4, 25, 20, 0, 0, 0, ZoneId.of("America/Los_Angeles"))
                        .plusDays(5),
                DateTimeUtil.parseEditedDateTimeString("5 days after 25 Apr 8pm PST", fixedRandomDateTime));

        // relative date respective to another date-time with time-zone offset
        assertFuzzyEquals(
                ZonedDateTime.of(Year.now().getValue(), 4, 25, 20, 0, 0, 0, ZoneId.of("+1000"))
                        .plusDays(3),
                DateTimeUtil.parseEditedDateTimeString("3 days after 25 Apr 8pm +1000", fixedRandomDateTime));


        // relative date respective to another date with time
        // note that this does not work because Natty is not working correctly
        assertFuzzyNotEquals(
                ZonedDateTime.now().withMonth(4).withDayOfMonth(25).withHour(20).withMinute(0).withSecond(0)
                        .plusHours(2),
                DateTimeUtil.parseEditedDateTimeString("2 hours after 25 Apr 8pm", fixedRandomDateTime));
        // TODO
        assertFuzzyNotEquals(
                ZonedDateTime.of(Year.now().getValue(), 4, 25, 20, 0, 0, 0, ZoneId.of("America/Los_Angeles"))
                        .plusDays(5),
                DateTimeUtil.parseEditedDateTimeString("5 days after 8pm PST 25 Apr", fixedRandomDateTime));
    }

    /**
     * Make a fuzzy match ignoring milliseconds
     */
    private void assertFuzzyEquals(ZonedDateTime expected, ZonedDateTime actual) {
        ChronoUnit truncationUnit = ChronoUnit.SECONDS;
        ZonedDateTime expectedTruncated = expected.truncatedTo(truncationUnit);
        ZonedDateTime actualTruncated = actual.truncatedTo(truncationUnit);
        System.out.println(expectedTruncated);
        System.out.println(actualTruncated);
        // converting to Instant so we compare without caring about timezones
        assertEquals(expectedTruncated.toInstant(), actualTruncated.toInstant());
    }

    private void assertFuzzyNotEquals(ZonedDateTime expected, ZonedDateTime actual) {
        // note that we are using isEqual instead of equals as we are just interested in the actual Instant
        // instead of also making sure for example the zones are equal
        ChronoUnit truncationUnit = ChronoUnit.SECONDS;
        ZonedDateTime expectedTruncated = expected.truncatedTo(truncationUnit);
        ZonedDateTime actualTruncated = actual.truncatedTo(truncationUnit);
        // converting to Instant so we compare without caring about timezones
        assertNotEquals(expectedTruncated.toInstant(), actualTruncated.toInstant());
    }
}
