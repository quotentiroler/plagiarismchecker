package com.example.plagcheck.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExcludeFingerprints {

    private JSONObject result;

    public ExcludeFingerprints(JSONObject fingerPrints, JSONObject object) {

        result = new JSONObject(object.toString());

        JSONComparison c = new JSONComparison(object, fingerPrints);

        var wrapper = new Object() {
            int i = 0;

        };

        c.getMatches().forEach((k, v) -> {
            String f = k;
            k = f.substring(f.indexOf("/")+1, f.lastIndexOf("/"));
            String[] loc = ((String) v).split("/");
            wrapper.i = indexOf(result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0), k);

        });
        result = removeNullsFrom(result);
    }

    public ExcludeFingerprints(Path fp, Path jsonFile) throws IOException {

        JSONObject fingerPrints = parseJSONFile(fp.toString());
        JSONObject object = parseJSONFile(jsonFile.toString());

        result = parseJSONFile(jsonFile.toString());

        JSONComparison c = new JSONComparison(object, fingerPrints);

        c.getMatches().forEach((k, v) -> {
            k = k.split("/")[1];
            String[] loc = ((String) v).split("/");
            int i = indexOf(result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0), k);
            result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0)
                    .remove(i);
        });
        result = removeNullsFrom(result);

    }

    private int indexOf(JSONArray array, String s) {
        for (int i = 0; i < array.length(); i++) {
            String entry = (String) array.get(i);
            if (s.equals(entry))
                return i;
        }
        return -1;

    }

    public static JSONObject removeNullsFrom(JSONObject object) throws JSONException {
        if (object != null) {
            Iterator<String> iterator = object.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object o = object.get(key);
                if (o.toString().trim().equals("[[]]") || o.toString().trim().equals("[[\"\"]]")) {
                    iterator.remove();
                } else {
                    removeNullsFrom(o);
                }
            }
            return object;
        }
        return null;
    }

    public static void removeNullsFrom(Object o) throws JSONException {
        if (o instanceof JSONObject) {
            removeNullsFrom((JSONObject) o);
        } else if (o instanceof JSONArray) {
            removeNullsFrom((JSONArray) o);
        }
    }

    public static void removeNullsFrom(JSONArray array) throws JSONException {
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                Object o = array.get(i);
                if (o == null || o == JSONObject.NULL) {
                    array.remove(i);
                } else {
                    removeNullsFrom(o);
                }
            }
        }
    }

    public JSONObject getResult() {
        return result;
    }

    public static JSONObject parseJSONFile(String filename) throws JSONException, IOException {
        String content = new String(Files.readString(Paths.get(filename)));
        return new JSONObject(content);
    }

    public static void letsGo(Path pathOfJson, Path pathOfDir) throws JSONException, IOException, InterruptedException {

        JSONObject o1 = parseJSONFile(pathOfJson.toString());

        JSONObject o2;

        new File(pathOfDir + "/fpExcluded/").mkdirs();

        for (File f : pathOfDir.toFile().listFiles())
            if (!f.isDirectory() && f.getName().endsWith(".json")) {
                o2 = parseJSONFile(f.toString());
                ExcludeFingerprints ef = new ExcludeFingerprints(o1, o2);
                Path dest = Paths.get(f.getParent() + "/fpExcluded/" + f.getName());
                Files.write(dest, List.of(ef.getResult().toString()));
            }
    }
}
