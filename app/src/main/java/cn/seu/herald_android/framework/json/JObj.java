package cn.seu.herald_android.framework.json;

import android.support.annotation.Nullable;

import org.json.JSONException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JObj {

    private static final Double NEGATIVE_ZERO = -0d;

    public static final Object NULL = new Object() {
        @Override
        public boolean equals(Object o) {
            return o == this || o == null; // API specifies this broken equals implementation
        }

        @Override
        public String toString() {
            return "null";
        }
    };

    private final LinkedHashMap<String, Object> nameValuePairs;

    public JObj() {
        nameValuePairs = new LinkedHashMap<String, Object>();
    }

    public JObj(Map copyFrom) {
        this();
        if (copyFrom != null) {
            Map<?, ?> contentsTyped = (Map<?, ?>) copyFrom;
            for (Map.Entry<?, ?> entry : contentsTyped.entrySet()) {
                String key = (String) entry.getKey();
                if (key != null) {
                    nameValuePairs.put(key, wrap(entry.getValue()));
                }
            }
        }
    }

    public JObj(JTokener readFrom) {
        LinkedHashMap<String, Object> nameValuePairs1 = new LinkedHashMap<>();
        try {
            Object object = readFrom.nextValue();
            if (object instanceof JObj) {
                nameValuePairs1 = ((JObj) object).nameValuePairs;
            }
        } catch (JSONException e) {
        }
        nameValuePairs = nameValuePairs1;
    }

    public JObj(String json) {
        this(new JTokener(json));
    }

    public int size() {
        return nameValuePairs.size();
    }

    public <T> void put(String name, T value) {
        if (name == null) return;
        if (value == null) {
            nameValuePairs.remove(name);
        } else {
            nameValuePairs.put(name, value);
        }
    }

    @Nullable
    public Object remove(String name) {
        Object result = nameValuePairs.remove(name);
        return result;
    }

    public boolean isNull(String name) {
        Object value = nameValuePairs.get(name);
        return value == null || value == NULL;
    }

    public boolean has(String name) {
        return nameValuePairs.containsKey(name);
    }

    @Nullable
    public Object $(String name) {
        Object result = nameValuePairs.get(name);
        if (result == null) {
            return null;
        }
        return result;
    }

    public boolean $b(String name) {
        Object object = $(name);
        Boolean result = JUtil.toBoolean(object);
        if (result == null) {
            return false;
        }
        return result;
    }

    public double $d(String name) {
        Object object = $(name);
        Double result = JUtil.toDouble(object);
        if (result == null) {
            return 0;
        }
        return result;
    }

    public int $i(String name) {
        Object object = $(name);
        Integer result = JUtil.toInteger(object);
        if (result == null) {
            return 0;
        }
        return result;
    }

    public long $l(String name) {
        Object object = $(name);
        Long result = JUtil.toLong(object);
        if (result == null) {
            return 0L;
        }
        return result;
    }

    public String $s(String name) {
        Object object = $(name);
        String result = JUtil.toString(object);
        if (result == null) {
            return "";
        }
        return result;
    }

    public JArr $a(String name) {
        Object object = $(name);
        if (object != null && object instanceof JArr) {
            return (JArr) object;
        } else {
            return new JArr();
        }
    }

    public JObj $o(String name) {
        Object object = $(name);
        if (object != null && object instanceof JObj) {
            return (JObj) object;
        } else {
            return new JObj();
        }
    }

    public Set<String> keySet() {
        return nameValuePairs.keySet();
    }

    @Override
    public String toString() {
        try {
            JStringer stringer = new JStringer();
            writeTo(stringer);
            return stringer.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    public void writeTo(JStringer stringer) throws JSONException {
        stringer.object();
        for (Map.Entry<String, Object> entry : nameValuePairs.entrySet()) {
            stringer.key(entry.getKey()).value(entry.getValue());
        }
        stringer.endObject();
    }

    public static Object wrap(Object o) {
        if (o == null) {
            return NULL;
        }
        if (o instanceof JArr || o instanceof JObj) {
            return o;
        }
        if (o.equals(NULL)) {
            return o;
        }
        try {
            if (o instanceof Collection) {
                return new JArr((Collection) o);
            } else if (o.getClass().isArray()) {
                return new JArr(o);
            }
            if (o instanceof Map) {
                return new JObj((Map) o);
            }
            if (o instanceof Boolean ||
                    o instanceof Byte ||
                    o instanceof Character ||
                    o instanceof Double ||
                    o instanceof Float ||
                    o instanceof Integer ||
                    o instanceof Long ||
                    o instanceof Short ||
                    o instanceof String) {
                return o;
            }
            if (o.getClass().getPackage().getName().startsWith("java.")) {
                return o.toString();
            }
        } catch (Exception ignored) {
        }
        return NULL;
    }
}
