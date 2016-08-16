package cn.seu.herald_android.app_module.srtp;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

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

    public static ArrayList<SrtpModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<SrtpModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj json_item = jsonArray.$o(i);
            if (json_item.has("credit")) {
                // 判断不是首项才开始处理，首项为srtp总概况相关信息
                list.add(new SrtpModel(
                        json_item.$s("credit"),
                        json_item.$s("proportion"),
                        json_item.$s("project"),
                        json_item.$s("department"),
                        json_item.$s("date"),
                        json_item.$s("type"),
                        json_item.$s("total credit")
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
