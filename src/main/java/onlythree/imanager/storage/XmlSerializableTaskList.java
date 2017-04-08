package onlythree.imanager.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import onlythree.imanager.commons.core.UnmodifiableObservableList;
import onlythree.imanager.commons.exceptions.IllegalValueException;
import onlythree.imanager.model.ReadOnlyTaskList;
import onlythree.imanager.model.tag.Tag;
import onlythree.imanager.model.task.ReadOnlyTask;
import onlythree.imanager.model.task.Task;
import onlythree.imanager.model.task.exceptions.InvalidDurationException;

/**
 * An Immutable TaskList that is serializable to XML format
 */
@XmlRootElement(name = "tasklist")
public class XmlSerializableTaskList implements ReadOnlyTaskList {

    @XmlElement(name = "task")
    private List<XmlAdaptedTask> tasks;
    @XmlElement
    private List<XmlAdaptedTag> tags;

    /**
     * Creates an empty XmlSerializableTaskList.
     * This empty constructor is required for marshalling.
     */
    public XmlSerializableTaskList() {
        tasks = new ArrayList<>();
        tags = new ArrayList<>();
    }

    /**
     * Conversion
     */
    public XmlSerializableTaskList(ReadOnlyTaskList src) {
        this();
        tasks.addAll(src.getTaskList().stream().map(XmlAdaptedTask::new).collect(Collectors.toList()));
        tags.addAll(src.getTagList().stream().map(XmlAdaptedTag::new).collect(Collectors.toList()));
    }

    @Override
    public ObservableList<ReadOnlyTask> getTaskList() {
        final ObservableList<Task> tasks = this.tasks.stream().map(p -> {
            try {
                return p.toModelType();
            } catch (InvalidDurationException e) {
                return null;
            } catch (IllegalValueException e) {
                //TODO: better error handling
                return null;
            }
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
        return new UnmodifiableObservableList<>(tasks);
    }

    @Override
    public ObservableList<Tag> getTagList() {
        final ObservableList<Tag> tags = this.tags.stream().map(t -> {
            try {
                return t.toModelType();
            } catch (IllegalValueException e) {
                e.printStackTrace();
                //TODO: better error handling
                return null;
            }
        }).collect(Collectors.toCollection(FXCollections::observableArrayList));
        return new UnmodifiableObservableList<>(tags);
    }

}
