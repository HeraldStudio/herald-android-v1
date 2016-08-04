package cn.seu.herald_android.app_module.grade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GradeModel {
    private String name;
    private Double credit;
    private Semester semester;
    private String score;
    private String type;

    private GradeModel(String name, Double credit, Semester semester, String score, String type) {
        this.name = name;
        this.credit = credit;
        this.semester = semester;
        this.score = score;
        this.type = type;
    }

    public static ArrayList<GradeModel> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        // 提供JSONArray到list的转换
        ArrayList<GradeModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonItem = jsonArray.getJSONObject(i);
            if (jsonItem.has("name")) {
                list.add(new GradeModel(
                        jsonItem.getString("name"),
                        jsonItem.getDouble("credit"),
                        new GradeModel.Semester(jsonItem.getString("semester")),
                        jsonItem.getString("score"),
                        jsonItem.getString("type")
                ));
            }
        }
        return list;
    }

    public String getName() {
        return name;
    }

    public Double getCredit() {
        return credit;
    }

    public Semester getSemester() {
        return semester;
    }

    public String getScore() {
        return score;
    }

    public String getType() {
        return type;
    }

    public static class Semester {
        // 学期类
        String semester;
        int startYear;
        int endYear;
        int semesterNum;

        public Semester(String term) {
            this.semester = term;
            this.startYear = Integer.parseInt(semester.split("-")[0]);
            this.endYear = Integer.parseInt(semester.split("-")[1]);
            this.semesterNum = Integer.parseInt(semester.split("-")[2]);
        }
    }
}


