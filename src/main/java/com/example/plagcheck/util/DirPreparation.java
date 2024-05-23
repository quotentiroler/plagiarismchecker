package com.example.plagcheck.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;

public class DirPreparation {

    public static void processFiles(Path path) {
        Path destDir = path.resolve("tempx");
        if (!Files.exists(destDir)) {
            try {
                Files.createDirectories(destDir);
            } catch (IOException e) {
                // Handle exception
                e.printStackTrace();
            }
        }
        File file = path.toFile();
        if (file.isDirectory()) {
            rec_processFiles(file.listFiles(), destDir);
        }
        deleteAllExceptTempx(path);

        moveFromTempxToParent(path);

    }

    // Recursively go through each given file from File Array, and collect all files
    // with "source" in their name saving them to the destination path
    public static void rec_processFiles(File[] files, Path destDir) {
        for (File file : files) {
            if (file.isDirectory()) {
                rec_processFiles(file.listFiles(), destDir); // Recursive call for directories
            } else if (file.getPath().replace("\\", "/").contains("/src/")) {
                try {
                    Files.copy(file.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void deleteAllExceptTempx(Path path) {
        File dir = path.toFile();
        for (File file : dir.listFiles()) {
            if (!file.getName().equals("tempx")) {
                try {
                    FileUtils.forceDelete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Move all files from tempx to parent directory
    public static void moveFromTempxToParent(Path tempx) {
        File tempxDir = tempx.resolve("tempx").toFile();
        File parentDir = tempx.toFile();
        for (File file : tempxDir.listFiles()) {
            try {
                Files.move(file.toPath(), parentDir.toPath().resolve(file.getName()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // delete tempx directory
        try {
            FileUtils.forceDelete(tempxDir);
        } catch (IOException e) {
            e.printStackTrace();

        }

    }
}