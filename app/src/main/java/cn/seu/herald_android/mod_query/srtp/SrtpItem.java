package cn.seu.herald_android.mod_query.srtp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by heyon on 2016/3/13.
 */
public class SrtpItem {
    //从该srtp项目中获得的学分
    String credit;
    //改srtp项目中工作所占比例
    String proportion;
    //项目名字
    String project;
    //项目所属单位
    String department;
    //完成时间
    String date;
    //项目类型
    String type;
    //项目总学分
    String totalCredit;

    public SrtpItem(String credit, String proportion, String project, String department, String date, String type, String totalCredit) {
        this.credit = credit;
        this.proportion = proportion;
        this.project = project;
        this.department = department;
        this.date = date;
        this.type = type;
        this.totalCredit = totalCredit;
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

    public static ArrayList<SrtpItem> transfromJSONArrayToArrayList(JSONArray jsonArray)throws JSONException {
        ArrayList<SrtpItem> list = new ArrayList<>();
        for(int i = 0;i<jsonArray.length();i++){
            JSONObject json_item = jsonArray.getJSONObject(i);
            if(json_item.has("credit")){
                //判断不是首项才开始处理，首项为srtp总概况相关信息
                list.add(new SrtpItem(
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
}
