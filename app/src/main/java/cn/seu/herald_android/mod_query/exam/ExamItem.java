package cn.seu.herald_android.mod_query.exam;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.custom.CalendarUtils;

public class ExamItem {
    String hour;
    String course;
    String location;
    String time;
    String type;
    String teacher;
    //标识是否是用户自定义的
    boolean isdefined = false;

    public ExamItem(String hour, String course, String location, String time, String type, String teacher) {
        this.hour = hour;
        this.course = course;
        this.location = location;
        this.time = time;
        this.type = type;
        this.teacher = teacher;
    }

    public ExamItem(JSONObject Json) {
        try{
            this.hour = Json.getString("hour");
            this.course = Json.getString("course");
            this.location =  Json.getString("location");
            this.time =  Json.getString("time");
            this.type =  Json.getString("type");
            this.teacher = Json.getString("teacher");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        ExamItem item = (ExamItem)o;
        return this.hour.equals(item.hour)
                && this.course.equals(item.course)
                && this.location.equals(item.location)
                && this.time.equals(item.time)
                && this.type.equals(item.type)
                && this.teacher.equals(item.teacher);
    }

    public JSONObject getJSON()throws JSONException{
        JSONObject newexam = new JSONObject();
        newexam.put("hour",hour);
        newexam.put("course",course);
        newexam.put("time",time);
        newexam.put("location",location);
        newexam.put("teacher",teacher);
        newexam.put("type",type);
        return newexam;
    }



    public static ArrayList<ExamItem> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<ExamItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            list.add(new ExamItem(
                    jsonObject.getString("hour"),
                    jsonObject.getString("course"),
                    jsonObject.getString("location"),
                    jsonObject.getString("time"),
                    jsonObject.getString("type"),
                    jsonObject.getString("teacher")
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
