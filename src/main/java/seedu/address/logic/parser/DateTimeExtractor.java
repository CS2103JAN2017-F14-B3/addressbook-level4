package seedu.address.logic.parser;

import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.StartEndDateTime;
import seedu.address.model.task.exceptions.InvalidDurationException;
import seedu.address.model.task.exceptions.PastDateTimeException;

//@@author A0140023E
/**
 * Specialized class that extracts Deadlines and StartEndDateTime if they exist.
 */
public class DateTimeExtractor {
    private final Logger logger = LogsCenter.getLogger(DateTimeExtractor.class);

    /**
     * Regex that matches the preamble before any argument.
     * Matches everything greedily until the next regex so any number of from and to can be included.
     * e.g. preamblePattern matches "from by to" from the String "from by to by tmr" in a Regex expression
     * containing PREAMBLE_REGEX + ARG_NAME_FROM_REGEX
     */
    private static final String PREAMBLE_REGEX = ".*";

    /**
     * Regex that matches the argument name [from].
     * Matches a single whitespace character (i.e. \t\n\x0B\f\r) in between a "from".
     * This prevents matching words such as therefrom and fromage.
     */
    private static final String ARG_NAME_FROM_REGEX = "(?<fromArg>\\sfrom\\s)";

    /**
     * Regex that matches the argument value startDateTime.
     * Matches everything greedily.
     */
    private static final String ARG_VALUE_FROM_GREEDY_REGEX = "(?<startDateTime>.+)";

    /**
     * Regex that matches the argument value startDateTime.
     * Matches everything lazily.
     */
    private static final String ARG_VALUE_FROM_LAZY_REGEX = "(?<startDateTime>.+?)";

    /**
     * Regex that matches the argument name [to].
     * Matches a single whitespace character (i.e. \t\n\x0B\f\r) in between a "to".
     * This prevents matching words such as auto and tomorrow.
     */
    private static final String ARG_NAME_TO_REGEX =  "(?<toArg>\\sto\\s)";

    /**
     * Regex that matches the argument value endDateTime.
     * Matches everything lazily.
     */
    private static final String ARG_VALUE_TO_LAZY_REGEX = "(?<endDateTime>.+?)";

    /**
     * Regex that matches the argument name [by].
     * Matches a single whitespace character (i.e. \t\n\x0B\f\r) in between a "by".
     * This prevents matching words such as baby and bypass.
     */
    private static final String ARG_NAME_BY_REGEX = "(?<byArg>\\sby\\s)";
    /**
     * Regex that matches the argument value deadline.
     * Matches everything lazily.
     */
    private static final String ARG_VALUE_BY_LAZY_REGEX = "(?<deadline>.+?)";

    // match a white space character and a tag with zero or more times
    private static final String ARG_ALL_TAGS_REGEX = "(?<tagArguments>(\\st/[^/]+)*)";
    // TODO consider changing the following format
    // match a white space character with zero or one tag;
    // "(?<tagArguments>(\\stags\\s[^/]+)?)");

    /**
     * Pattern that checks if a string contains a start and end date/time.
     * Can be used for adding or editing tasks with a start and end date/time.
     */
    public static final Pattern HAS_START_END_DATETIME_FORMAT = Pattern.compile(
            PREAMBLE_REGEX
            + ARG_NAME_FROM_REGEX
            + ARG_VALUE_FROM_GREEDY_REGEX
            + ARG_NAME_TO_REGEX
            + ARG_VALUE_TO_LAZY_REGEX // lazy match as next expression is optional
            + ARG_ALL_TAGS_REGEX);

    /**
     * Pattern that checks if a string contains a deadline date/time.
     * Can be used for adding or editing tasks with a deadline.
     */
    public static final Pattern HAS_DEADLINE_FORMAT = Pattern.compile(
            PREAMBLE_REGEX
            + ARG_NAME_BY_REGEX
            + ARG_VALUE_BY_LAZY_REGEX // lazy match as next expression is optional
            + ARG_ALL_TAGS_REGEX);

    /**
     * Pattern that checks if a string contains a start date/time.
     * Can be used for editing tasks that already has a start and end date/time.
     */
    public static final Pattern HAS_START_DATETIME_FORMAT = Pattern.compile(
            PREAMBLE_REGEX
            + ARG_NAME_FROM_REGEX
            + ARG_VALUE_FROM_LAZY_REGEX // lazy match as next expression is optional
            + ARG_ALL_TAGS_REGEX);

    /**
     * Pattern that checks if a string contains a end date/time.
     * Can be used for editing tasks that already has a start and end date/time.
     */
    public static final Pattern HAS_END_DATETIME_FORMAT = Pattern.compile(
            PREAMBLE_REGEX
            + ARG_NAME_TO_REGEX
            + ARG_VALUE_TO_LAZY_REGEX // lazy match as next expression is optional
            + ARG_ALL_TAGS_REGEX);

    /**
     * Contains the argument string after processing.
     */
    private String processedArgs;

    /**
     * Contains the Deadline after processing. Empty if does not exist.
     */
    private Optional<Deadline> processedDeadline;
    /**
     * Contains the raw Deadline string after processing. Empty if does not exist.
     */
    private Optional<String> processedRawDeadline;

    /**
     * Contains the startEndDateTime after processing. Empty if does not exist.
     */
    private Optional<StartEndDateTime> processedStartEndDateTime;
    /**
     * Contains the raw startDateTime string after processing. Empty if does not exist.
     */
    private Optional<String> processedRawStartDateTime;
    /**
     * Contains the raw endDateTime string after processing. Empty if does not exist.
     */
    private Optional<String> processedRawEndDateTime;

    public DateTimeExtractor(String args) {
        processedArgs = args;
    }

    public void processDeadline() throws PastDateTimeException {
        processedDeadline = Optional.empty();

        try {
            processRawDeadline();
        } catch (IllegalValueException e) {
            // TODO
            // This means what comes after by is not a date. Thus we stop here as no Deadline is found.
            // e.g. add Download song stand by me
            return;
        }

        if (processedRawDeadline.isPresent()) {
            try {
                // Note that if performance is a concern, we should process the Deadline directly instead
                // of calling processRawDeadline so to avoid parsing dates twice
                // i.e. once in processRawDeadline and once in processDeadline
                processedDeadline =
                        Optional.of(new Deadline(ParserUtil.parseDateTimeString(processedRawDeadline.get())));
            } catch (IllegalValueException e) {
                // TODO message
                logger.severe("TODO processRawDeadline() should have ensured that it is a valid date.");
            }
        }
    }

    public void processRawDeadline() throws IllegalValueException {
        // already processed, do not re-process
        if (processedRawDeadline != null) {
            logger.warning("Trying to re-process a processed deadline");
            return;
        }
        processedRawDeadline = Optional.empty();

        Matcher matcher = HAS_DEADLINE_FORMAT.matcher(processedArgs);

        if (!matcher.matches()) {
            logger.info("----------------[PROCESS RAW DEADLINE][No deadline found]");
            return;
        }

        final String matchedRawDeadline = matcher.group("deadline");
        if (!ParserUtil.isDateTimeString(matchedRawDeadline)) {
            return;
        }

        // Note that we still do not know the exact date/time of the deadline so it can be a date in the past.
        processedRawDeadline = Optional.of(matchedRawDeadline);

        // Date is valid so we can extract the arguments out. However, if it is a past date,
        // the processed argument becomes invalid but a PastDateTimeException will be thrown anyway, thus
        // not allowing the command to continue.
        processedArgs = extractArguments(matcher.start("byArg"), matcher.end("deadline"));
    }

    public void processStartEndDateTime()
            throws PastDateTimeException, InvalidDurationException {
        processedStartEndDateTime = Optional.empty();

        try {
            processRawStartEndDateTime();
        } catch (IllegalValueException e) {
            // TODO
            // This means that information between from and to are not dates. Thus we stop here
            // as no StartEndDateTime is found.
            // e.g. add Travel from Singapore to Malaysia
            return;
        }
        if (processedRawStartDateTime.isPresent() && processedRawEndDateTime.isPresent()) {
            try {
                // Note that if performance is a concern, we should process the startEndDateTime directly instead
                // of calling processRawStartEndDateTime() so to avoid parsing dates twice
                // i.e. once in processRawStartEndDateTime and once in processStartEndDateTime
                processedStartEndDateTime =
                        Optional.of(new StartEndDateTime(
                                ParserUtil.parseDateTimeString(processedRawStartDateTime.get()),
                                ParserUtil.parseDateTimeString(processedRawEndDateTime.get())));
            } catch (IllegalValueException e) {
                // TODO message
                logger.severe("TODO processStartEndDateTime() should have ensured that it is a valid date.");
            }
        }
    }

    public void processRawStartEndDateTime() throws IllegalValueException {
        // already processed, do not re-process
        if (processedRawStartDateTime != null && processedRawEndDateTime != null) {
            logger.warning("Trying to re-process a processed StartEndDateTime");
            return;
        }
        processedRawStartDateTime = Optional.empty();
        processedRawEndDateTime = Optional.empty();

        Matcher matcher = HAS_START_END_DATETIME_FORMAT.matcher(processedArgs);

        if (!matcher.matches()) {
            logger.info("----------------[PROCESS RAWSTARTENDDATETIME][No Start and End Date Time found]");
            return;
        }

        final String matchedStartDateTime = matcher.group("startDateTime");
        final String matchedEndDateTime = matcher.group("endDateTime");

        logger.info("----------------[PROCESS RAWSTARTENDDATETIME][Start:"
                + matchedStartDateTime + "]");
        logger.info("----------------[PROCESS RAWSTARTENDDATETIME][End: "
                + matchedEndDateTime + "]");

        if (!ParserUtil.isDateTimeString(matchedStartDateTime)
                || !ParserUtil.isDateTimeString(matchedEndDateTime)) {
            return;
        }

        // Note that we still do not know the exact date/time of the dates so they can be dates in the past.
        // We also do not know if the end date will be after the start date.
        processedRawStartDateTime = Optional.of(matchedStartDateTime);
        processedRawEndDateTime = Optional.of(matchedEndDateTime);

        // Dates are valid so we can extract the arguments out. However, if there is any past date,
        // the processed argument becomes invalid but a PastDateTimeException will be thrown anyway, thus
        // not allowing the command to continue. Similarly, a InvalidDurationException will be thrown if the
        // End Date comes after the Start Date.
        processedArgs = extractArguments(matcher.start("fromArg"), matcher.end("endDateTime"));
    }

    public void processRawStartDateTime() throws IllegalValueException {
        // already processed, do not re-process
        //if (rawStartDateTime != null) {
        //    return;
            // re-process because if we call others first gg?
        //}
        // note the above will make edit 4 from Friday to Saturday fail
        if (processedRawStartDateTime == null) {
            processedRawStartDateTime = Optional.empty();
        }

        //rawStartDateTime = Optional.empty();

        Matcher matcher = HAS_START_DATETIME_FORMAT.matcher(processedArgs);

        if (!matcher.matches()) {
            logger.info("----------------[PROCESS RAWSTARTDATETIME][No Start Date/Time found]");
            return;
        }

        final String matchedStartDateTime = matcher.group("startDateTime");

        logger.info("----------------[PROCESS RAWSTARTDATETIME][Start:"
                + matchedStartDateTime + "]");

        if (!ParserUtil.isDateTimeString(matchedStartDateTime)) {
            return;
        }

        // Note that we still do not know the exact date/time of the date so it can be a date in the past.
        // We also do not know if the end date will be after the start date.
        processedRawStartDateTime = Optional.of(matchedStartDateTime);

        // Date is valid so we can extract the arguments out. However, if there is any past date,
        // the processed argument becomes invalid but a PastDateTimeException will be thrown anyway, thus
        // not allowing the command to continue. Similarly, an InvalidDurationException will be thrown if the
        // End Date comes after the Start Date.
        processedArgs = extractArguments(matcher.start("fromArg"), matcher.end("startDateTime"));
    }


    public void processRawEndDateTime() throws IllegalValueException {
        // TODO only initialize if haven't initialized
        if (processedRawEndDateTime == null) {
            processedRawEndDateTime = Optional.empty();
        }

        Matcher matcher = HAS_END_DATETIME_FORMAT.matcher(processedArgs);

        if (!matcher.matches()) {
            logger.info("----------------[PROCESS RAWENDDATETIME][No End Date/Time found]");
            return;
        }

        final String matchedEndDateTime = matcher.group("endDateTime");

        logger.info("----------------[PROCESS RAWENDDATETIME][End:"
                + matchedEndDateTime + "]");

        if (!ParserUtil.isDateTimeString(matchedEndDateTime)) {
            return;
        }

        // Note that we still do not know the exact date/time of the date so it can be a date in the past.
        // We also do not know if the end date will be after the start date.
        processedRawEndDateTime = Optional.of(matchedEndDateTime);

        // Date is valid so we can extract the arguments out. However, if there is any past date,
        // the processed argument becomes invalid but a PastDateTimeException will be thrown anyway, thus
        // not allowing the command to continue. Similarly, an InvalidDurationException will be thrown if the
        // End Date comes after the Start Date.
        processedArgs = extractArguments(matcher.start("toArg"), matcher.end("endDateTime"));
    }

    /**
     * Returns the processed arguments that have the arguments specified by
     * {@code startIndex} to {@code endIndex} extracted out.
     */
    private String extractArguments(int startIndex, int endIndex) {
        return StringUtil.replace(processedArgs, startIndex, endIndex, "");
    }

    /**
     * Returns the arguments after processing
     */
    public String getProcessedArgs() {
        return processedArgs;
    }

    /**
     * Returns the processed deadline if it exists, otherwise returns empty. Returns null if not processed.
     */
    public Optional<Deadline> getProcessedDeadline() {
        return processedDeadline;
    }

    /**
     * Returns the processed raw deadline if it exists, otherwise returns empty. Returns null if not processed.
     */
    public Optional<String> getProcessedRawDeadline() {
        return processedRawDeadline;
    }

    /**
     * Returns the processed startEndDateTime if it exists, otherwise returns empty. Returns null if not processed.
     */
    public Optional<StartEndDateTime> getProcessedStartEndDateTime() {
        return processedStartEndDateTime;
    }

    /**
     * Returns the processed raw startDateTime if it exists, otherwise returns empty. Returns null if not processed.
     */
    public Optional<String> getProcessedRawStartDateTime() {
        return processedRawStartDateTime;
    }

    /**
     * Returns the processed raw endDateTime if it exists, otherwise returns empty. Returns null if not processed.
     */
    public Optional<String> getProcessedRawEndDateTime() {
        return processedRawEndDateTime;
    }
}
