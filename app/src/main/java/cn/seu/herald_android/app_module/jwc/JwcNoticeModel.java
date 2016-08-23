package cn.seu.herald_android.app_module.jwc;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class JwcNoticeModel {
    public String date;
    public String href;
    public String title;

    public JwcNoticeModel(String date, String href, String title) {
        this.date = date;
        this.href = href;
        this.title = title;
    }

    public static ArrayList<JwcNoticeModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<JwcNoticeModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj jsonObject = jsonArray.$o(i);
            list.add(new JwcNoticeModel(
                    jsonObject.$s("date"),
                    jsonObject.$s("href"),
                    jsonObject.$s("title")
            ));
        }
        return list;
    }
}
