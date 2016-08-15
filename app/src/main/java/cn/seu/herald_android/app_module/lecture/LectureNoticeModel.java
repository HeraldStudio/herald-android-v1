package cn.seu.herald_android.app_module.lecture;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class LectureNoticeModel {
    private String date;
    private String topic;
    private String speaker;
    private String location;

    public LectureNoticeModel(String date, String topic, String speaker, String location) {
        this.date = date;
        this.topic = topic;
        this.speaker = speaker;
        this.location = location;
    }

    public static ArrayList<LectureNoticeModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<LectureNoticeModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj json_item = jsonArray.$o(i);
            list.add(new LectureNoticeModel(
                    json_item.$s("date"),
                    json_item.$s("topic"),
                    json_item.$s("speaker"),
                    json_item.$s("location")
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
