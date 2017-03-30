package guitests;

import static org.junit.Assert.assertTrue;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import guitests.guihandles.TaskCardHandle;
import seedu.address.commons.core.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.model.tag.Tag;
import seedu.address.model.task.Name;
import seedu.address.model.task.StartEndDateTime;
import seedu.address.testutil.TaskBuilder;
import seedu.address.testutil.TestTask;

// TODO: reduce GUI tests by transferring some tests to be covered by lower level tests.
public class EditCommandTest extends TaskListGuiTest {

    // The list of tasks in the task list panel(NOTE TO CHANGE) is expected to match this list.
    // This list is updated with every successful call to assertEditSuccess().
    TestTask[] expectedTasksList = td.getTypicalTasks();

    //author A0140023E
    //TODO not all fields are specified because they cannot
    // GUI tests are slow, so just check StartEndDateTime here and check Deadline and floating task elsewhere
    /**
     * This method tests if editing all fields will be successful. However, StartEndDateTime
     * cannot exist with Deadline, so Deadline cannot be edited here
     * @throws Exception
     */
    @Test
    public void edit_allFieldsSpecifiedWithStartEndDateTime_success() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Task with start and end date");
        // A date after today is created so as to not generate a PastDateTimeException.
        // Date is also truncated to seconds precision as Natty does not parse milliseconds
        ZonedDateTime startDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(3);
        ZonedDateTime endDateTime = startDateTime.plusDays(1);
        sb.append(" from " + startDateTime);
        sb.append(" to " + endDateTime);
        sb.append(" t/IMPT");
        String detailsToEdit = sb.toString();

        int taskListIndex = 1;

        TestTask editedTask = new TaskBuilder().withName("Task with start and end date")
                .withStartEndDateTime(new StartEndDateTime(startDateTime, endDateTime)).withTags("IMPT").build();

        assertEditSuccess(taskListIndex, taskListIndex, detailsToEdit, editedTask);
    }

    @Test
    public void edit_notAllFieldsSpecifiedWithTagsOnly_success() throws Exception {
        // TODO notice how tags have to be in alphabetical order because of the way tags are added
        // and compared. See TestUtil::compareCardAndTask using list compare instead of set compare
        String detailsToEdit = "t/fail t/lose";
        int taskListIndex = 2;

        TestTask taskToEdit = expectedTasksList[taskListIndex - 1];
        TestTask editedTask = new TaskBuilder(taskToEdit).withTags("fail", "lose").build();

        assertEditSuccess(taskListIndex, taskListIndex, detailsToEdit, editedTask);
    }

    //@@author
    @Test
    public void edit_clearTags_success() throws Exception {
        String detailsToEdit = "t/";
        int taskListIndex = 2;

        TestTask taskToEdit = expectedTasksList[taskListIndex - 1];
        TestTask editedTask = new TaskBuilder(taskToEdit).withTags().build();

        assertEditSuccess(taskListIndex, taskListIndex, detailsToEdit, editedTask);
    }

    @Test
    public void edit_findThenEdit_success() throws Exception {
        commandBox.runCommand("find Elephant");

        String detailsToEdit = "Bellyphant";
        int filteredTaskListIndex = 1; // TODO notice the bad naming here again
        int taskListIndex = 5;

        TestTask taskToEdit = expectedTasksList[taskListIndex - 1];
        TestTask editedTask = new TaskBuilder(taskToEdit).withName("Bellyphant").build();

        assertEditSuccess(filteredTaskListIndex, taskListIndex, detailsToEdit, editedTask);
    }

    @Test
    public void edit_missingTaskIndex_failure() {
        commandBox.runCommand("edit Basketball");
        assertResultMessage(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE));
    }

    @Test
    public void edit_invalidTaskIndex_failure() {
        commandBox.runCommand("edit 8 Basketball");
        assertResultMessage(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
    }

    @Test
    public void edit_noFieldsSpecified_failure() {
        commandBox.runCommand("edit 1");
        assertResultMessage(EditCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void edit_invalidValues_failure() {
        commandBox.runCommand("edit 1 *&");
        assertResultMessage(Name.MESSAGE_NAME_CONSTRAINTS);

        //commandBox.runCommand("edit 1 p/abcd"); TODO currently not sufficient constraints so commented
        //assertResultMessage(Phone.MESSAGE_PHONE_CONSTRAINTS);

        //commandBox.runCommand("edit 1 e/yahoo!!!");
        //assertResultMessage(Email.MESSAGE_EMAIL_CONSTRAINTS);

        //commandBox.runCommand("edit 1 a/");
        //assertResultMessage(Address.MESSAGE_ADDRESS_CONSTRAINTS);

        commandBox.runCommand("edit 1 t/*&");
        assertResultMessage(Tag.MESSAGE_TAG_CONSTRAINTS);
    }

    @Test
    public void edit_duplicateTask_failure() {
        commandBox.runCommand("edit 3 Amuse friend t/friends");
        assertResultMessage(EditCommand.MESSAGE_DUPLICATE_TASK);
    }

    /**
     * Checks whether the edited task has the correct updated details.
     *
     * @param filteredTaskListIndex index of task to edit in filtered list // TODO note again due to bad naming
     * @param taskListIndex index of task to edit in the task list.
     *      Must refer to the same task as {@code filteredTaskListIndex}
     * @param detailsToEdit details to edit the task with as input to the edit command
     * @param editedTask the expected task after editing the task's details
     */
    private void assertEditSuccess(int filteredTaskListIndex, int taskListIndex,
                                    String detailsToEdit, TestTask editedTask) {
        commandBox.runCommand("edit " + filteredTaskListIndex + " " + detailsToEdit);

        // confirm the new card contains the right data
        TaskCardHandle editedCard = taskListPanel.navigateToTask(editedTask.getName().value);
        assertMatching(editedTask, editedCard);

        // confirm the list now contains all previous tasks plus the task with updated details
        expectedTasksList[taskListIndex - 1] = editedTask;
        assertTrue(taskListPanel.isListMatching(expectedTasksList));
        assertResultMessage(String.format(EditCommand.MESSAGE_EDIT_TASK_SUCCESS, editedTask));
    }
}
