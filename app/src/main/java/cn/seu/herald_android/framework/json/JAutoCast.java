package cn.seu.herald_android.framework.json;

public class JAutoCast {

    @SuppressWarnings("unchecked")
    public static <T> T autoCast(Object object) {

        // as int
        try {
            if (object == null) {
                return (T) (Integer) 0;
            }
            return (T) (Integer) object;
        } catch (ClassCastException e) {
        }

        // as long
        try {
            if (object == null) {
                return (T) (Long) 0L;
            }
            return (T) (Long) object;
        } catch (ClassCastException e) {
        }

        // as double
        try {
            if (object == null) {
                return (T) (Double) 0d;
            }
            return (T) (Double) object;
        } catch (ClassCastException e) {
        }


        // as float
        try {
            if (object == null) {
                return (T) (Float) 0f;
            }
            return (T) (Float) object;
        } catch (ClassCastException e) {
        }

        // as boolean
        try {
            if (object == null) {
                return (T) (Boolean) false;
            }
            return (T) (Boolean) object;
        } catch (ClassCastException e) {
        }

        // as string
        try {
            if (object == null) {
                return (T) (String) "";
            }
            return (T) (String) object;
        } catch (ClassCastException e) {
        }

        // as jarr
        try {
            if (object == null) {
                return (T) (JArr) new JArr();
            }
            return (T) (JArr) object;
        } catch (ClassCastException e) {
        }

        // as jobj
        try {
            if (object == null) {
                return (T) (JObj) new JObj();
            }
            return (T) (JObj) object;
        } catch (ClassCastException e) {
        }

        // as any object
        if (object == null) {
            return null;
        }

        // if not convertible to T, this will throw a ClassCastException from here
        return (T) object;
    }
}
