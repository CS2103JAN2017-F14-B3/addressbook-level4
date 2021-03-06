package onlythree.imanager.model.task;

import java.util.Optional;

import onlythree.imanager.commons.exceptions.IllegalValueException;
import onlythree.imanager.commons.util.CollectionUtil;
import onlythree.imanager.model.tag.UniqueTagList;

/**
 * Represents a Task in the task list.
 * Guarantees: details are present and not null, field values are validated.
 */
public class Task extends ReadOnlyTask {

    //@@author A0140023E
    public static final String MESSAGE_TASK_CONSTRAINTS =
            "Task cannot have both deadline and start and end date/time";

    //@@author
    private Name name;

    private UniqueTagList tags;

    //@@author A0140023E
    private Optional<StartEndDateTime> startEndDateTime;

    private Optional<Deadline> deadline;

    //@@author A0135998H
    private boolean complete;

    //@@author A0140023E
    /**
     * Every field must not be null except for the {@code Optional} fields. The task is
     * automatically initialized as not complete.
     *
     * @throws IllegalValueException if the Task to be constructed has both Deadline and
     *         StartEndDateTime
     */
    public Task(Name name, Optional<Deadline> deadline, Optional<StartEndDateTime> startEndDateTime,
            UniqueTagList tags) throws IllegalValueException {
        this(name, deadline, startEndDateTime, tags, false);
    }

    /**
     * Every field must not be null except for the {@code Optional} fields. This constructor
     * requires whether the task is complete to be specified.
     *
     * @throws IllegalValueException if the Task to be constructed has both Deadline and
     *         StartEndDateTime
     */
    public Task(Name name, Optional<Deadline> deadline, Optional<StartEndDateTime> startEndDateTime,
            UniqueTagList tags, boolean isComplete) throws IllegalValueException {
        assert !CollectionUtil.isAnyNull(name, deadline, startEndDateTime, tags);

        if (deadline.isPresent() && startEndDateTime.isPresent()) {
            throw new IllegalValueException(MESSAGE_TASK_CONSTRAINTS);
        }

        this.name = name;
        this.deadline = deadline;
        this.startEndDateTime = startEndDateTime;
        this.tags = new UniqueTagList(tags); // protect internal tags from changes in the arg list
        this.complete = isComplete;
    }

    /**
     * Creates a copy of the given ReadOnlyTask.
     * @throws IllegalValueException if the ReadOnlyTask to be copied has both Deadline and StartEndDateTime
     */
    public Task(ReadOnlyTask source) throws IllegalValueException {
        this(source.getName(), source.getDeadline(), source.getStartEndDateTime(), source.getTags(),
             source.isComplete());
    }

    //@@author
    public void setName(Name name) {
        assert name != null;
        this.name = name;
    }

    @Override
    public Name getName() {
        return name;
    }

    //@@author A0140023E
    @Override
    public Optional<Deadline> getDeadline() {
        return deadline;
    }

    /**
     * Only allow changing the {@link StartEndDateTime} if there is actually a value (not {@link Optional}).
     */
    public void setDeadline(Deadline dateTime) {
        assert dateTime != null;
        this.deadline = Optional.of(dateTime);
    }

    //@@author A0135998H
    @Override
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    //@@author A0140023E
    @Override
    public Optional<StartEndDateTime> getStartEndDateTime() {
        return startEndDateTime;
    }

    /**
     * Only allow changing the {@link StartEndDateTime} if there is actually a value (not {@link Optional}).
     */
    public void setStartEndDateTime(StartEndDateTime startEndDateTime) {
        assert startEndDateTime != null;
        this.startEndDateTime = Optional.of(startEndDateTime);
    }

    //@@author
    @Override
    public UniqueTagList getTags() {
        return new UniqueTagList(tags);
    }

    /**
     * Replaces this task's tags with the tags in the argument tag list.
     */
    public void setTags(UniqueTagList replacement) {
        tags.setTags(replacement);
    }

    //@@author A0140023E
    /**
     * Updates this task with the details of {@code replacement} using a "shallow copy" of the data. Refer to
     * {@link Object#clone()} for more information about shallow copy.
     */
    public void resetData(ReadOnlyTask replacement) {
        assert replacement != null;

        // Note that we are shallow copying data replacement's data so replacement should not be
        // reused anymore. Otherwise modify this method to do a deep copy

        setName(replacement.getName());

        // Note that the same Optional is being reused from replacement directly
        // Hence the setter method cannot be used for both deadline and startEndDateTime
        deadline = replacement.getDeadline();
        startEndDateTime = replacement.getStartEndDateTime();

        setTags(replacement.getTags());
        setComplete(replacement.isComplete());
    }
}
