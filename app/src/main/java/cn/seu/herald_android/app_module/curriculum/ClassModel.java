package cn.seu.herald_android.app_module.curriculum;

import cn.seu.herald_android.framework.json.JArr;


/**
 * 单次课程信息的类
 */
public class ClassModel {

    public String weekNum;
    private String className, place;
    private int startWeek, endWeek, startTime, endTime;

    public ClassModel(JArr json) throws ArrayIndexOutOfBoundsException {
        className = json.$s(0);
        place = json.$s(2);
        String timeStr = json.$s(1);
        String[] timeStrs = timeStr
                .replace("]", "-")
                .replace("[", "")
                .replace("周", "")
                .replace("节", "")
                .split("-");
        startWeek = Integer.valueOf(timeStrs[0]);
        endWeek = Integer.valueOf(timeStrs[1]);
        startTime = Integer.valueOf(timeStrs[2]);
        endTime = Integer.valueOf(timeStrs[3]);
    }

    private static String time60ToHourMinute(int time) {
        int minute = time % 60;
        int hour = time / 60;
        return hour + (minute < 10 ? ":0" : ":") + minute;
    }

    public String getClassName() {
        return className;
    }

    public String getPlace() {
        return place;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public String getTimePeriod() {
        return time60ToHourMinute(CurriculumScheduleLayout.CLASS_BEGIN_TIME[startTime - 1]) + "~"
                + time60ToHourMinute(CurriculumScheduleLayout.CLASS_BEGIN_TIME[endTime - 1] + 45);
    }

    public int getPeriodCount() {
        return getEndTime() - getStartTime() + 1;
    }

    public boolean isFitEvenOrOdd(int weekNum) {
        return weekNum % 2 == 0 ?
                !place.startsWith("(单)") : !place.startsWith("(双)");
    }
}
