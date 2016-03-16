package cn.seu.herald_android.mod_query.grade;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 成绩表每一行所对应的对象
 * Created by heyon on 2016/2/28.
 */
public class GradeItem {
    private String name;
    private Double credit;
    private Semester semester;
    private String score;
    private String type;

    private GradeItem(String name, Double credit, Semester semester, String score, String type) {
        this.name = name;
        this.credit = credit;
        this.semester = semester;
        this.score = score;
        this.type = type;
    }

    public static ArrayList<GradeItem> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        //提供jsonarray到list的转换
        ArrayList<GradeItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonitem = jsonArray.getJSONObject(i);
            if (jsonitem.has("name")) {
                list.add(new GradeItem(
                        jsonitem.getString("name"),
                        jsonitem.getDouble("credit"),
                        new GradeItem.Semester(jsonitem.getString("semester")),
                        jsonitem.getString("score"),
                        jsonitem.getString("type")
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
        //学期类
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


