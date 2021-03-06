package onlythree.imanager.testutil;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import onlythree.imanager.commons.exceptions.IllegalValueException;
import onlythree.imanager.model.TaskList;
import onlythree.imanager.model.task.Deadline;
import onlythree.imanager.model.task.StartEndDateTime;
import onlythree.imanager.model.task.Task;
import onlythree.imanager.model.task.exceptions.InvalidDurationException;
import onlythree.imanager.model.task.exceptions.PastDateTimeException;

public class TypicalTestTasks {

    //@@author A0140023E
    // Naming of tasks as names does not seem to be a good idea, and not descriptive enough
    // for example helpMe and iAmCode are to be manually added in test cases but it is not clear.
    // Conversely the name shows what the task contains, so it might be helpful
    public TestTask amuseFriend, bet, count, dog, elephant, flipTable, goondu, helpMe, iAmCode;

    public TypicalTestTasks() {
        // Starting Test Date Time is set to one day after today so that dates in the past is not
        // generated to prevent a PastDateTimeException from occuring. Furthermore the precision
        // is truncated to seconds as Natty does not parse milliseconds
        ZonedDateTime startTestDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1);
        try {
            // floating task with a tag
            amuseFriend = new TaskBuilder().withName("Amuse friend").withTags("friends").build();
            // Notice how flipping the order of tags and test cases can fail, see TestUtil::compareCardAndTask
            // using list compare instead of set compare
            // floating task with two tags
            bet = new TaskBuilder().withName("Bet dog race").withTags("luck", "money").build();
            // floating task with no tags
            count = new TaskBuilder().withName("Count chickens before they hatch").build();
            // task with deadlines
            dog = new TaskBuilder().withName("Dog naming")
                    .withDeadline(new Deadline(startTestDateTime.plusDays(3))).build();
            // task with start and end dates
            elephant = new TaskBuilder().withName("Elephant riding")
                    .withStartEndDateTime(new StartEndDateTime(startTestDateTime.plusHours(2),
                                                               startTestDateTime.plusHours(3)))
                    .build();
            // some other random floating tasks
            flipTable = new TaskBuilder().withName("Flip table").build();
            goondu = new TaskBuilder().withName("Goondu goon").build();

            // Manually added
            helpMe = new TaskBuilder().withName("Help me").build();
            iAmCode = new TaskBuilder().withName("I am code").build();
        } catch (PastDateTimeException e) {
            throw new AssertionError("The typical test tasks should not be built with date-times in the past");
        } catch (InvalidDurationException e) {
            throw new AssertionError("The typical test tasks should not be built with an invalid duration period");
        } catch (IllegalValueException e) {
            throw new AssertionError("The typical test tasks should already meet all the constraints required");
        }
    }

    public static void loadTaskListWithSampleData(TaskList taskList) {
        for (TestTask task : new TypicalTestTasks().getTypicalTasks()) {
            try {
                taskList.addTask(new Task(task));
            } catch (IllegalValueException e) {
                throw new AssertionError("Copying a valid task should always result in a valid task.");
            }
        }
    }

    public TestTask[] getTypicalTasks() {
        return new TestTask[]{amuseFriend, bet, count, dog, elephant, flipTable, goondu};
    }

    //@@author
    public TaskList getTypicalTaskList() {
        TaskList taskList = new TaskList();
        loadTaskListWithSampleData(taskList);
        return taskList;
    }
}
