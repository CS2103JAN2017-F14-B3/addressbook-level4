package seedu.address.logic.parser;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.tag.UniqueTagList;
import seedu.address.model.task.Deadline;
import seedu.address.model.task.Name;
import seedu.address.model.task.ReadOnlyTask;
import seedu.address.model.task.Task;
import seedu.address.model.task.UniqueTaskList;
import seedu.address.model.task.exceptions.PastDateTimeException;

//@@author A0140023E
public class AddCommandParserTest {
        // add stand by tmr by me
    // TODO make logicmanagertest test some simple add and edit

    private static Task actualTask;

    private class ModelManagerMock extends ModelManager {
        @Override
        public synchronized void addTask(Task task) throws UniqueTaskList.DuplicateTaskException {
            AddCommandParserTest.actualTask = task;
            super.addTask(task);
        }

        // TODO move this to EditCommandParserTest
        @Override
        public void updateTask(int filteredTaskListIndex, ReadOnlyTask editedTask)
                throws UniqueTaskList.DuplicateTaskException {
            AddCommandParserTest.actualTask = new Task(editedTask);
            super.updateTask(filteredTaskListIndex, editedTask);
        }
    }


    //TODO
    @Test
    public void testAdd() throws PastDateTimeException, IllegalValueException, CommandException {
        Name name = new Name("stand by me");
        Deadline deadline = new Deadline(ZonedDateTime.now().plusDays(1).truncatedTo(ChronoUnit.MINUTES));
        Task expectedTask = new Task(name, Optional.of(deadline), Optional.empty(), new UniqueTagList());

        Command command = new AddCommandParser().parse("stand by me by tmr");
        command.setData(new ModelManagerMock());
        CommandResult result = command.execute();
        actualTask.setDeadline(new Deadline(
                actualTask.getDeadline().get().getDateTime().truncatedTo(ChronoUnit.MINUTES)));
        assertEquals(expectedTask, actualTask);
        System.out.println(actualTask);
        System.out.println(result.feedbackToUser);
    }

    @Test
    public void testEdit() throws PastDateTimeException, IllegalValueException, CommandException {
        Model model = new ModelManagerMock();

        Command command = new AddCommandParser().parse("stand by me by tmr");
        command.setData(model);
        CommandResult result = command.execute();

        Command command2 = new EditCommandParser().parse("1 by 2 days later");
        command2.setData(model);
        CommandResult result2 = command2.execute();

        Deadline deadline = new Deadline(ZonedDateTime.now().plusDays(2).truncatedTo(ChronoUnit.MINUTES));
        Task expectedTask = new Task(new Name("stand by me"), Optional.of(deadline), Optional.empty(),
                new UniqueTagList());
        actualTask.setDeadline(new Deadline(
                actualTask.getDeadline().get().getDateTime().truncatedTo(ChronoUnit.MINUTES)));

        assertEquals(expectedTask, actualTask);
        System.out.println(result2.feedbackToUser);
    }

}
