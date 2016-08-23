package cn.seu.herald_android.app_module.experiment;

import java.util.ArrayList;

import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class ExperimentModel {
    String name;
    String date;
    String time;
    String teacher;
    String address;
    String grade;
    private int beginStamp;

    public ExperimentModel(String name, String date, String time, String teacher, String address, String grade) {
        this.name = name;
        this.date = date;
        this.time = getTimePeriod(time);
        this.teacher = teacher;
        this.address = address;
        this.grade = grade;
        this.beginStamp = getBeginHourMinuteStamp(time);
    }

    public static ArrayList<ExperimentModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<ExperimentModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj jsonObject = jsonArray.$o(i);
            list.add(new ExperimentModel(
                    jsonObject.$s("name"),
                    jsonObject.$s("Date"),
                    jsonObject.$s("Day"),
                    jsonObject.$s("Teacher"),
                    jsonObject.$s("Address"),
                    jsonObject.$s("Grade")
            ));
        }
        return list;
    }

    private int getBeginHourMinuteStamp(String time) {
        int timeStamp;
        switch (time) {
            case "上午":
                timeStamp = ExperimentActivity.EXPERIMENT_BEGIN_TIME[0];
                break;
            case "下午":
                timeStamp = ExperimentActivity.EXPERIMENT_BEGIN_TIME[1];
                break;
            case "晚上":
                timeStamp = ExperimentActivity.EXPERIMENT_BEGIN_TIME[2];
                break;
            default:
                timeStamp = 0;
        }
        return timeStamp;
    }

    private String getTimePeriod(String time) {
        int timeStamp = getBeginHourMinuteStamp(time);
        if (timeStamp == 0) return "未知";

        return CalendarUtils.formatHourMinuteStamp(timeStamp) + "~"
                + CalendarUtils.formatHourMinuteStamp(timeStamp + 3 * 60);
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getGrade() {
        return grade;
    }

    public int getBeginStamp() {
        return beginStamp;
    }
}
