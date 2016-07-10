package cn.seu.herald_android.mod_query.jwc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JwcItem {
    public String date;
    public String href;
    public String title;

    public JwcItem(String date, String href, String title) {
        this.date = date;
        this.href = href;
        this.title = title;
    }

    public static ArrayList<JwcItem> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<JwcItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            list.add(new JwcItem(
                    jsonObject.getString("date"),
                    jsonObject.getString("href"),
                    jsonObject.getString("title")
            ));
        }
        return list;
    }
}
