package cn.seu.herald_android.app_module.schoolbus;

import java.util.ArrayList;

import cn.seu.herald_android.framework.json.JArr;

class SchoolBusModel {
    private String period;
    private String time;

    private SchoolBusModel(String period, String time) {
        this.period = period;
        this.time = time;
    }

    public static ArrayList<SchoolBusModel> transformJSONtoArrayList(JArr jsonArray) {
        ArrayList<SchoolBusModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            list.add(new SchoolBusModel(
                    jsonArray.$o(i).$s("time"),
                    jsonArray.$o(i).$s("bus")
            ));
        }
        return list;
    }

    public String getPeriod() {
        return period;
    }

    public String getTime() {
        return time;
    }
}
