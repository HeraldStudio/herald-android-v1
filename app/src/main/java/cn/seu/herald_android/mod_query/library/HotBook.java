package cn.seu.herald_android.mod_query.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by heyon on 2016/3/15.
 */
public class HotBook {
    //书的借阅次数
    int count;
    //书所在的位置
    String place;
    //书名
    String name;
    //作者
    String author;

    public HotBook(int count, String place, String name, String author) {
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

    public static ArrayList<HotBook> transformJSONArrayToArrayList(JSONArray array)throws JSONException{
        ArrayList<HotBook> list = new ArrayList<>();
        for(int i=0;i<array.length();i++){
            JSONObject objectItem = array.getJSONObject(i);
            list.add(new HotBook(
                    objectItem.getInt("count"),
                    objectItem.getString("place"),
                    objectItem.getString("name"),
                    objectItem.getString("author")
            ));
        }
        return list;
    }
}
