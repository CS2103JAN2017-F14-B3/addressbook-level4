//@@author A0148052L
package guitests;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import onlythree.imanager.commons.core.Messages;
import onlythree.imanager.logic.commands.SaveCommand;

public class SaveCommandTest extends TaskListGuiTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void save_file_successful() {
        String filePath = testFolder.getRoot().getPath() + File.separator + "taskList.xml";
        assertSaveSuccess(filePath);
    }

    @Test
    public void save_filePathError() {
        commandBox.runCommand("save " + "/.xml");
        assertResultMessage(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                             SaveCommand.MESSAGE_INVALID_FILE_PATH));
    }

    private void assertSaveSuccess(String filePath) {
        commandBox.runCommand("save " + filePath);
        assertResultMessage(String.format(SaveCommand.MESSAGE_SUCCESS, filePath));
    }
}
