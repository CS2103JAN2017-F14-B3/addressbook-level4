package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.task.ReadOnlyTask;
import seedu.address.model.task.StartEndDateTime;

// TODO card design
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "Card.fxml";

    @FXML
    private HBox cardPane;
    @FXML
    private HBox startend_only;
    @FXML
    private HBox deadline_only;
    @FXML
    private HBox startend_deadline;
    @FXML
    private Label event;
    @FXML
    private Label id;
    @FXML
    private Label startdate1;
    @FXML
    private Label startdate2;
    @FXML
    private Label enddate1;
    @FXML
    private Label enddate2;
    @FXML
    private Label deadline1;
    @FXML
    private Label deadline2;
    @FXML
    private FlowPane tags;

    public PersonCard(ReadOnlyTask person, int displayedIndex) {
        super(FXML);
        event.setText(person.getName().value);
        id.setText(displayedIndex + ". ");

        if (person.getStartEndDateTime().isPresent() && person.getDeadline().isPresent()) {
            startend_deadline.setVisible(true);
            startend_only.setVisible(false);
            deadline_only.setVisible(false);
            StartEndDateTime startEndDateTime = person.getStartEndDateTime().get();
            startdate2.setText(startEndDateTime.getStartDateTime().format(ParserUtil.DATE_TIME_FORMAT));
            enddate2.setText(startEndDateTime.getEndDateTime().format(ParserUtil.DATE_TIME_FORMAT));
            deadline2.setText(person.getDeadline().get().getValue().format(ParserUtil.DATE_TIME_FORMAT));
        } else if (person.getStartEndDateTime().isPresent()) {
            startend_only.setVisible(true);
            startend_deadline.setVisible(false);
            deadline_only.setVisible(false);
            StartEndDateTime startEndDateTime = person.getStartEndDateTime().get();
            startdate1.setText(startEndDateTime.getStartDateTime().format(ParserUtil.DATE_TIME_FORMAT));
            enddate1.setText(startEndDateTime.getEndDateTime().format(ParserUtil.DATE_TIME_FORMAT));
        } else if (person.getDeadline().isPresent()) {
            deadline_only.setVisible(true);
            startend_only.setVisible(false);
            startend_deadline.setVisible(false);
            deadline1.setText(person.getDeadline().get().getValue().format(ParserUtil.DATE_TIME_FORMAT));
        } else {
            startend_only.setVisible(false);
            startend_only.setVisible(false);
            deadline_only.setVisible(false);
            startend_deadline.setVisible(false);
        }
        initTags(person);
    }

    private void initTags(ReadOnlyTask person) {
        person.getTags().forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
    }
}
