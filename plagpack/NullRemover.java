package plagpack;

import java.util.Iterator;

import javax.xml.stream.events.EndElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NullRemover {
    public static JSONObject removeNullsFrom( JSONObject object) throws JSONException {
        if (object != null) {
            Iterator<String> iterator = object.keys();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object o = object.get(key);
                if (o.toString().trim().equals("[[]]")) {
                    iterator.remove();
                } else {
                    removeNullsFrom(o);
                }
            }
            return object;
        }
        return null;
    }

    public static void removeNullsFrom( JSONArray array) throws JSONException {
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

    public static void removeNullsFrom(Object o) throws JSONException {
        if (o instanceof JSONObject) {
            removeNullsFrom((JSONObject) o);
        } else if (o instanceof JSONArray) {
            removeNullsFrom((JSONArray) o);
        }
    }
}