package com.example.plagcheck.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class CompareStreams {


    public static <T> String compareEntriesOfTwoStreams(Iterator<T> sourceOne, Iterator<T> sourceTwo,
            Comparator<T> comparator) {
        int counter = 0;
        String matches = "Start matching..\n";
        T valueInOne = sourceOne != null ? sourceOne.hasNext() ? sourceOne.next() : null : null;
        T valueInTwo = sourceTwo != null ? sourceTwo.hasNext() ? sourceTwo.next() : null : null;
        while (valueInOne != null && valueInTwo != null) {

            if (comparator.compare(valueInOne, valueInTwo) > 0) {
                // advance sourcetwo
                while (valueInTwo != null && comparator.compare(valueInOne, valueInTwo) > 0) {
                    // System.out.println("Not present in list 1, Present in list 2: " +
                    // valueInTwo);
                    valueInTwo = sourceTwo.hasNext() ? sourceTwo.next() : null;
                }

            } else if (comparator.compare(valueInOne, valueInTwo) < 0) {
                // advance sourceone
                while (valueInOne != null && comparator.compare(valueInOne, valueInTwo) < 0) {
                    // this will advance
                    // System.out.println("Not present in list 2, Present in list 1: " +
                    // valueInOne);
                    valueInOne = sourceOne.hasNext() ? sourceOne.next() : null;
                }

            } else if (comparator.compare(valueInOne, valueInTwo) == 0) {
                // System.out.println("present in both list:" + valueInOne);
                counter++;
                matches += ("Match: " + valueInOne + "\n");
                valueInTwo = sourceTwo.hasNext() ? sourceTwo.next() : null;
                valueInOne = sourceOne.hasNext() ? sourceOne.next() : null;
                // present in both list if one of list is ended
            }
        }
        return matches + "Counter: " + counter + "\n";
    }

    public static void compareAllFilesInDir(Path resultDir) throws IOException {

        for (int i = 0; i < resultDir.toFile().listFiles().length; i++) {
            for (int j = 0; j < resultDir.toFile().listFiles().length; j++) {
                Files.write(Paths.get(resultDir.toFile().listFiles()[i].toString()),
                        List.of(resultDir.toFile().listFiles()[i].getName() + " compared to "
                                + resultDir.toFile().listFiles()[j].getName() + ":"),
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE);

                        compareStreams(
                            Path.of(resultDir.toFile().listFiles()[i].getPath()),
                            Path.of(resultDir.toFile().listFiles()[j].getPath()),
                            Paths.get(resultDir.toFile().listFiles()[i].toString())
                        );

            }
        }
    }
    static <T> void compareStreams(Path path1, Path path2, Path resultDir) throws IOException {
        Comparator<String> comparator = String::compareTo;
    
        try (Stream<String> s1 = Files.lines(path1);
             Stream<String> s2 = Files.lines(path2)) {
            Iterator<?> iter1 = s1.iterator(), iter2 = s2.iterator();
            String matches = compareEntriesOfTwoStreams((Iterator<T>) iter1, (Iterator<T>) iter2,
                    (Comparator<T>) comparator);
            Files.write(resultDir, List.of(matches), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        }
    }

}
