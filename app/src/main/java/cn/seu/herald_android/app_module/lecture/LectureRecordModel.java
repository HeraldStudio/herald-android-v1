package cn.seu.herald_android.app_module.lecture;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;

class LectureRecordModel {
    String time;
    String place;

    private LectureRecordModel(String time, String place) {
        this.time = time;
        this.place = place;
    }

    public static ArrayList<LectureRecordModel> transformJArrToArrayList(JArr jsonArray) {
        ArrayList<LectureRecordModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            list.add(new LectureRecordModel(
                    jsonArray.$o(i).$s("date"),
                    jsonArray.$o(i).$s("place")
            ));
        }
        return list;
    }
}
