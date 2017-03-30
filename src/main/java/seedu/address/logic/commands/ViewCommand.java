package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.Arrays;

//@@author A0135998H
/**
 * View all tasks in the view list to the user.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    public static final String TYPE_ALL = "";
    public static final String TYPE_DONE = "d";
    public static final String TYPE_FLOATING = "f";
    public static final String TYPE_OVERDUE = "o";
    public static final String TYPE_PENDING = "p";
    public static final String TYPE_TODAY = "t";

    private static ArrayList<String> validCommands = new ArrayList<String>(Arrays.asList(
            TYPE_ALL, TYPE_DONE, TYPE_FLOATING, TYPE_OVERDUE, TYPE_PENDING, TYPE_TODAY));

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": View a type of specified tasks.\n"
            + "Parameters: TYPE\n"
            + "Example: " + COMMAND_WORD + TYPE_DONE;


    public static final String MESSAGE_SUCCESS = "Viewed all tasks";

    private final String listType;

    public ViewCommand(String listType) {
        this.listType = listType;
    }

    public static boolean isValidCommand(String command) {
        return validCommands.contains(command);
    }

    @Override
    public CommandResult execute() {
        switch(listType) {
        case TYPE_OVERDUE:
            model.updateFilteredListToShowOverdue();
            break;

        default:
            model.updateFilteredListToShowAll();
        }
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
