package cn.seu.herald_android.helper;

import android.text.style.TextAppearanceSpan;
import android.util.Log;

/**
 * 为了输出log方便, 特意定义该类
 * 使用时:
 *      import static ***.makeLogTag;
 *      import static ***.LOGD;
 * Created by corvo on 8/3/16.
 */
public class LogUtils {
    private static final String LOG_PREFIX = "HOU_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static boolean LOGGING_ENABLED = true;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH);
        }
        return  LOG_PREFIX + str;
    }

    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void LOGD(final String tag, String message) {
        if (LOGGING_ENABLED) {
            //if (Log.isLoggable(tag, Log.DEBUG)) {     // google 后期支持, 适合单元调试, 暂不使用
                Log.d(tag, message);
            //}
        }
    }


    public static void LOGD(final String tag, String message, Throwable cause) {
        //if (LOGGING_ENABLED) {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message, cause);
            }
        //}
    }

    public static void LOGV(final String tag, String message) {
        if (LOGGING_ENABLED) {
            //if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message);
            //}
        }
    }

    public static void LOGV(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            //if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message, cause);
            //}
        }
    }

    public static void LOGI(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message);
        }
    }

    public static void LOGI(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.i(tag, message, cause);
        }
    }

    public static void LOGW(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message);
        }
    }
    public static void LOGW(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.w(tag, message, cause);
        }
    }

    public static void LOGE(final String tag, String message) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message);
        }
    }

    public static void LOGE(final String tag, String message, Throwable cause) {
        if (LOGGING_ENABLED) {
            Log.e(tag, message, cause);
        }
    }
}
