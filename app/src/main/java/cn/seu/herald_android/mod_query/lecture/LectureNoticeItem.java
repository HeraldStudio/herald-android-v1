package cn.seu.herald_android.mod_query.lecture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LectureNoticeItem {
    String date;
    String topic;
    String speaker;
    String location;

    public LectureNoticeItem(String date, String topic, String speaker, String location) {
        this.date = date;
        this.topic = topic;
        this.speaker = speaker;
        this.location = location;
    }

    public static ArrayList<LectureNoticeItem> transformJSONArrayToArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<LectureNoticeItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json_item = jsonArray.getJSONObject(i);
            list.add(new LectureNoticeItem(
                    json_item.getString("date"),
                    json_item.getString("topic"),
                    json_item.getString("speaker"),
                    json_item.getString("location")
            ));
        }
        return list;
    }

    public String getDate() {
        return date;
    }

    public String getTopic() {
        return topic;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getLocation() {
        return location;
    }
}
