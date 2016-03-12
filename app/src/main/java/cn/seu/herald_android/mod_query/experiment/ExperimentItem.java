package cn.seu.herald_android.mod_query.experiment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.seu.herald_android.custom.CalendarUtils;

/**
 * Created by heyon on 2016/3/3.
 */
public class ExperimentItem {
    String name;
    String date;
    String time;
    String teacher;
    String address;
    String grade;
    int beginStamp;
    public ExperimentItem(String name, String date, String time, String teacher, String address, String grade) {
        this.name = name;
        this.date = date;
        this.time = getTimePeriod(time);
        this.teacher = teacher;
        this.address = address;
        this.grade = grade;
        this.beginStamp = getBeginHourMinuteStamp(time);
    }

    private int getBeginHourMinuteStamp(String time){
        int timeStamp;
        switch (time){
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
        if(timeStamp == 0) return "未知";

        return CalendarUtils.formatHourMinuteStamp(timeStamp) + "~"
                + CalendarUtils.formatHourMinuteStamp(timeStamp + 3 * 60);
    }

    public static ArrayList<ExperimentItem> transfromJSONArrayToArrayList(JSONArray jsonArray)throws JSONException{
        ArrayList<ExperimentItem> list = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            list.add(new ExperimentItem(
                    jsonObject.getString("name"),
                    jsonObject.getString("Date"),
                    jsonObject.getString("Day"),
                    jsonObject.getString("Teacher"),
                    jsonObject.getString("Address"),
                    jsonObject.getString("Grade")
            ));
        }
        return list;
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

    public String getAddress() {
        return address;
    }

    public String getGrade() {
        return grade;
    }

    public int getBeginStamp() {
        return beginStamp;
    }
}
