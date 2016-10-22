package cn.seu.herald_android.app_module.pedetail;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.seu.herald_android.framework.json.JObj;

/**
 * 表示单个早操数据的类
 */
class PedetailRecordModel {

    // 打卡时间（年、月、日、时、分）
    private Calendar dateTime;

    public PedetailRecordModel(JObj obj) {
        // 识别年月日，比较保险的方法（-和/做分隔符均可）
        String[] ymd = obj.$s("sign_date").replaceAll("-", "/").split("/");
        int year = Integer.valueOf(ymd[0]);
        int month = Integer.valueOf(ymd[1]) - 1;
        int date = Integer.valueOf(ymd[2]);

        // 识别时间，比较保险的方法（.和:做分隔符均可）
        String[] time = obj.$s("sign_time").replaceAll("\\.", ":").split(":");

        // 由于体育系程序员是体育老师教的，他们直接把小数点分隔的时分当作小数来储存，百分位上的0会被抹去
        // 所以一位字符表示的分钟要在其后补0
        if (time[1].length() < 2) time[1] += "0";
        int hour = Integer.valueOf(time[0]);
        int minute = Integer.valueOf(time[1]);

        // 用识别出来的年、月、日、时、分和其它参数进行初始化
        dateTime = Calendar.getInstance();
        dateTime.set(year, month, date, hour, minute);
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    // 详情显示，用于点击日期时弹出对话框
    @Override
    public String toString() {
        return "打卡时间：" + new SimpleDateFormat("yyyy/M/d h:mm").format(dateTime.getTime());
    }
}
