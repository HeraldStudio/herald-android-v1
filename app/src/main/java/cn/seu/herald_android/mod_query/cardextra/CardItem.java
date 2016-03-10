package cn.seu.herald_android.mod_query.cardextra;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.String;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_timeline.TimelineView;

/**
 * Created by heyon on 2016/3/1.
 */
public class CardItem {
    //消费日期
    String date;
    //消费时间
    String time;
    //消费数目
    String price;
    //消费种类
    String type;
    //扣费系统（消费地点
    String system;
    //消费后余额
    String left;

    public CardItem(String date, String time, String price, String type, String system, String left) {
        this.date = date;
        this.time = time;
        this.price = price;
        this.type = type;
        this.system = system;
        this.left = left;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String getSystem() {
        return system;
    }

    public String getLeft() {
        return left;
    }

    public String getTime() {
        return time;
    }

    public static ArrayList<CardItem> transfromJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<CardItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(new CardItem(
                    jsonArray.getJSONObject(i).getString("date").split(" ")[0],
                    jsonArray.getJSONObject(i).getString("date").split(" ")[1],
                    jsonArray.getJSONObject(i).getString("price"),
                    jsonArray.getJSONObject(i).getString("type"),
                    jsonArray.getJSONObject(i).getString("system"),
                    jsonArray.getJSONObject(i).getString("left")
            ));
        }
        return list;
    }

}
