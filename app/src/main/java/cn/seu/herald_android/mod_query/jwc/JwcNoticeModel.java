package cn.seu.herald_android.mod_query.jwc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JwcNoticeModel {
    public String date;
    public String href;
    public String title;

    public JwcNoticeModel(String date, String href, String title) {
        this.date = date;
        this.href = href;
        this.title = title;
    }

    public static ArrayList<JwcNoticeModel> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<JwcNoticeModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            list.add(new JwcNoticeModel(
                    jsonObject.getString("date"),
                    jsonObject.getString("href"),
                    jsonObject.getString("title")
            ));
        }
        return list;
    }
}
