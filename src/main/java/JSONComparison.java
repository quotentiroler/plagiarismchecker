
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gwt.thirdparty.guava.common.collect.MapDifference;
import com.google.gwt.thirdparty.guava.common.collect.Maps;

public class JSONComparison {

    private MapDifference<String, Object> difference;

    public JSONComparison(JsonElement leftJson, JsonElement rightJson)
            throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException, FileNotFoundException {

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();

        Map<String, Object> leftMap = gson.fromJson(leftJson.toString(), type);
        Map<String, Object> rightMap = gson.fromJson(rightJson.toString(), type);
        Map<String, Object> leftFlatMap = flatten(leftMap);
        Map<String, Object> rightFlatMap = flatten(rightMap);

        difference = Maps.difference(leftFlatMap, rightFlatMap);

        difference.entriesInCommon().forEach((k, v) -> {
            // System.out.println(k + v);
        });
    }

    public JSONComparison(Path srcDir)
            throws JsonIOException, JsonSyntaxException, IOException {

        File[] files = srcDir.toFile().listFiles();

        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory()) {
                JsonElement leftJson = JsonParser
                        .parseReader(new InputStreamReader(new FileInputStream(files[i].toString()),
                                "UTF-8"));
                for (int j = 0; j < files.length; j++) {
                    if (!files[j].isDirectory()) {
                        JsonElement rightJson = JsonParser
                                .parseReader(new InputStreamReader(
                                        new FileInputStream(files[j].toString()), "UTF-8"));
                        JSONComparison c = new JSONComparison(leftJson, rightJson);
                        Path dest = Paths.get(srcDir + "/out/" + files[i].getName() + "-evaluation.json");
                        if (!Files.exists(dest))
                            Files.write(dest,
                                    List.of(files[i].getName() + " compared to "
                                            + files[j].getName() + ": Total matches = "
                                            + c.getDifference().entriesInCommon().size()),
                                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                        else
                            Files.write(dest,
                                    List.of(files[i].getName() + " compared to "
                                            + files[j].getName() + ": Total matches = "
                                            + c.getDifference().entriesInCommon().size()
                                            + " Unique entries: " + (c.getDifference().entriesOnlyOnLeft().size()+c.getDifference().entriesOnlyOnRight().size())),
                                    StandardOpenOption.APPEND, StandardOpenOption.WRITE);

                        /*
                         * For details, run this code
                         * c.getDifference().entriesInCommon().forEach((k, v) -> {
                         * 
                         * 
                         * try {
                         * Files.write(dest, List.of(k + v),
                         * StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                         * 
                         * } catch (IOException e) {
                         * // TODO Auto-generated catch block
                         * e.printStackTrace();
                         * }
                         * });
                         */

                    }
                }
            }
        }
    }

    public MapDifference<String, Object> getDifference() {
        return difference;
    }

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return map.entrySet().stream()
                .flatMap(JSONComparison::flatten)
                .collect(LinkedHashMap::new, (m, e) -> m.put("/" + e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private static Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {

        if (entry == null) {
            return Stream.empty();
        }

        if (entry.getValue() instanceof Map<?, ?>) {
            return ((Map<?, ?>) entry.getValue()).entrySet().stream()
                    .flatMap(e -> flatten(
                            new AbstractMap.SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        if (entry.getValue() instanceof List<?>) {
            List<?> list = (List<?>) entry.getValue();
            return IntStream.range(0, list.size())
                    .mapToObj(i -> new AbstractMap.SimpleEntry<String, Object>(entry.getKey() + "/" + i, list.get(i)))
                    .flatMap(JSONComparison::flatten);
        }
        return Stream.of(entry);
    }

    public static void main(String[] args)
            throws JsonIOException, JsonSyntaxException, IOException {

        Path p = Paths.get("/mnt/c/Users/Max/Desktop/Plagiarism Task 1/Prepared/results/");

        for (File f : Paths.get(p + "/out").toFile().listFiles())
            f.delete();

        JSONComparison f = new JSONComparison(p);

    }

}
