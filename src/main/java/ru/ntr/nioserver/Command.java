package ru.ntr.nioserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
public enum Command {
    CREATE_DIR("mkdir", "create dir"),
    CHANGE_DIR("cd", "change dir"),
    GET_ALL_FILES_FROM_SERVER_DIR("ls root all", "get all files from root dir"),
    GET_FILES_FROM_SERVER_DIR("ls root", "get files in root dir"),
    GET_ALL_FILES_FROM_CURRENT_DIR("ls cur all", "get all files from current dir"),
    GET_FILES_FROM_CURRENT_DIR("ls cur", "get files in current directory"),
    CREATE_FILE("touch", "create new file"),
    PRINT_FILE("cat", "print file in console"),
    HELP("help", "show help");

    @Getter
    private String command;
    @Getter
    private String description;


    public String getCmdAndDescription() {
        return command + " - " + description;
    }

    public static String getAllCmdAndDescription() {

        StringBuilder sb = new StringBuilder();

        for (Command day : Command.values()) {
            sb.append(day.getCmdAndDescription());
            sb.append("\r\n");
        }

        return  sb.toString();
    }


}
