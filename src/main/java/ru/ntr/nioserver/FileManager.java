package ru.ntr.nioserver;

import java.util.List;

public interface FileManager {
    // ls root all - all files from server dir
    String getAllFilesFromServerDir();

    // ls root - files from server dir
    String getFilesFromServerDir();

    // ls cur all - all files from server dir
    String getAllFilesFromCurrentDir();

    // ls cur - files from current dir
    String getFilesFromCurrentDir();

    // cd - change dir
    boolean changeDir(String relPath);

    // cat - print file in console
    String readFile(String relPath);

    // touch -  create file
    boolean createFile(String relPath);

    // mkdir - create dir
    boolean createDir(String relPath);

    String getCurrentDir();
}
