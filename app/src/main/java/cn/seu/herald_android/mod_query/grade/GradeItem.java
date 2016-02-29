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

    public GradeItem(String name, Double credit, Semester semester, String score, String type) {
        this.name = name;
        this.credit = credit;
        this.semester = semester;
        this.score = score;
        this.type = type;
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

    public static String testdata = "{\n" +
            "  \"content\": [   \n" +
            " {\n" +
            "      \"name\": \"程序设计基础及语言A\", \n" +
            "      \"extra\": \"\", \n" +
            "      \"credit\": \"2.0\", \n" +
            "      \"semester\": \"13-14-2\", \n" +
            "      \"score\": \"86\", \n" +
            "      \"type\": \"首修\"\n" +
            "    }, \n" +
            "    {\n" +
            "      \"name\": \"军事理论\", \n" +
            "      \"extra\": \"\", \n" +
            "      \"credit\": \"1.0\", \n" +
            "      \"semester\": \"13-14-2\", \n" +
            "      \"score\": \"84\", \n" +
            "      \"type\": \"首修\"\n" +
            "    }, \n" +
            "    {\n" +
            "      \"name\": \"图像处理及其应用介绍\", \n" +
            "      \"extra\": \"\", \n" +
            "      \"credit\": \"1.0\", \n" +
            "      \"semester\": \"13-14-2\", \n" +
            "      \"score\": \"优\", \n" +
            "      \"type\": \"首修\"\n" +
            "    }, \n" +
            "    {\n" +
            "      \"name\": \"军训（含理论课）\", \n" +
            "      \"extra\": \"\", \n" +
            "      \"credit\": \"2.0\", \n" +
            "      \"semester\": \"13-14-1\", \n" +
            "      \"score\": \"良\", \n" +
            "      \"type\": \"首修\"\n" +
            "    }\n" +
            "  ], \n" +
            "  \"code\": 200\n" +
            "}";

    public static class Semester{
        //学期类
        String semester;
        int startYear;
        int endYear;
        int semesterNum;
        public Semester(String term){
            this.semester = term;
            this.startYear = Integer.parseInt(semester.split("-")[0]);
            this.endYear = Integer.parseInt(semester.split("-")[1]);
            this.semesterNum =Integer.parseInt(semester.split("-")[2]);
        }
    }

    public static ArrayList<GradeItem> transfromJSONArrayToArrayList(JSONArray jsonArray)throws JSONException {
        //提供jsonarray到list的转换
        ArrayList<GradeItem> list = new ArrayList<>();
        for(int i = 0;i<jsonArray.length();i++)
        {
            JSONObject jsonitem = jsonArray.getJSONObject(i);
            if(jsonitem.has("name")){
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
}


