package ru.ntr.nioserver;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.TreeSet;

@Log4j
@AllArgsConstructor
public class FileManagerImpl implements FileManager {

    private String serverDir;

    private String currentDir;

    public FileManagerImpl(String serverDir) {
        this(serverDir, "");
    }

    @Override
    public String getCurrentDir() {
        return currentDir;
    }

    private String getAllFilesFromDir(String rootDir, boolean noDepthWalk) {
        final Path rootDirPath = Paths.get(rootDir);
        final TreeSet<String> paths = new TreeSet<>();

        try {
            Files.walkFileTree(rootDirPath, EnumSet.noneOf(FileVisitOption.class),
                    noDepthWalk ? 1 : Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            paths.add(Paths.get(serverDir).relativize(dir).toString());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            paths.add(Paths.get(serverDir).relativize(file).toString());
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            log.error("Error during walking file tree: ", e);

        }

        StringBuilder sb = new StringBuilder();
        paths.forEach(e -> sb.append(e).append("\n\r"));

        return sb.toString();
    }


    // ls root all - all files from server dir
    @Override
    public String getAllFilesFromServerDir() {
        return getAllFilesFromDir(serverDir, false);
    }

    // ls root - files from server dir
    @Override
    public String getFilesFromServerDir() {
        return getAllFilesFromDir(serverDir, true);
    }

    // ls cur all - all files from server dir
    @Override
    public String getAllFilesFromCurrentDir() {
        return getAllFilesFromDir(serverDir + File.separator + currentDir, false);
    }

    // ls cur - files from current dir
    @Override
    public String getFilesFromCurrentDir() {
        return getAllFilesFromDir(serverDir + File.separator + currentDir, true);
    }

    // cd - change dir
    @Override
    public boolean changeDir(String relPath) {
        Path path = Paths.get(serverDir, currentDir, relPath).normalize();

        if (isIllegalAccess(path)) {
            log.warn("Illegal access attempt!");
            return false;
        }


        if (Files.exists(path)) {
            currentDir = Paths.get(serverDir).relativize(path).toString();
            log.info("Current dir: " + currentDir);
            return true;
        } else {
            log.info(String.format("Directory %s does not exist", path.toString()));
            return false;
        }
    }

    // cat - print file in console
    @Override
    public String readFile(String relPath) {
        Path path = Paths.get(serverDir, currentDir, relPath).normalize();

        if (isIllegalAccess(path)) {
            log.warn("Illegal access attempt!");
            return "Access denied!\r\n";
        }

        try {
            return new String(Files.readAllBytes(path)) + "\r\n";
        } catch (IOException e) {
            log.error("Error has occurred during reading file " + getCurrentDir() + File.separator + relPath, e);
        }
        return null;
    }

    // touch -  create file
    @Override
    public boolean createFile(String relPath) {
        try {
            Path path = Paths.get(serverDir, currentDir, relPath).normalize();

            if (isIllegalAccess(path)) {
                log.warn("Illegal access attempt!");
                return false;
            }

            Files.createFile(path);
            log.info("File was created successfully!");
            return true;
        } catch (IOException e) {
            log.error("Failed to create file" + relPath + ": ", e);
            return false;
        }
    }

    // mkdir - create dir
    @Override
    public boolean createDir(String relPath) {
        try {

            Path path = Paths.get(serverDir, currentDir, relPath).normalize();

            if (isIllegalAccess(path)) {
                log.warn("Illegal access attempt!");
                return false;
            }

            Files.createDirectories(path);
            log.info("Directory was created successfully!");
            return true;
        } catch (IOException e) {
            log.error("Failed to create directory: ", e);
            return false;
        }
    }

    private boolean isIllegalAccess(Path path) {
        return serverDir.startsWith(path.toString());
    }

}
