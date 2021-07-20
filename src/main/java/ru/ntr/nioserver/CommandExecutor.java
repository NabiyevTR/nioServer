package ru.ntr.nioserver;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.io.File;

import static ru.ntr.nioserver.Command.*;

@Log4j
@AllArgsConstructor
public class CommandExecutor {

    private FileManager fileManager;
    private static final String WRONG_COMMAND = "Wrong command.\r\n";

    public String execute(String command) {

        String cmd = command.toLowerCase().trim().replaceAll(" +", " ");

        try {
            log.info("Command received: " + cmd);

            if (cmd.startsWith(CHANGE_DIR.getCommand())) {
                String newDir = cmd.split(CHANGE_DIR.getCommand())[1].trim();
                if (fileManager.changeDir(newDir)) {
                    return "Current dir changed to: " + fileManager.getCurrentDir() + "\r\n";
                } else {
                    return "Cannot change current dir.\r\n";
                }
            }


            if (cmd.startsWith(CREATE_DIR.getCommand())) {
                String[] ddd = cmd.split(CREATE_DIR.getCommand());
                String relPath = cmd.split(CREATE_DIR.getCommand())[1].trim();
                if (fileManager.createDir(relPath)) {
                    return String.format("Dir %s was created.\r\n",
                            fileManager.getCurrentDir() + File.separator + relPath);
                } else {
                    String.format("Dir %s was not created.\r\n",
                            fileManager.getCurrentDir() + File.separator + relPath);
                }
            }

            if (cmd.startsWith(CREATE_FILE.getCommand())) {
                String relPath = cmd.split(CREATE_FILE.getCommand())[1].trim();
                if (fileManager.createFile(relPath)) {
                    return String.format("File %s was created\r\n",
                            fileManager.getCurrentDir() + File.separator + relPath);
                } else {
                    return String.format("File %s was not created\r\n",
                            fileManager.getCurrentDir() + File.separator + relPath);
                }
            }

            if (cmd.equals(GET_ALL_FILES_FROM_SERVER_DIR.getCommand())) {
                return fileManager.getAllFilesFromServerDir();
            }

            if (cmd.equals(GET_FILES_FROM_SERVER_DIR.getCommand())) {
                return fileManager.getFilesFromServerDir();
            }


            if (cmd.equals(GET_ALL_FILES_FROM_CURRENT_DIR.getCommand())) {
                return fileManager.getAllFilesFromCurrentDir();
            }

            if (cmd.equals(GET_FILES_FROM_CURRENT_DIR.getCommand())) {
                return fileManager.getFilesFromCurrentDir();
            }

            if (cmd.startsWith(PRINT_FILE.getCommand())) {
                String relPath = cmd.split(PRINT_FILE.getCommand())[1].trim();
                return fileManager.readFile(relPath);
            }

            if (cmd.equals("?") || cmd.equals("help")) {
                return getAllCmdAndDescription();
            }

            return WRONG_COMMAND;

        } catch (Exception e) {
            log.error("Error during execution command: " + cmd, e);
            return "Error during execution command: " + cmd + "\r\n";
        }

    }
}
