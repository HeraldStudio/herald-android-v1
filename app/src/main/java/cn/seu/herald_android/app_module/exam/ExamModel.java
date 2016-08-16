package cn.seu.herald_android.app_module.exam;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class ExamModel {
    String hour;
    String course;
    String location;
    String time;
    String type;
    String teacher;
    // 标识是否是用户自定义的
    boolean isCustom = false;

    public ExamModel(String hour, String course, String location, String time, String type, String teacher) {
        this.hour = hour;
        this.course = course;
        this.location = location;
        this.time = time;
        this.type = type;
        this.teacher = teacher;
    }

    public ExamModel(JObj Json) {
        this.hour = Json.$s("hour");
        this.course = Json.$s("course");
        this.location = Json.$s("location");
        this.time = Json.$s("time");
        this.type = Json.$s("type");
        this.teacher = Json.$s("teacher");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ExamModel) {
            ExamModel item = (ExamModel) o;
            return this.hour.equals(item.hour)
                    && this.course.equals(item.course)
                    && this.location.equals(item.location)
                    && this.time.equals(item.time)
                    && this.type.equals(item.type)
                    && this.teacher.equals(item.teacher);
        }
        return false;
    }

    public static ArrayList<ExamModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<ExamModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj jsonObject = jsonArray.$o(i);
            list.add(new ExamModel(
                    jsonObject.$s("hour"),
                    jsonObject.$s("course"),
                    jsonObject.$s("location"),
                    jsonObject.$s("time"),
                    jsonObject.$s("type"),
                    jsonObject.$s("teacher")
            ));
        }
        return list;
    }

    public int getRemainingDays() throws Exception {
        String[] ymdhm = time.trim().split("\\(")[0].replaceAll("-", " ").replaceAll(":", " ").split(" ");
        Calendar calendar = CalendarUtils.toSharpDay(Calendar.getInstance());
        calendar.set(Integer.valueOf(ymdhm[0]), Integer.valueOf(ymdhm[1]) - 1, Integer.valueOf(ymdhm[2]));
        Calendar today = CalendarUtils.toSharpDay(Calendar.getInstance());
        return (int) ((calendar.getTimeInMillis() - today.getTimeInMillis()) / 1000 / 60 / 60 / 24);
    }
}
