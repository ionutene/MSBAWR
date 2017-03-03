package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilesAndDirectoryUtil {

    private static final Logger LOGGER = LogManager.getLogger(FilesAndDirectoryUtil.class);

    public static List<Path> findFilesInPathWithPattern(String pathToSearch, String pattern) throws IOException {
        List<Path> filesFound = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream =
                     Files.newDirectoryStream(FileSystems.getDefault().getPath(pathToSearch), pattern)) {
            for (Path file : directoryStream) {
//                LOGGER.info(file.getFileName());
                filesFound.add(file);
            }
        }
        return filesFound;
    }

    public static boolean findFileInPath(String fileToFind, String pathToSearch) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(FileSystems.getDefault().getPath(pathToSearch))) {
            for (Path file : ds) {
//                LOGGER.info(file.getFileName());
                if (fileToFind.equals(file.getFileName().toString()))
                    return true;
            }
        }
        return false;
    }

    public static void deleteDirectoryContents(String directoryPath) throws IOException {
        Files.walkFileTree(FileSystems.getDefault().getPath(directoryPath), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (!FileSystems.getDefault().getPath(directoryPath).equals(dir)) {
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void deleteDirectoryContents(Path directoryPath) throws IOException {
        Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void createDirectory(String directoryPath) throws IOException {
        Files.createDirectory(FileSystems.getDefault().getPath(directoryPath));
    }

    public static void moveDirectory(Path sourceDirectory, Path targetDirectory) throws IOException {
        Files.move(sourceDirectory, targetDirectory, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void moveDirectory(String sourceDirectory, String targetDirectory) throws IOException {
        Files.move(FileSystems.getDefault().getPath(sourceDirectory),
                FileSystems.getDefault().getPath(targetDirectory),
                StandardCopyOption.REPLACE_EXISTING);
    }

}
