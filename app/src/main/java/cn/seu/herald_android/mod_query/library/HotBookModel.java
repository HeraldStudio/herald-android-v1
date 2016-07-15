package cn.seu.herald_android.mod_query.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class HotBookModel {
    //书的借阅次数
    private int count;
    //书所在的位置
    private String place;
    //书名
    private String name;
    //作者
    private String author;

    private HotBookModel(int count, String place, String name, String author) {
        this.count = count;
        this.place = place;
        this.name = name;
        this.author = author;
    }

    public int getCount() {
        return count;
    }

    public String getPlace() {
        return place;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public static ArrayList<HotBookModel> transformJSONArrayToArrayList(JSONArray array) throws JSONException {
        ArrayList<HotBookModel> list = new ArrayList<>();
        for(int i=0;i<array.length();i++){
            JSONObject objectItem = array.getJSONObject(i);
            list.add(new HotBookModel(
                    objectItem.getInt("count"),
                    objectItem.getString("place"),
                    objectItem.getString("name"),
                    objectItem.getString("author")
            ));
        }
        return list;
    }
}
