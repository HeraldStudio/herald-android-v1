package cn.seu.herald_android.custom;

import java.util.Calendar;

public class CalendarUtils {

    public static final long ONE_DAY = 1000 * 60 * 60 * 24;

    public static Calendar toSharpDay(Calendar src) {
        // 复制这个Calendar
        Calendar dst = Calendar.getInstance();
        dst.setTimeInMillis(src.getTimeInMillis());

        dst.set(Calendar.HOUR_OF_DAY, 0);
        dst.set(Calendar.MINUTE, 0);
        dst.set(Calendar.SECOND, 0);
        dst.set(Calendar.MILLISECOND, 0);
        return dst;
    }

    public static Calendar toSharpWeek(Calendar src) {
        // 复制这个Calendar并转换为整数日
        Calendar dst = toSharpDay(src);

        while (dst.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            dst.setTimeInMillis(dst.getTimeInMillis() - ONE_DAY);
        }
        return dst;
    }

    public static String formatHourMinuteStamp(int hourMinute) {
        int hour = hourMinute / 60;
        int minute = hourMinute % 60;
        return hour + (minute < 10 ? ":0" : ":") + minute;
    }
}
