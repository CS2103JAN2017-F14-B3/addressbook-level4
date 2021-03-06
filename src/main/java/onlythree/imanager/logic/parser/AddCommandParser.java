package onlythree.imanager.logic.parser;

import static onlythree.imanager.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static onlythree.imanager.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.NoSuchElementException;

import onlythree.imanager.commons.exceptions.IllegalValueException;
import onlythree.imanager.logic.commands.AddCommand;
import onlythree.imanager.logic.commands.Command;
import onlythree.imanager.logic.commands.IncorrectCommand;
import onlythree.imanager.model.task.exceptions.InvalidDurationException;
import onlythree.imanager.model.task.exceptions.PastDateTimeException;


//@@author A0140023E
/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     */
    public Command parse(String args) {
        DateTimeExtractor dateTimeExtractor;
        try {
            dateTimeExtractor = extractDateTimes(args);
        } catch (PastDateTimeException | InvalidDurationException e) {
            return new IncorrectCommand(e.getMessage());
        }

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
     * Extracts date-times from the arguments if they exist and returns a {@link DateTimeExtractor}
     * with the processed date-times if they exist.
     *
     * @param args the arguments to extract date/time from
     * @throws PastDateTimeException if any of the extracted date-times are in the past
     * @throws InvalidDurationException if a start and end date-time is found and the end date-time
     *         is before or same as the start date-time
     */
    private DateTimeExtractor extractDateTimes(String args)
            throws PastDateTimeException, InvalidDurationException {
        DateTimeExtractor dateTimeExtractor = new DateTimeExtractor(args);
        // process StartEndDateTime first because it is more likely to fail due to more constraints
        dateTimeExtractor.processStartEndDateTime();
        // constraints for deadline are looser so it is less likely to fail
        dateTimeExtractor.processDeadline();

        return dateTimeExtractor;
    }

}
