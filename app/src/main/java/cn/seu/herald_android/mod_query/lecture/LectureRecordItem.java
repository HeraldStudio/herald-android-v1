package cn.seu.herald_android.mod_query.lecture;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * 讲座记录的类
 * Created by heyon on 2016/3/1.
 */
public class LectureRecordItem {
    String time;
    String place;

    public LectureRecordItem(String time, String place) {
        this.time = time;
        this.place = place;
    }

    public static ArrayList<LectureRecordItem> transfromJSONArrayToArrayList(JSONArray jsonArray)throws JSONException{
        ArrayList<LectureRecordItem> list = new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            list.add(new LectureRecordItem(
                    jsonArray.getJSONObject(i).getString("date"),
                    jsonArray.getJSONObject(i).getString("place")
            ));
        }
        return list;
    }
}
