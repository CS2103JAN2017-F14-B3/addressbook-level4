package onlythree.imanager.model;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.transformation.FilteredList;
import onlythree.imanager.commons.core.ComponentManager;
import onlythree.imanager.commons.core.LogsCenter;
import onlythree.imanager.commons.core.UnmodifiableObservableList;
import onlythree.imanager.commons.events.model.TaskListChangedEvent;
import onlythree.imanager.commons.events.model.ViewListChangedEvent;
import onlythree.imanager.commons.events.ui.JumpToListRequestEvent;
import onlythree.imanager.commons.util.CollectionUtil;
import onlythree.imanager.commons.util.StringUtil;
import onlythree.imanager.logic.commands.ViewCommand;
import onlythree.imanager.model.task.Deadline;
import onlythree.imanager.model.task.IterableTaskList.TaskNotFoundException;
import onlythree.imanager.model.task.ReadOnlyTask;
import onlythree.imanager.model.task.StartEndDateTime;
import onlythree.imanager.model.task.Task;

/**
 * Represents the in-memory model of the task list data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TaskList taskList;
    private final FilteredList<ReadOnlyTask> filteredTasks;
    //@@author A0148052L
    private Stack<TaskList> statusStack;
    private Stack<String> commandStack;
    private Stack<TaskList> undoneStatus;
    private Stack<String> undoneCommand;
    private TaskList statusAfterUndo;
    //@@author

    /**
     * Initializes a ModelManager with the given taskList and userPrefs.
     */
    public ModelManager(ReadOnlyTaskList taskList, UserPrefs userPrefs) {
        super();
        assert !CollectionUtil.isAnyNull(taskList, userPrefs);

        logger.fine("Initializing with task list: " + taskList + " and user prefs " + userPrefs);

        this.taskList = new TaskList(taskList);
        filteredTasks = new FilteredList<>(this.taskList.getTaskList());
        //@@author A0148052L
        statusStack = new Stack<TaskList>();
        commandStack = new Stack<String>();
        undoneStatus = new Stack<TaskList>();
        undoneCommand = new Stack<String>();
        pushStatus(taskList);
        //@@author
    }

    public ModelManager() {
        this(new TaskList(), new UserPrefs());
    }

    @Override
    public void resetData(ReadOnlyTaskList newData) {
        taskList.resetData(newData);
        indicateTaskListChanged();
    }

    @Override
    public ReadOnlyTaskList getTaskList() {
        return taskList;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateTaskListChanged() {
        raise(new TaskListChangedEvent(taskList));
    }

    //@@author A0135998H
    /** Raises an event to indicate the filteredList has changed */
    private void indicateViewListChanged(String typeOfListView) {
        raise(new ViewListChangedEvent(typeOfListView));
    }

    //@@author
    @Override
    public synchronized void deleteTask(ReadOnlyTask target) throws TaskNotFoundException {
        taskList.removeTask(target);
        indicateTaskListChanged();
    }

    //@@author A0140023E
    /**
     * Adds a task to the task list and select the added task.
     */
    @Override
    public synchronized void addTask(Task task) {
        int taskIndex = taskList.addTask(task);

        updateFilteredListToShowAll();

        indicateTaskListChanged();

        raise(new JumpToListRequestEvent(taskIndex));
    }

    //@@author
    @Override
    public void updateTask(int filteredTaskListIndex, ReadOnlyTask editedTask) {
        assert editedTask != null;

        int taskIndex = filteredTasks.getSourceIndex(filteredTaskListIndex);

        taskList.updateTask(taskIndex, editedTask);

        indicateTaskListChanged();
    }

    //@@author A0148052L
    @Override
    public boolean isCommandStackEmpty() {
        return commandStack.isEmpty();
    }

    @Override
    public boolean isUndoneCommandEmpty() {
        return undoneCommand.isEmpty();
    }

    @Override
    public boolean isUndoneStatusEmpty() {
        return undoneStatus.isEmpty();
    }

    @Override
    public void pushCommand(String command) {
        commandStack.push(command);
    }

    @Override
    public void pushStatus(ReadOnlyTaskList currentStatus) {
        TaskList presentStatus = new TaskList(currentStatus);
        statusStack.push(presentStatus);
    }

    @Override
    public void setStatusAfterUndo(ReadOnlyTaskList statusAfterUndo) {
        TaskList theStatusAfterUndo = new TaskList(statusAfterUndo);
        this.statusAfterUndo = theStatusAfterUndo;
    }

    @Override
    public void popUndoneStatus() {
        TaskList latestUndoneStatus = undoneStatus.pop();
        statusStack.push(latestUndoneStatus);
    }

    @Override
    public void popUndoneCommand() {
        String latestUndoneCommand = undoneCommand.pop();
        commandStack.push(latestUndoneCommand);
    }

    @Override
    public void popCurrentStatus() {
        if (!statusStack.isEmpty()) {
            TaskList currentStatus = statusStack.pop();
            undoneStatus.push(currentStatus);
        }
    }

    @Override
    public String getPreviousCommand() {
        String prevCommand;
        if (commandStack.isEmpty()) {
            prevCommand = null;
        } else {
            prevCommand = commandStack.pop();
            undoneCommand.push(prevCommand);
        }
        return prevCommand;
    }

    @Override
    public TaskList getPrevStatus() {
        return statusStack.peek();
    }

    @Override
    public TaskList getStatusAfterUndo() {
        return this.statusAfterUndo;
    }
   //@@author

    //=========== Filtered Task List Accessors =============================================================

    //@@author A0140023E
    @Override
    public int getSourceIndexFromFilteredTasks(int filteredTaskListIndex) {
        return filteredTasks.getSourceIndex(filteredTaskListIndex);
    }

    //@@author
    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredTaskList() {
        return new UnmodifiableObservableList<>(filteredTasks);
    }

    //@@author A0135998H
    @Override
    public void updateFilteredListToShowAll() {
        indicateViewListChanged(ViewCommand.TYPE_ALL);
        filteredTasks.setPredicate(null);
    }

    @Override
    public void updateFilteredListToShowDone() {
        filteredTasks.setPredicate((Predicate<? super ReadOnlyTask>) task -> {
            return task.isComplete();
        });
        indicateViewListChanged(ViewCommand.TYPE_DONE);
    }

    @Override
    public void updateFilteredListToShowFloating() {
        filteredTasks.setPredicate((Predicate<? super ReadOnlyTask>) task -> {
            return isFloating(task) && !(task.isComplete());
        });
        indicateViewListChanged(ViewCommand.TYPE_FLOATING);
    }

    @Override
    public void updateFilteredListToShowOverdue() {
        filteredTasks.setPredicate((Predicate<? super ReadOnlyTask>) task -> {
            return isOverdue(task) && !(task.isComplete());
        });
        indicateViewListChanged(ViewCommand.TYPE_OVERDUE);
    }

    @Override
    public void updateFilteredListToShowPending() {
        filteredTasks.setPredicate((Predicate<? super ReadOnlyTask>) task -> {
            return !(task.isComplete());
        });
        indicateViewListChanged(ViewCommand.TYPE_PENDING);
    }

    @Override
    public void updateFilteredListToShowToday() {
        filteredTasks.setPredicate((Predicate<? super ReadOnlyTask>) task -> {
            return isToday(task) && !(task.isComplete());
        });
        indicateViewListChanged(ViewCommand.TYPE_TODAY);
    }

    @Override
    public void updateFilteredTaskList(Set<String> keywords) {
        indicateViewListChanged(ViewCommand.TYPE_ALL);
        updateFilteredTaskList(new PredicateExpression(new NameQualifier(keywords)));
    }

    //@@author
    private void updateFilteredTaskList(Expression expression) {
        filteredTasks.setPredicate(expression::satisfies);
    }

    //========== Inner classes/interfaces used for filtering =================================================

    interface Expression {
        boolean satisfies(ReadOnlyTask task);
        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyTask task) {
            return qualifier.run(task);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyTask task);
        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            return nameKeyWords.stream()
                    .filter(keyword -> StringUtil.containsWordIgnoreCase(task.getName().value, keyword))
                    .findAny()
                    .isPresent();
        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }

    //@@author A0135998H
    public boolean isOverdue(ReadOnlyTask task) {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        if (task.getStartEndDateTime().isPresent()) {
            StartEndDateTime startEndDateTime = task.getStartEndDateTime().get();
            return currentDateTime.isAfter(startEndDateTime.getEndDateTime());
        } else if (task.getDeadline().isPresent()) {
            Deadline deadline = task.getDeadline().get();
            return currentDateTime.isAfter(deadline.getDateTime());
        }
        return false;
    }

    public boolean isFloating(ReadOnlyTask task) {
        return !(task.getStartEndDateTime().isPresent()) && !(task.getDeadline().isPresent());
    }

    public boolean isToday(ReadOnlyTask task) {
        ZonedDateTime currentDateTime = ZonedDateTime.now();
        if (task.getStartEndDateTime().isPresent()) {
            StartEndDateTime startEndDateTime = task.getStartEndDateTime().get();
            return !(currentDateTime.isBefore(startEndDateTime.getStartDateTime())
                    || currentDateTime.isAfter(startEndDateTime.getEndDateTime()));
        } else if (task.getDeadline().isPresent()) {
            Deadline deadline = task.getDeadline().get();
            return !(currentDateTime.isAfter(deadline.getDateTime()));
        }
        return false;
    }

}
