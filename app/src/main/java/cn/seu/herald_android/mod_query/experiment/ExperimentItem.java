package cn.seu.herald_android.mod_query.experiment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by heyon on 2016/3/3.
 */
public class ExperimentItem {
    String name;
    String date;
    String day;
    String teacher;
    String address;
    String grade;
    public ExperimentItem(String name, String date, String day, String teacher, String address, String grade) {
        this.name = name;
        this.date = date;
        this.day = day;
        this.teacher = teacher;
        this.address = address;
        this.grade = grade;
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

    public String getDay() {
        return day;
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
}
