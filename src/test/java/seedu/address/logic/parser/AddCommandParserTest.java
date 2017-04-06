package seedu.address.logic.parser;

import static org.junit.Assert.assertEquals;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
import seedu.address.model.task.StartEndDateTime;
import seedu.address.model.task.Task;
import seedu.address.model.task.UniqueTaskList;
import seedu.address.model.task.exceptions.InvalidDurationException;
import seedu.address.model.task.exceptions.PastDateTimeException;

//@@author A0140023E
public class AddCommandParserTest {
        // add stand by tmr by me
    // TODO make logicmanagertest test some simple add and edit
    // TODO fixed dates like 25 Apr can fail if it is in the past
    // TODO see who else is using ExpectedException

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

    @Test
    public void testEdit2() throws PastDateTimeException, IllegalValueException, CommandException {
        Model model = new ModelManagerMock();

        Command command = new AddCommandParser().parse("stand by me by tmr");
        command.setData(model);
        CommandResult result = command.execute();

        Command command2 = new EditCommandParser().parse("1 Pass rose from Uncle to Jane by 5 days later");
        command2.setData(model);
        CommandResult result2 = command2.execute();

        Deadline deadline = new Deadline(ZonedDateTime.now().plusDays(5).truncatedTo(ChronoUnit.MINUTES));
        Task expectedTask = new Task(new Name("Pass rose from Uncle to Jane"), Optional.of(deadline), Optional.empty(),
                new UniqueTagList());
        actualTask.setDeadline(new Deadline(
                actualTask.getDeadline().get().getDateTime().truncatedTo(ChronoUnit.MINUTES)));

        assertEquals(expectedTask, actualTask);
        System.out.println(result2.feedbackToUser);
    }

    @Test
    public void testEdit3() throws PastDateTimeException, IllegalValueException, CommandException {
        Model model = new ModelManagerMock();

        Command command = new AddCommandParser().parse("stand by me by tmr");
        command.setData(model);
        CommandResult result = command.execute();

        Command command2 = new EditCommandParser().parse("1 by 8 days from 25 Apr");
        command2.setData(model);
        CommandResult result2 = command2.execute();

        Deadline deadline = new Deadline(ZonedDateTime.now().withMonth(4).withDayOfMonth(25).plusDays(8)
                .truncatedTo(ChronoUnit.MINUTES));
        Task expectedTask = new Task(new Name("stand by me"), Optional.of(deadline), Optional.empty(),
                new UniqueTagList());
        actualTask.setDeadline(new Deadline(
                actualTask.getDeadline().get().getDateTime().truncatedTo(ChronoUnit.MINUTES)));

        assertEquals(expectedTask, actualTask);
        System.out.println(result2.feedbackToUser);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testEditSpecial1() throws PastDateTimeException, IllegalValueException, CommandException {
        Model model = new ModelManagerMock();

        Command command = new AddCommandParser().parse("stand by me by tmr");
        command.setData(model);
        CommandResult result = command.execute();

        exception.expect(CommandException.class); // TODO

        Command command2 = new EditCommandParser().parse("1 by 8 days later from 25 Apr");
        command2.setData(model);
        CommandResult result2 = command2.execute();


        Deadline deadline = new Deadline(ZonedDateTime.now().withMonth(4).withDayOfMonth(25).plusDays(8)
                .truncatedTo(ChronoUnit.MINUTES));
        Task expectedTask = new Task(new Name("stand by me"), Optional.of(deadline), Optional.empty(),
                new UniqueTagList());
        actualTask.setDeadline(new Deadline(
                actualTask.getDeadline().get().getDateTime().truncatedTo(ChronoUnit.MINUTES)));

        assertEquals(expectedTask, actualTask);
        System.out.println(result2.feedbackToUser);
    }

    @Test
    public void testEditSpecial2()
            throws PastDateTimeException, InvalidDurationException, IllegalValueException, CommandException {
        Model model = new ModelManagerMock();

        Command command = new AddCommandParser().parse("stand by me from 23 Apr to 28 Apr");
        command.setData(model);
        CommandResult result = command.execute();

        Command command2 = new EditCommandParser().parse("1 by 8 days later from 25 Apr");
        command2.setData(model);
        CommandResult result2 = command2.execute();


        ZonedDateTime startDateTime =
                ZonedDateTime.now().withMonth(4).withDayOfMonth(25).truncatedTo(ChronoUnit.MINUTES);
        ZonedDateTime endDateTime =
                ZonedDateTime.now().withMonth(4).withDayOfMonth(28).truncatedTo(ChronoUnit.MINUTES);
        StartEndDateTime startEndDateTime = new StartEndDateTime(startDateTime, endDateTime);
        Task expectedTask = new Task(new Name("by 8 days later"), Optional.empty(), Optional.of(startEndDateTime),
                new UniqueTagList());

        StartEndDateTime actualStartEndDateTime = actualTask.getStartEndDateTime().get();
        actualTask.setStartEndDateTime(new StartEndDateTime(
                actualStartEndDateTime.getStartDateTime().truncatedTo(ChronoUnit.MINUTES),
                actualStartEndDateTime.getEndDateTime().truncatedTo(ChronoUnit.MINUTES)));

        assertEquals(expectedTask, actualTask);
        System.out.println(result2.feedbackToUser);
    }

}
