package plagpack;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.util.ElementScanner14;

import org.junit.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.taskdefs.Length;
import org.apache.tools.ant.taskdefs.Sleep;

public class Plagiarism {

    static private JSONObject result;

    public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    private static void rec_processFiles(File[] files, Path targetPath) throws IOException {
        for (File file : files) {
            if (file.isDirectory() && file.getName().startsWith("task")) {
                var wrapper = new Object() {
                    File destDir = new File(
                            targetPath.toString() + "/" + file.getName() + "-evaluation.json");

                    File destDirDistinct = new File(
                            targetPath.toString() + "/Distinct/" + file.getName() + "-DistinctEvaluation.json");

                };
                result = new JSONObject();
                for (File codeFile : file.listFiles()) {
                    var wrapper2 = new Object() {
                        int line = 0;
                    };

                    List<String> name = Collections
                            .synchronizedList(new ArrayList<>(Arrays.asList(codeFile.getName() + ":")));
                    if (codeFile.getName().length() >= 2)
                        if (isInEnum(name.get(0).substring(name.get(0).indexOf(".") + 1, name.get(0).length() - 1),
                                FileExtensions.class)) {
                            System.out
                                    .println(name.get(0).substring(name.get(0).indexOf(".") + 1,
                                            name.get(0).length() - 1));

                            Stream<String> lines = Files.lines(Path.of(codeFile.getPath()));
                            lines.collect(Collectors.toCollection(() -> name))
                                    .forEach(c -> {
                                        wrapper2.line++;
                                        try {
                                            processTokens(c.split(" "), wrapper.destDir, codeFile.getName(),
                                                    wrapper2.line);
                                        } catch (IOException | InterruptedException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }

                                    });
                            lines.close();
                        }
                }
                result = NullRemover.removeNullsFrom(result);
                Files.write(wrapper.destDir.toPath(), List.of(result.toString()));

                distinctValues(result, wrapper.destDirDistinct.toPath());

            }
        }
    }

    private static void processTokens(String[] tokens, File destDir, String key, int line)
            throws IOException, InterruptedException {

        JSONArray tokensInLine = new JSONArray();
        for (String token : tokens) {
            token = token.trim();
            if (!isInEnum(token, Keywords.class) && !token.equals("") && token != null) {

                tokensInLine.put(token);

                /*
                 * * if (!token.equals("{") && !token.equals("}"))
                 * if (Files.exists(destDir.toPath())) {
                 * Files.write(destDir.toPath(), List.of(token), StandardCharsets.UTF_8,
                 * StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                 * Files.write(destDir.toPath(), List.of(result.toString()),
                 * StandardCharsets.UTF_8,
                 * StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                 * } else {
                 * Files.write(destDir.toPath(), List.of(token), StandardCharsets.UTF_8,
                 * StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 * 
                 * }
                 */

            }

        }

        result.append(key + ":" + line, tokensInLine);
    }

    private static JSONObject distinctValues(JSONObject object, Path targetDir) throws IOException, JSONException {

        Set<String> values = new HashSet<String>();
        Iterator<String> iterator = object.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            values.add(object.get(key).toString());

        }
        for (String value : values)
            if (!Files.exists(targetDir))
                Files.write(targetDir, List.of(value));
            else
                Files.write(targetDir, List.of(value), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
        return object;
        /*
         * List<String> lines;
         * lines =
         * Files.lines(Path.of(destDir.getPath())).distinct().collect(Collectors.toList(
         * ));
         * lines.forEach(c -> {
         * c.trim();
         * });
         * Files.write(destDir.toPath(), lines);
         */

    }

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

    public static void compareResults(Path resultDir) throws IOException {

        for (int i = 0; i < resultDir.toFile().listFiles().length; i++) {
            for (int j = 0; j < resultDir.toFile().listFiles().length; j++) {
                Files.write(Paths.get(resultDir.toFile().listFiles()[i].toString()),
                        List.of(resultDir.toFile().listFiles()[i].getName() + " compared to "
                                + resultDir.toFile().listFiles()[j].getName() + ":"),
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE);

                compareStreams(Files.lines(Path.of(resultDir.toFile().listFiles()[i].getPath())),
                        Files.lines(Path.of(resultDir.toFile().listFiles()[j].getPath())),
                        Paths.get(resultDir.toFile().listFiles()[i].toString()));

            }
        }
    }

    static <T> void compareStreams(Stream<?> s1, Stream<?> s2, Path resultDir) throws IOException {

        Comparator<String> comparator = String::compareTo;
        Iterator<?> iter1 = s1.iterator(), iter2 = s2.iterator();
        String matches = compareEntriesOfTwoStreams((Iterator<T>) iter1, (Iterator<T>) iter2,
                (Comparator<T>) comparator);
        Files.write(resultDir, List.of(matches), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String source = "/mnt/c/Users/Max/Desktop/Plagiarism Task 1/Prepared/";
        File srcDir = new File(source);
        Path targetDir = Paths.get(source + "results/");

        rec_processFiles(srcDir.listFiles(), targetDir);

        System.out.println("Wait 10 Seconds..");
        TimeUnit.SECONDS.sleep(10);

        Path distinctTargetDir = Paths.get(targetDir.toString() + "/Distinct");
        compareResults(distinctTargetDir);
    }

}
