package cn.seu.herald_android.app_module.schoolbus;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

class SchoolBusModel {
    private String period;
    private String time;

    private SchoolBusModel(String period, String time) {
        this.period = period;
        this.time = time;
    }

    public static ArrayList<SchoolBusModel> transformJSONtoArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<SchoolBusModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(new SchoolBusModel(
                    jsonArray.getJSONObject(i).getString("time"),
                    jsonArray.getJSONObject(i).getString("bus")
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
