package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.NoSuchElementException;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.IncorrectCommand;
import seedu.address.model.task.exceptions.InvalidDurationException;
import seedu.address.model.task.exceptions.PastDateTimeException;


//@@author A0140023E
/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser {
    //private DateTimeExtractor dateTimeExtractor;

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     */
    public Command parse(String args) {
        DateTimeExtractor dateTimeExtractor;
        try {
            dateTimeExtractor = extractDates(args);
        } catch (PastDateTimeException e) {
            return new IncorrectCommand(e.getMessage());
        } catch (InvalidDurationException e) {
            return new IncorrectCommand(e.getMessage());
        }

        // TODO ArgumentTokenizer became very irrelevant in this class but is it still relevant for other classes?
        ArgumentTokenizer argsTokenizer = new ArgumentTokenizer(PREFIX_TAG);
        argsTokenizer.tokenize(dateTimeExtractor.getProcessedArgs());
        try {
            String nameArgs = argsTokenizer.getPreamble().get();

            return new AddCommand(nameArgs, dateTimeExtractor.getProcessedDeadline(),
                    dateTimeExtractor.getProcessedStartEndDateTime(),
                    ParserUtil.toSet(argsTokenizer.getAllValues(PREFIX_TAG)));
        } catch (NoSuchElementException nsee) {
            return new IncorrectCommand(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        } catch (IllegalValueException ive) {
            return new IncorrectCommand(ive.getMessage());
        }
    }

    /**
     * Extracts date/time from the arguments if they exist and returns a {@link DateTimeExtractor} with the
     * processed dates if they exist.
     *
     * @param args the arguments to extract date/time from
     * @throws PastDateTimeException TODO
     * @throws InvalidDurationException TODO
     */
    private DateTimeExtractor extractDates(String args)
            throws PastDateTimeException, InvalidDurationException {
        DateTimeExtractor dateTimeExtractor = new DateTimeExtractor(args);
        // process StartEndDateTime first because it is more constrained
        dateTimeExtractor.processStartEndDateTime();
        dateTimeExtractor.processDeadline();

        return dateTimeExtractor;
    }

}
