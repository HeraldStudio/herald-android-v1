package cn.seu.herald_android.mod_query.cardextra;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

class CardItem {
    //消费日期
    private String date;
    //消费时间
    private String time;
    //消费数目
    private String price;
    //消费种类
    private String type;
    //扣费系统（消费地点
    private String system;
    //消费后余额
    private String left;

    private CardItem(String date, String time, String price, String type, String system, String left) {
        this.date = date;
        this.time = time;
        this.price = price;
        this.type = type;
        this.system = system;
        this.left = left;
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

}
