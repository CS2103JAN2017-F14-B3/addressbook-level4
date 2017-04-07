package guitests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.testfx.api.FxToolkit;

import com.google.common.base.Charsets;

import edu.emory.mathcs.backport.java.util.Arrays;
import guitests.guihandles.CommandBoxHandle;
import guitests.guihandles.MainGuiHandle;
import guitests.guihandles.MainMenuHandle;
import guitests.guihandles.PersonCardHandle;
import guitests.guihandles.PersonListPanelHandle;
import guitests.guihandles.ResultDisplayHandle;
import javafx.application.Platform;
import javafx.stage.Stage;
import seedu.address.TestApp;
import seedu.address.commons.core.EventsCenter;
import seedu.address.commons.events.BaseEvent;
import seedu.address.model.TaskList;
import seedu.address.model.task.ReadOnlyTask;
import seedu.address.testutil.TestUtil;
import seedu.address.testutil.TypicalTestTasks;

/**
 * A GUI Test class for TaskList.
 */
public abstract class TaskListGuiTest {

    /* The TestName Rule makes the current test name available inside test methods */
    @Rule
    public TestName name = new TestName();

    TestApp testApp;

    protected TypicalTestTasks td = new TypicalTestTasks();

    /*
     *   Handles to GUI elements present at the start up are created in advance
     *   for easy access from child classes.
     */
    protected MainGuiHandle mainGui;
    protected MainMenuHandle mainMenu;
    protected PersonListPanelHandle personListPanel;
    protected ResultDisplayHandle resultDisplay;
    protected CommandBoxHandle commandBox;
    private Stage stage;

    @BeforeClass
    public static void setupSpec() {
        try {
            FxToolkit.registerPrimaryStage();
            FxToolkit.hideStage();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() throws Exception {
        FxToolkit.setupStage((stage) -> {
            mainGui = new MainGuiHandle(new GuiRobot(), stage);
            mainMenu = mainGui.getMainMenu();
            personListPanel = mainGui.getPersonListPanel();
            resultDisplay = mainGui.getResultDisplay();
            commandBox = mainGui.getCommandBox();
            this.stage = stage;
        });
        EventsCenter.clearSubscribers();
        testApp = (TestApp) FxToolkit.setupApplication(() -> new TestApp(this::getInitialData, getDataFileLocation()));
        FxToolkit.showStage();
        while (!stage.isShowing());
        mainGui.focusOnMainApp();
    }

    /**
     * Override this in child classes to set the initial local data.
     * Return null to use the data in the file specified in {@link #getDataFileLocation()}
     */
    protected TaskList getInitialData() {
        TaskList taskList = new TaskList();
        TypicalTestTasks.loadTaskListWithSampleData(taskList);
        return taskList;
    }

    /**
     * Override this in child classes to set the data file location.
     */
    protected String getDataFileLocation() {
        return TestApp.SAVE_LOCATION_FOR_TESTING;
    }

    @After
    public void cleanup() throws TimeoutException {
        FxToolkit.cleanupStages();
    }

    /**
     * Asserts the task shown in the card is same as the given task
     */
    public void assertMatching(ReadOnlyTask task, PersonCardHandle card) {
        assertTrue(TestUtil.compareCardAndPerson(card, task)); // TODO UI renaming
    }

    /**
     * Asserts the size of the person list is equal to the given number. // TODO UI renaming
     */
    protected void assertListSize(int size) {
        int numberOfPeople = personListPanel.getNumberOfPeople();
        assertEquals(size, numberOfPeople);
    }

    /**
     * Asserts the message shown in the Result Display area is same as the given string.
     */
    protected void assertResultMessage(String expected) {
        System.out.println("Expected");
        System.out.println("Length: " + expected.getBytes(Charsets.UTF_8).length);
        System.out.println(Arrays.toString(expected.getBytes(Charsets.UTF_8)));
        System.out.println("Expected End");
        System.out.println("resultDisplay");
        System.out.println("Length: " + resultDisplay.getText().getBytes(Charsets.UTF_8).length);
        System.out.println(Arrays.toString(resultDisplay.getText().getBytes(Charsets.UTF_8)));
        System.out.println("resultDisplay End");
        System.out.println("Difference begin");
        System.out.println("Length: " + StringUtils.difference(expected, resultDisplay.getText()).length());
        System.out.println(StringUtils.difference(expected, resultDisplay.getText()));
        System.out.println("Distance: " + StringUtils.getLevenshteinDistance(expected, resultDisplay.getText()));
        System.out.println("Difference end");
        assertEquals(expected, resultDisplay.getText());
        //assertEquals(expected.replaceAll("\\s+", ""), resultDisplay.getText().replaceAll("\\s+", ""));
    }

    public void raise(BaseEvent e) {
        //JUnit doesn't run its test cases on the UI thread. Platform.runLater is used to post event on the UI thread.
        Platform.runLater(() -> EventsCenter.getInstance().post(e));
    }
}
