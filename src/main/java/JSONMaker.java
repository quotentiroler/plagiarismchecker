
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import java.util.List;

import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONMaker {

    static private JSONObject result;

    public JSONMaker(Path srcPath, String targetPath) throws IOException {
        result = new JSONObject();

        for (File codeFile : srcPath.toFile().listFiles()) {

            var wrapper2 = new Object() {
                int line = 0;
                JSONArray jsonArray;
                HashSet<String> allTokens = new HashSet<>();
                JSONObject fileJSON = new JSONObject();
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
                    lines.forEach(c -> {
                        wrapper2.jsonArray = new JSONArray();
                        wrapper2.line++;
                        for (String token : c.split(" ")) {
                            if (wrapper2.allTokens.add(token.trim()) && !isInEnum(token, Keywords.class)) {
                                wrapper2.jsonArray.put(token.trim());
                            }
                        }
                        if (!wrapper2.jsonArray.isEmpty())
                            wrapper2.fileJSON.append(wrapper2.line + "", wrapper2.jsonArray);

                    });
                    lines.close();
                }

            result.append(codeFile.getName(), wrapper2.fileJSON);
            Files.write(Paths.get(targetPath), List.of(result.toString()));
        }
    }

    public JSONObject getResult() {
        return result;
    }

    public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public static void runThis(Path p) throws IOException {

        JSONMaker j;
        new File(p + "/results/").mkdirs();
        for (File f : p.toFile().listFiles())
            if (f.getName().startsWith("task"))
                j = new JSONMaker(f.toPath(), p.toString() + "/results/" + f.getName() + ".json");

    }

    public static void main(String[] args) throws IOException {

        Path src = Paths.get("/mnt/c/Users/Max/Desktop/Plagiarism Task 1/Prepared/");
        runThis(src);

    }

}
