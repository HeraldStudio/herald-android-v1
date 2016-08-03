package cn.seu.herald_android.app_module.srtp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class SrtpModel {
    // 从该srtp项目中获得的学分
    private String credit;
    // 改srtp项目中工作所占比例
    private String proportion;
    // 项目名字
    private String project;
    // 项目所属单位
    private String department;
    // 完成时间
    private String date;
    // 项目类型
    private String type;
    // 项目总学分
    private String totalCredit;

    private SrtpModel(String credit, String proportion, String project, String department, String date, String type, String totalCredit) {
        this.credit = credit;
        this.proportion = proportion;
        this.project = project;
        this.department = department;
        this.date = date;
        this.type = type;
        this.totalCredit = totalCredit;
    }

    public static ArrayList<SrtpModel> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<SrtpModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json_item = jsonArray.getJSONObject(i);
            if (json_item.has("credit")) {
                // 判断不是首项才开始处理，首项为srtp总概况相关信息
                list.add(new SrtpModel(
                        json_item.getString("credit"),
                        json_item.getString("proportion"),
                        json_item.getString("project"),
                        json_item.getString("department"),
                        json_item.getString("date"),
                        json_item.getString("type"),
                        json_item.getString("total credit")
                ));
            }
        }
        return list;
    }

    public String getCredit() {
        return credit;
    }

    public String getProportion() {
        return proportion;
    }

    public String getProject() {
        return project;
    }

    public String getDepartment() {
        return department;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getTotalCredit() {
        return totalCredit;
    }
}