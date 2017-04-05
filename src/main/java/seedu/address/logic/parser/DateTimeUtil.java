package seedu.address.logic.parser;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.antlr.runtime.tree.Tree;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import seedu.address.commons.exceptions.IllegalValueException;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes TODO
 */
public class DateTimeUtil {

    private static final Pattern INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    //@@author A0140023E
    private static Parser dateTimeParser = new Parser(TimeZone.getDefault()); // use the system default timezone
    // TODO decide if this is the right class
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    // TODO create format for XmlAdaptedTask only
    // TODO create test format for LogicManagerTest
    public static final ZoneId TIME_ZONE = ZoneId.systemDefault();

    private static final String NATTY_TOKEN_DATE_TIME_ALTERNATIVE = "DATE_TIME_ALTERNATIVE";
    private static final String NATTY_TOKEN_DATE_TIME = "DATE_TIME";
    private static final String NATTY_TOKEN_RELATIVE_DATE = "RELATIVE_DATE";
    private static final String NATTY_TOKEN_RELATIVE_TIME = "RELATIVE_TIME";

    static {
        initializeNatty();
    }

    private static void initializeNatty() {
        // TODO Auto-generated method stub

    }

    public static ZonedDateTime parseEditedDateTimeString(String dateTime, ZonedDateTime previousDate)
            throws IllegalValueException {
        // TODO extract commonalities from parseDateTimeString
        // TODO check if only one group and only one date from list (date alternatives)
        List<DateGroup> groups = dateTimeParser.parse(dateTime);
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();
            if (dates.size() > 0) {
                logDateGroupTest(group);
                String dateType = getDateTimeType(group.getSyntaxTree());
                if (dateType.equals("RELATIVE_DATE") || dateType.equals("RELATIVE_TIME")) {
                    // such as 24 hours later
                    // such as 2 days later
                    // Relative dates should always be relative to current date not other dates
                    // special cases such as 2 days after 25 Apr also works
                    // but cases such as 2 hours after 25 Apr 8pm does not work
                    // Neither does cases such as 2 hours after 25 Apr 8pm work
                    System.out.println("Relative do nothing!");
                } else {
                    // this means there is a bug somewhere as the date and time cannot be both inferred
                    assert !(group.isDateInferred() && group.isTimeInferred());

                    String extractedDateTest = null;
                    // TODO using SimpleDateFormat cos old date
                    if (group.isDateInferred()) {
                        System.out.println("Date inferred");
                        // TODO do timezones properly
                        // note Natty does not support SGT so we use offset but this means timezone info
                        // such as daylight saving time adjustments are lost
                        extractedDateTest = new SimpleDateFormat("HH:mm:ss Z").format(dates.get(0));
                    }

                    if (group.isTimeInferred()) {
                        System.out.println("Time inferred");
                        extractedDateTest = new SimpleDateFormat("yyyy-MM-dd").format(dates.get(0));
                    }
                    List<DateGroup> groupsRedone =
                            dateTimeParser.parse(extractedDateTest, Date.from(previousDate.toInstant()));
                    Date newDate = groupsRedone.get(0).getDates().get(0);
                    return ZonedDateTime.ofInstant(newDate.toInstant(), DateTimeUtil.TIME_ZONE);
                }
                // TODO comment Avoid old Date class where possible format
                Instant instant = dates.get(0).toInstant();
                ZoneId zoneId = ZoneId.systemDefault();

                // TODO use a ZonedDateTime so user can see time in his timezone, perhaps
                // Instant can be used where possible and only when reading input and output from user
                // we use ZonedDateTime
                ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
                return zonedDateTime;
            }
        }
        throw new IllegalValueException(dateTime + " is not a valid date/time.");
    }


    public static ZonedDateTime parseEditedDateTimeStringTest(String dateTime, ZonedDateTime previousDateTime)
            throws IllegalValueException {

        DateGroup dateGroup = parseDateTimeHelper(dateTime);
        List<Date> dates = dateGroup.getDates();
        // 24 hours later what happens
        String dateTimeType = getDateTimeType(dateGroup.getSyntaxTree());
        logDateGroupTest(dateGroup);

        if (dateTimeType.equals(NATTY_TOKEN_RELATIVE_DATE) || dateTimeType.equals(NATTY_TOKEN_RELATIVE_TIME)) {
            // such as 24 hours later
            // such as 2 days later
            // Relative dates should always be relative to current date not other dates
            // special cases such as 2 days after 25 Apr also works
            // but cases such as 2 hours after 25 Apr 8pm does not work
            // Neither does cases such as 2 hours after 25 Apr 8pm work
            System.out.println("Relative do nothing!");
        } else {
            return parseDateTimeUsingPrevious(previousDateTime, dateGroup, dates);
        }
        // Convert the old java.util.Date class to the much better new classes in java.time package
        Instant instant = dates.get(0).toInstant();
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, DateTimeUtil.TIME_ZONE);
        return zonedDateTime;
        //throw new IllegalValueException(dateTime + " is not a valid date/time.");
    }

    private static void logDateGroupTest(DateGroup dateGroup) {
        System.out.println("Date inferred: " + dateGroup.isDateInferred());
        System.out.println("Time inferred: " + dateGroup.isTimeInferred());
        System.out.println("Date type: " + dateGroup.getSyntaxTree().getChild(0).getChild(0));
    }

    /**
     * Returns the type of the date-time, e.g. whether it is an explicit date, relative date, or relative time.
     */
    private static String getDateTimeType(Tree dateTimeAlternativeRoot) {
        assert dateTimeAlternativeRoot != null;

        assert dateTimeAlternativeRoot.getText().equals(NATTY_TOKEN_DATE_TIME_ALTERNATIVE)
        && dateTimeAlternativeRoot.getChildCount() == 1;

        Tree dateTimeSubtree = dateTimeAlternativeRoot.getChild(0);
        assert dateTimeSubtree.getText().equals(NATTY_TOKEN_DATE_TIME)
        && dateTimeSubtree.getChildCount() == 1;

        Tree dateTimeTypeSubtree = dateTimeSubtree.getChild(0);
        return dateTimeTypeSubtree.getText();
    }

    private static ZonedDateTime parseDateTimeUsingPrevious(ZonedDateTime previousDateTime, DateGroup dateGroup,
            List<Date> dates) throws IllegalValueException {
        String extractedDateTime = extractSpecifiedDateTime(dateGroup, dates);
        DateGroup newDateGroup = parseDateTimeUsingPreviousHelper(extractedDateTime, previousDateTime);

        // the date group returned should contain one and only one date.
        assert newDateGroup.getDates() != null && newDateGroup.getDates().size() == 1;

        Date newDate = newDateGroup.getDates().get(0);
        return ZonedDateTime.ofInstant(newDate.toInstant(), DateTimeUtil.TIME_ZONE);
    }


    /**
     * Extracts the specified date-time and format it as a string. e.g, extracting only the date component from
     * the date-time if only date is specified from the date.
     */
    private static String extractSpecifiedDateTime(DateGroup dateGroup, List<Date> dates) {
        // this means there is a bug somewhere as the date and time cannot be both inferred
        assert !(dateGroup.isDateInferred() && dateGroup.isTimeInferred());

        // TODO using SimpleDateFormat cos old date
        if (dateGroup.isDateInferred()) {
            System.out.println("Date inferred");
            // TODO do timezones properly
            // note Natty does not support SGT so we use offset but this means timezone info
            // such as daylight saving time adjustments are lost
            return new SimpleDateFormat("HH:mm:ss Z").format(dates.get(0));
        }

        if (dateGroup.isTimeInferred()) {
            System.out.println("Time inferred");
            return new SimpleDateFormat("yyyy-MM-dd").format(dates.get(0));
        }
        // There is no date extraction if neither date or time is inferred, thus return a null
        return null;
    }
    private static DateGroup parseDateTimeUsingPreviousHelper(String dateTime,
            ZonedDateTime previousDateTime) throws IllegalValueException {
        // Convert back to old java.util.Date class for use in Natty
        Date previousDateTimeAsOldDateClass = Date.from(previousDateTime.toInstant());
        List<DateGroup> dateGroups = dateTimeParser.parse(dateTime, previousDateTimeAsOldDateClass);
        // TODO check if only one group and only one date from list (date alternatives)
        if (dateGroups.size() == 0) {
            throw new IllegalValueException(dateTime + " is not a valid date/time.");
        }

        if (dateGroups.size() > 1) {
            throw new IllegalValueException(
                    "Multiple dates found when expecting only one date from " + dateTime);
        }

        List<Date> datesTest = dateGroups.get(0).getDates();

        // if there is at least one date group, there should be at least one date. This probably
        // means there is a bug in Natty
        assert datesTest.size() != 0;

        if (datesTest.size() > 1) {
            throw new IllegalValueException("Date-time alternatives found, please only enter one date" + dateTime);
        }

        // returns the date group that represents information about the date
        return dateGroups.get(0);
    }

    /**
     * Returns a DateGroup representing the date-time parsed along with relevant info. TODO
     */
    private static DateGroup parseDateTimeHelper(String dateTime) throws IllegalValueException {
        List<DateGroup> dateGroups = dateTimeParser.parse(dateTime);
        // TODO check if only one group and only one date from list (date alternatives)
        if (dateGroups.size() == 0) {
            throw new IllegalValueException(dateTime + " is not a valid date/time.");
        }

        if (dateGroups.size() > 1) {
            throw new IllegalValueException(
                    "Multiple dates found when expecting only one date from " + dateTime);
        }

        List<Date> datesTest = dateGroups.get(0).getDates();

        // if there is at least one date group, there should be at least one date. This probably
        // means there is a bug in Natty
        assert datesTest.size() != 0;

        if (datesTest.size() > 1) {
            throw new IllegalValueException("Date-time alternatives found, please only enter one date" + dateTime);
        }

        // returns the date group that represents information about the date
        return dateGroups.get(0);
        // Convert the old java.util.Date class to the much better new classes in java.time package
        //Instant instant = datesTest.get(0).toInstant();
        //ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ParserUtil.TIME_ZONE);
        //return zonedDateTime;
    }

    // TODO notice the inconsistencies of the parsing class, maybe need to change this
    /**
     * Parses Date strings into a {@code ZonedDateTime}.
     */
    public static ZonedDateTime parseDateTimeString(String dateTime) throws IllegalValueException {
        List<DateGroup> groups = dateTimeParser.parse(dateTime);
        // TODO check if only one group and only one date from list (date alternatives)
        if (groups.size() == 0) {
            throw new IllegalValueException(dateTime + " is not a valid date/time.");
        }

        if (groups.size() > 1) {
            throw new IllegalValueException(
                    "Multiple dates found when expecting only one date from " + dateTime);
        }

        List<Date> datesTest = groups.get(0).getDates();

        // if there is at least one date group, there should be at least one date. This probably
        // means there is a bug in Natty
        assert datesTest.size() != 0;

        if (datesTest.size() > 1) {
            throw new IllegalValueException("Date-time alternatives found, please only enter one date" + dateTime);
        }

        //for (DateGroup group : groups) {
        //    List<Date> dates = group.getDates();
        //    if (dates.size() > 0) {
                // TODO comment Avoid old Date class where possible format
        Instant instant = datesTest.get(0).toInstant();
        ZoneId zoneId = ZoneId.systemDefault();

                // TODO use a ZonedDateTime so user can see time in his timezone, perhaps
                // Instant can be used where possible and only when reading input and output from user
                // we use ZonedDateTime
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        return zonedDateTime;
        //    }
        //}
        // TODO not expected to reach here
        //throw new IllegalValueException(dateTime + " is not a valid date/time.");
    }

    // DateTimeUtil
    // TODO maybe doesn't belong in ParserUtil
    /**
     * Returns true if a String contains only a single date-time string parseable by Natty, otherwise returns false.
     */
    public static boolean isSingleDateTimeString(String dateTime) {
        List<DateGroup> dateGroups = dateTimeParser.parse(dateTime);
        // Example: "Wed ~ Thur" will result in 2 date groups
        if (dateGroups.size() == 0 || dateGroups.size() > 1) {
            return false;
        }

        assert dateGroups.size() == 1; // to check for logical error

        DateGroup dateGroup = dateGroups.get(0);
        // rejects recurring dates // TODO
        if (dateGroup.isRecurring()) {
            return false;
        }

        List<Date> dateAlternatives = dateGroup.getDates();

        // if there is at least one date group, there should always be at least one date.
        // Therefore, if the assertion fail there might be a bug in Natty.
        assert dateAlternatives.size() != 0;

        // Example: "Wed or Thur" will result in 2 date alternatives
        if (dateAlternatives.size() > 1) {
            return false;
        } else {
            assert dateAlternatives.size() == 1; // to check for logical error
            return true;
        }
    }
}
