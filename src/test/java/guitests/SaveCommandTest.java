//@@author A0148052L
package guitests;

import org.junit.Test;

import onlythree.imanager.commons.core.Messages;
import onlythree.imanager.logic.commands.SaveCommand;

public class SaveCommandTest extends TaskListGuiTest {

    @Test
    public void save_file_successful() {
        String filePath = "data" + "\\" + "taskList.xml";
        assertSaveSuccess(filePath);
    }

    @Test
    public void save_filePathError() {
        commandBox.runCommand("save " + "C:" + "\\" + "Users" + "\\" + ".xml");
        assertResultMessage(String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                             SaveCommand.MESSAGE_INVALID_FILE_PATH));
    }

    private void assertSaveSuccess(String filePath) {
        commandBox.runCommand("save " + filePath);
        assertResultMessage(String.format(SaveCommand.MESSAGE_SUCCESS, filePath));
    }
}
