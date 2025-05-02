package at.aau.serg.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Utils {
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static String[] convertToStringArray(Object value) {
        if (value instanceof String[]) {
            return (String[]) value;
        } else if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.stream().map(Object::toString).toArray(String[]::new);
        } else {
            return new String[]{value.toString()};
        }
    }
}

