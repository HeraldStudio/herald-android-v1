package cn.seu.herald_android.app_module.lecture;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

class LectureRecordModel {
    String time;
    String place;

    private LectureRecordModel(String time, String place) {
        this.time = time;
        this.place = place;
    }

    public static ArrayList<LectureRecordModel> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<LectureRecordModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(new LectureRecordModel(
                    jsonArray.getJSONObject(i).getString("date"),
                    jsonArray.getJSONObject(i).getString("place")
            ));
        }
        return list;
    }
}
