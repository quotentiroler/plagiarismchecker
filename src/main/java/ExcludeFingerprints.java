import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.tools.ant.taskdefs.Sleep;
import org.apache.tools.ant.taskdefs.WaitFor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ExcludeFingerprints {

    private JSONObject result;

    public ExcludeFingerprints(Path fp, Path jsonFile) throws IOException {

        JSONObject fingerPrints = new JSONMaker(fp).getResult();
        JSONObject object = new JSONMaker(jsonFile).getResult();

        result = new JSONMaker(jsonFile).getResult();

        JSONComparison c = new JSONComparison(object, fingerPrints);
        System.out.println(c.getMatches().size());
        c.getMatches().forEach((k, v) -> {
            String[] loc = k.split("/");
            result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0)
                    .remove(Integer.parseInt(loc[5]));
        });

        JSONComparison b = new JSONComparison(fingerPrints, object);
        b.getMatches().forEach((k, v) -> {
            String[] loc = k.split("/");
            result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0)
                    .remove(Integer.parseInt(loc[5]));
        });
        result = removeNullsFrom(result);

    }

    public ExcludeFingerprints(JSONObject fingerPrints, JSONObject object) {

        result = new JSONObject(object.toString());

        JSONComparison c = new JSONComparison(object, fingerPrints);
        System.out.println(c.getMatches().size());
        c.getMatches().forEach((k, v) -> {
            String[] loc = k.split("/");
            result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0)
                    .remove(Integer.parseInt(loc[5]));
        });

        JSONComparison b = new JSONComparison(fingerPrints, object);
        b.getMatches().forEach((k, v) -> {
            String[] loc = k.split("/");
            result.getJSONArray(loc[1]).getJSONObject(0).getJSONArray(loc[3]).getJSONArray(0)
                    .remove(Integer.parseInt(loc[5]));
        });

        result = removeNullsFrom(result);
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
        ExcludeFingerprints ff;
        new File(pathOfDir + "/fpExcluded/").mkdirs();

        for (File f : pathOfDir.toFile().listFiles())
            if (!f.isDirectory() && f.getName().endsWith(".json")) {
                o2 = parseJSONFile(f.toString());
                ExcludeFingerprints ef = new ExcludeFingerprints(o1, o2);
                Path dest = Paths.get(f.getParent() + "/fpExcluded/" + f.getName());
                Files.write(dest, List.of(ef.getResult().toString()));
                TimeUnit.SECONDS.sleep(1);
               // ff = new ExcludeFingerprints(pathOfJson, dest);
            }
    }

    public static void main(String[] args) throws IOException, JSONException, InterruptedException {

        Path p1 = Paths.get("/mnt/c/Users/Max/Desktop/Plagiarism Task 1/Prepared/fpToExclude.json");
        Path p2 = Paths.get("/mnt/c/Users/Max/Desktop/Plagiarism Task 1/Prepared/results/");

        letsGo(p1,p2);
        



    }
}
