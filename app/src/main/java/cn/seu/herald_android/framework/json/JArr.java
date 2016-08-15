package cn.seu.herald_android.framework.json;

import android.support.annotation.Nullable;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JArr {

    private final List<Object> values;

    public JArr() {
        values = new ArrayList<Object>();
    }

    public JArr(Collection copyFrom) {
        this();
        if (copyFrom != null) {
            for (Object aCopyFrom : copyFrom) {
                put(JObj.wrap(aCopyFrom));
            }
        }
    }

    public JArr(JTokener readFrom) {
        List<Object> values1 = new ArrayList<>();
        try {
            Object object = readFrom.nextValue();
            if (object instanceof JArr) {
                values1 = ((JArr) object).values;
            }
        } catch (JSONException e) {
        }
        values = values1;
    }

    public JArr(String json) {
        this(new JTokener(json));
    }

    public JArr(Object array) {
        if (array.getClass().isArray()) {
            values = new ArrayList<>();
        } else {
            final int length = Array.getLength(array);
            values = new ArrayList<Object>(length);
            for (int i = 0; i < length; ++i) {
                put(JObj.wrap(Array.get(array, i)));
            }
        }
    }

    public int size() {
        return values.size();
    }

    public <T> void put(T value) {
        put(size(), value);
    }

    public <T> void put(int index, T value) {
        if (index < 0) return;
        while (values.size() <= index) {
            values.add(null);
        }
        values.set(index, value);
    }

    @Nullable
    public Object remove(int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.remove(index);
    }

    public boolean isNull(int index) {
        Object value = $(index);
        return value == null || value == JObj.NULL;
    }

    @Nullable
    public Object $(int index) {
        try {
            Object value = values.get(index);
            if (value == null) {
                return null;
            }
            return value;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public boolean $b(int index) {
        Object object = $(index);
        Boolean result = JUtil.toBoolean(object);
        if (result == null) {
            return false;
        }
        return result;
    }

    public double $d(int index) {
        Object object = $(index);
        Double result = JUtil.toDouble(object);
        if (result == null) {
            return 0;
        }
        return result;
    }

    public int $i(int index) {
        Object object = $(index);
        Integer result = JUtil.toInteger(object);
        if (result == null) {
            return 0;
        }
        return result;
    }

    public long $l(int index) {
        Object object = $(index);
        Long result = JUtil.toLong(object);
        if (result == null) {
            return 0L;
        }
        return result;
    }

    public String $s(int index) {
        Object object = $(index);
        String result = JUtil.toString(object);
        if (result == null) {
            return "";
        }
        return result;
    }

    public JArr $a(int index) {
        Object object = $(index);
        if (object != null && object instanceof JArr) {
            return (JArr) object;
        } else {
            return new JArr();
        }
    }

    public JObj $o(int index) {
        Object object = $(index);
        if (object != null && object instanceof JObj) {
            return (JObj) object;
        } else {
            return new JObj();
        }
    }

    @Override
    public String toString() {
        try {
            JStringer stringer = new JStringer();
            writeTo(stringer);
            return stringer.toString();
        } catch (JSONException e) {
            return null;
        }
    }

    public void writeTo(JStringer stringer) throws JSONException {
        stringer.array();
        for (Object value : values) {
            stringer.value(value);
        }
        stringer.endArray();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof JArr && ((JArr) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
