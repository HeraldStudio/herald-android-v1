package cn.seu.herald_android.custom;

import java.util.Calendar;

public class CalendarUtils {
    public static Calendar toSharpDay(Calendar src){
        src.set(Calendar.HOUR_OF_DAY, 0);
        src.set(Calendar.MINUTE, 0);
        src.set(Calendar.SECOND, 0);
        src.set(Calendar.MILLISECOND, 0);
        return src;
    }
}
