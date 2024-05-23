package com.example.plagcheck.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.file.Files;
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
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gwt.dev.util.collect.HashSet;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import java.lang.reflect.Type;

@Slf4j
public class JSONComparison {

    private Map<String, Object> matches;
    private JSONObject matchesJSON;
    private int totalTokens;
    private String summary = "";

    public JSONComparison(JsonElement leftJson, JsonElement rightJson)
            throws JsonIOException, JsonSyntaxException, UnsupportedEncodingException, FileNotFoundException {

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();

        Map<String, Object> leftMap = gson.fromJson(leftJson.toString(), type);
        Map<String, Object> rightMap = gson.fromJson(rightJson.toString(), type);
        Map<String, Object> leftFlatMap = flatten(leftMap);
        Map<String, Object> rightFlatMap = flatten(rightMap);
        HashSet<String> values = new HashSet<>();
        rightFlatMap.forEach((k, v) -> values.add((String) v));
        matchesJSON = new JSONObject();

        leftFlatMap.forEach((k, v) -> {
            if (!values.add((String) v)) {
                matchesJSON.append((String) v, k);
            }
        });
        totalTokens = values.size();
        leftMap = gson.fromJson(matchesJSON.toString(), type);
        matches = flatten(leftMap);
    }

    
    public JSONComparison(JSONObject leftJson, JSONObject rightJson) {

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();

        Map<String, Object> leftMap = gson.fromJson(leftJson.toString(), type);
        Map<String, Object> rightMap = gson.fromJson(rightJson.toString(), type);
        Map<String, Object> leftFlatMap = flatten(leftMap);
        Map<String, Object> rightFlatMap = flatten(rightMap);

        HashSet<String> values = new HashSet<>();
        rightFlatMap.forEach((k, v) -> values.add((String) v));
        matchesJSON = new JSONObject();

        leftFlatMap.forEach((k, v) -> {
            if (!values.add((String) v)) {
                matchesJSON.append((String) v, k);
            }
        });
        totalTokens = values.size();
        leftMap = gson.fromJson(matchesJSON.toString(), type);
        matches = flatten(leftMap);
    }

    public JSONComparison(Path srcDir)
            throws JsonIOException, JsonSyntaxException, IOException {

        File[] files = srcDir.toFile().listFiles();

        for (int i = 0; i < files.length; i++) {
            if (!files[i].isDirectory()) {
                JSONObject leftJson = ExcludeFingerprints.parseJSONFile(files[i].toString());
                for (int j = 0; j < files.length; j++) {
                    if (!files[i].getName().equals(files[j].getName()))
                        if (!files[j].isDirectory()) {
                            JSONObject rightJson = ExcludeFingerprints.parseJSONFile(files[j].toString());
                            JSONComparison c = new JSONComparison(leftJson, rightJson);
                            new File(srcDir + "/out/").mkdirs();
                            Path dest = Paths.get(srcDir + "/out/" + files[i].getName().replace(".json", "-analysis") + ".txt");
                            int matches = c.getMatchesJSON().length();
                            int total = c.getTotalTokens();
                            String out = "";
                            if (!Files.exists(dest)) {
                                out = files[i].getName() + " compared to "
                                        + files[j].getName() + ": Total matches = "
                                        + matches
                                        + " Total entries: " + total
                                        + " Similarity: " + (float) matches / total;
                                Files.write(dest,
                                        List.of(out),
                                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                            } else {
                                out = files[i].getName() + " compared to "
                                        + files[j].getName() + ": Total matches = "
                                        + matches
                                        + " Total entries: " + total
                                        + " Similarity: " + (float) matches / total;
                                Files.write(dest,
                                        List.of(out),
                                        StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                            }
                            summary += out + "\n";

                            Files.write(dest, List.of(c.getMatchesJSON().toString()), StandardOpenOption.APPEND,
                                    StandardOpenOption.WRITE);

                        }
                }
            }
        }

        Files.write(Paths.get(srcDir + "/out/Summary.txt"), List.of(summary));
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public JSONObject getMatchesJSON() {
        return matchesJSON;
    }

    public Map<String, Object> getMatches() {
        return matches;
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
}
