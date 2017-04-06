package seedu.address.logic.parser;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.Name;
import seedu.address.model.task.StartEndDateTime;
import seedu.address.model.task.exceptions.InvalidDurationException;
import seedu.address.model.task.exceptions.PastDateTimeException;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes
 */
public class ParserUtil {

    private static final Pattern INDEX_ARGS_FORMAT = Pattern.compile("(?<targetIndex>.+)");

    //@@author A0140023E
    private static Parser dateTimeParser = new Parser(TimeZone.getDefault()); // TODO timezones
    // TODO decide if this is the right class
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
    // TODO create format for XmlAdaptedTask only
    // TODO create test format for LogicManagerTest

    //@@author
    /**
     * Returns the specified index in the {@code command} if it is a positive unsigned integer
     * Returns an {@code Optional.empty()} otherwise.
     */
    public static Optional<Integer> parseIndex(String command) {
        final Matcher matcher = INDEX_ARGS_FORMAT.matcher(command.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String index = matcher.group("targetIndex");
        if (!StringUtil.isUnsignedInteger(index)) {
            return Optional.empty();
        }
        return Optional.of(Integer.parseInt(index));

    }

    /**
     * Returns a new Set populated by all elements in the given list of strings
     * Returns an empty set if the given {@code Optional} is empty,
     * or if the list contained in the {@code Optional} is empty
     */
    public static Set<String> toSet(Optional<List<String>> list) {
        List<String> elements = list.orElse(Collections.emptyList());
        return new HashSet<>(elements);
    }

    /**
    * Splits a preamble string into ordered fields.
    * @return A list of size {@code numFields} where the ith element is the ith field value if specified in
    *         the input, {@code Optional.empty()} otherwise.
    */
    public static List<Optional<String>> splitPreamble(String preamble, int numFields) {
        return Arrays.stream(Arrays.copyOf(preamble.split("\\s+", numFields), numFields))
                .map(Optional::ofNullable)
                .collect(Collectors.toList());
    }

    /**
     * Parses a {@code Optional<String> name} into an {@code Optional<Name>} if {@code name} is present.
     */
    public static Optional<Name> parseName(Optional<String> name) throws IllegalValueException {
        assert name != null;
        return name.isPresent() ? Optional.of(new Name(name.get())) : Optional.empty();
    }

    /**
     * Parses {@code Collection<String> tags} into an {@code UniqueTagList}.
     */
    public static UniqueTagList parseTags(Collection<String> tags) throws IllegalValueException {
        assert tags != null;
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(new Tag(tagName));
        }
        return new UniqueTagList(tagSet);
    }

    //@@author A0140023E
    /**
     * Parses a {@code Optional<String>} into an {@code Optional<Deadline>} if {@code deadline} is present.
     */
    public static Optional<Deadline> parseNewDeadline(Optional<String> deadline)
            throws PastDateTimeException, IllegalValueException {

        assert deadline != null;

        return deadline.isPresent() ? Optional.of(new Deadline(parseDateTimeString(deadline.get()))) : Optional.empty();
    }

    /**
     * Parses a {@code Optional<String>} into an {@code Optional<Deadline>} relative to
     * {@code previousDeadline} if {@code rawDeadline} is present.
     */
    public static Optional<Deadline> parseEditedDeadline(Optional<String> rawDeadline, Deadline previousDeadline)
            throws PastDateTimeException, IllegalValueException {

        assert rawDeadline != null && previousDeadline != null;

        if (!rawDeadline.isPresent()) {
            return Optional.empty();
        }

        ZonedDateTime parsedDateTime = parseEditedDateTimeString(rawDeadline.get(), previousDeadline.getDateTime());
        return Optional.of(new Deadline(parsedDateTime));
    }

    /**
     * Parses parameters {@code Optional<String> startDateTime} and {@code Optional<String> endDateTime} into
     * an {@code Optional<StartEndDateTime>} if they are present.
     */
    public static Optional<StartEndDateTime> parseNewStartEndDateTime(Optional<String> startDateTime,
            Optional<String> endDateTime)
            throws PastDateTimeException, InvalidDurationException, IllegalValueException {

        assert startDateTime != null && endDateTime != null;

        if (!startDateTime.isPresent() || !endDateTime.isPresent()) {
            return Optional.empty();
        }

        StartEndDateTime startEndDateTime = new StartEndDateTime(parseDateTimeString(startDateTime.get()),
                                                                 parseDateTimeString(endDateTime.get()));

        return Optional.of(startEndDateTime);
    }

    /**
     * Parses parameters {@code Optional<String> startDateTime} and
     * {@code Optional<String> endDateTime} relative to {@code previousStartEndDateTime} into an
     * {@code Optional<StartEndDateTime>} if the parameters are present.
     */
    public static Optional<StartEndDateTime> parseEditedStartEndDateTime(Optional<String> startDateTime,
            Optional<String> endDateTime, StartEndDateTime previousStartEndDateTime)
            throws PastDateTimeException, InvalidDurationException, IllegalValueException {

        assert startDateTime != null && endDateTime != null;

        if (!startDateTime.isPresent() || !endDateTime.isPresent()) {
            return Optional.empty();
        }

        StartEndDateTime startEndDateTime = new StartEndDateTime(
                parseEditedDateTimeString(startDateTime.get(), previousStartEndDateTime.getStartDateTime()),
                parseEditedDateTimeString(endDateTime.get(), previousStartEndDateTime.getEndDateTime()));

        return Optional.of(startEndDateTime);
    }


    public static ZonedDateTime parseEditedDateTimeString(String dateTime, ZonedDateTime previousDate)
            throws IllegalValueException {
        // TODO extract commonalities from parseDateTimeString
        // TODO check if only one group and only one date from list (date alternatives)
        List<DateGroup> groups = dateTimeParser.parse(dateTime);
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();
            if (dates.size() > 0) {
                // 24 hours later what happens
                System.out.println("Date inferred: " + group.isDateInferred());
                System.out.println("Time inferred: " + group.isTimeInferred());
                System.out.println("Date type: " + group.getSyntaxTree().getChild(0).getChild(0));
                String dateType = group.getSyntaxTree().getChild(0).getChild(0).getText();
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
                    // if they are both inferred, they should be relative date or relative time
                    assert !(group.isDateInferred() && group.isTimeInferred());

                    String extractedDateTest;
                    if (group.isDateInferred()) {
                        extractedDateTest = new SimpleDateFormat("HH:mm:ss Z").format(dates.get(0));
                    }

                    if (group.isTimeInferred()) {
                        extractedDateTest = new SimpleDateFormat("yyyy-MM-dd").format(dates.get(0));
                    }

                    if (group.isDateInferred()) {
                        System.out.println("Date inferred");
                        // TODO do timezones properly
                        // note Natty does not support SGT so we use offset but this means timezone info
                        // such as daylight saving time adjustments are lost
                        String extractedDate = new SimpleDateFormat("HH:mm:ss Z").format(dates.get(0));
                        //System.out.println(new SimpleDateFormat("HH:mm:ss z").format(dates.get(0)));
                        //System.out.println(new SimpleDateFormat("HH:mm:ss Z").format(dates.get(0)));
                        //System.out.println(new SimpleDateFormat("HH:mm:ss zz").format(dates.get(0)));
                        List<DateGroup> groupsRedone =
                                dateTimeParser.parse(extractedDate, Date.from(previousDate.toInstant()));
                        Date newDate = groupsRedone.get(0).getDates().get(0);
                        return ZonedDateTime.ofInstant(newDate.toInstant(), ZoneId.systemDefault());
                    }
                    if (group.isTimeInferred()) {
                        System.out.println("Time inferred");
                        // TODO using SimpleDateFormat cos old date
                        String extractedDate = new SimpleDateFormat("yyyy-MM-dd").format(dates.get(0));
                        List<DateGroup> groupsRedone =
                                dateTimeParser.parse(extractedDate, Date.from(previousDate.toInstant()));
                        Date newDate = groupsRedone.get(0).getDates().get(0);
                        return ZonedDateTime.ofInstant(newDate.toInstant(), ZoneId.systemDefault());
                    }
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

        List<Date> dateAlternatives = dateGroups.get(0).getDates();

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

    public static boolean isSingleDateTimeStringOld(String dateTime) {
        List<DateGroup> groups = dateTimeParser.parse(dateTime);
        for (DateGroup group : groups) {
            List<Date> dates = group.getDates();
            if (dates.size() > 0) {
                // TODO note that the size should be == 1 because it shouldn't be more than one date
                return true;
            }
        }
        return false;
    }
}
