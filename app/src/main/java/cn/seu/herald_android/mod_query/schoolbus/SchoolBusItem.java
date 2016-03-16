package cn.seu.herald_android.mod_query.schoolbus;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

class SchoolBusItem {
    private String period;
    private String time;

    private SchoolBusItem(String period, String time) {
        this.period = period;
        this.time = time;
    }

    public static ArrayList<SchoolBusItem> transformJSONtoArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<SchoolBusItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(new SchoolBusItem(
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
