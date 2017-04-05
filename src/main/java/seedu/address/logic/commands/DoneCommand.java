package seedu.address.logic.commands;

import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.task.ReadOnlyTask;
import seedu.address.model.task.Task;

//@@author A0135998H
/**
 * Mark an existing task in the task list as "done".
 */
public class DoneCommand extends Command {

    public static final String COMMAND_WORD = "done";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Marks an existing task "
            + "by the index number used in the last task listing.\n"
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_MARK_TASK_SUCCESS = "Marked Task: %1$s";

    private final int filteredTaskListIndex;

    /**
     * @param filteredTaskListIndex the index of the task in the filtered task list to edit
     */
    public DoneCommand(int filteredTaskListIndex) {
        assert filteredTaskListIndex > 0;

        // converts filteredTaskListIndex from one-based to zero-based.
        this.filteredTaskListIndex = filteredTaskListIndex - 1;
    }

    //@@author A0140023E
    @Override
    public CommandResult execute() throws CommandException {
        List<ReadOnlyTask> lastShownList = model.getFilteredTaskList();

        if (filteredTaskListIndex >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }
        ReadOnlyTask taskToEdit = lastShownList.get(filteredTaskListIndex);

        Task editedTask = new Task(taskToEdit.getName(),
                taskToEdit.getDeadline(),
                taskToEdit.getStartEndDateTime(),
                taskToEdit.getTags());

        editedTask.markDone();

        //TODO: switch to done category tab instead
        model.updateFilteredListToShowAll();
        return new CommandResult(String.format(MESSAGE_MARK_TASK_SUCCESS, taskToEdit));
    }

}
