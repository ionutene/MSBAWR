package util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilesAndDirectoryUtil {

    public static List<Path> findFilesInPathWithPattern(String pathToSearch, String pattern) throws IOException {
        List<Path> filesFound = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(pathToSearch), pattern)) {
            for (Path file : ds) {
                filesFound.add(file);
            }
        }
        return filesFound;
    }

    public static boolean findFileInPath(String fileToFind, String pathToSearch) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get(pathToSearch))) {
            for (Path file : ds) {
//                LOGGER.info(file.getFileName());
                if (fileToFind.equals(file.getFileName().toString()))
                    return true;
            }
        }
        return false;
    }

    public static void deleteDirectory(String directoryPath) throws IOException {
        Files.walkFileTree(Paths.get(directoryPath), new SimpleFileVisitor<Path>() {
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
    
}
