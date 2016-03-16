package cn.seu.herald_android.mod_query.pedetail;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;

/**
 * 表示单个早操数据的类
 */
class ExerciseInfo {

    // 打卡时间（年、月、日、时、分）
    private Calendar dateTime;
    // 比较器，用于在主程序中对时间进行排序，以便找出首页和末页的月份值，以及保证日期的顺序关系是正确的
    public static Comparator<ExerciseInfo> yearMonthComparator = (e1, e2) -> {
        long l1 = e1.getDateTime().getTimeInMillis();
        long l2 = e2.getDateTime().getTimeInMillis();
        if (l1 == l2) return 0;
        return l1 > l2 ? 1 : -1;
    };
    // 单独表示年月（年*12+自然月-1）
    private int yearMonth;
    // 打卡数据是否有效（无效数据不会显示，目前暂不做其他处理）
    private boolean isValid;
    // 表示到该次为止的总跑操次数
    private int exerciseCount;

    public ExerciseInfo(JSONObject obj, int count) {
        try {
            // 识别年月日，比较保险的方法（-和/做分隔符均可）
            String[] ymd = obj.getString("sign_date").replaceAll("-", "/").split("/");
            int year = Integer.valueOf(ymd[0]);
            int month = Integer.valueOf(ymd[1]) - 1;
            int date = Integer.valueOf(ymd[2]);
            yearMonth = year * 12 + month;

            // 识别时间，比较保险的方法（.和:做分隔符均可）
            String[] time = obj.getString("sign_time").replaceAll("\\.", ":").split(":");

            // 由于体育系程序员是体育老师教的，他们直接把小数点分隔的时分当作小数来储存，百分位上的0会被抹去
            // 所以一位字符表示的分钟要在其后补0
            if (time[1].length() < 2) time[1] += "0";
            int hour = Integer.valueOf(time[0]);
            int minute = Integer.valueOf(time[1]);

            // 用识别出来的年、月、日、时、分和其它参数进行初始化
            dateTime = Calendar.getInstance();
            dateTime.set(year, month, date, hour, minute);
            isValid = obj.getString("sign_effect").equals("有效");
            exerciseCount = count;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public int getYearMonth() {
        return yearMonth;
    }

    public boolean getValid() {
        return isValid;
    }

    // 详情显示，用于点击日期时弹出对话框
    @Override
    public String toString() {
        return "打卡时间：" + new SimpleDateFormat("yyyy/M/d h:mm").format(dateTime.getTime()) + "\n"
                + "这是你第 " + exerciseCount + " 次跑操";
    }
}
