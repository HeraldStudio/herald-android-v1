package cn.seu.herald_android.mod_query.gymreserve;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class GymReserveItem {
    private int usedSite;
    private boolean enable;
    private int surplus;
    private String siteIdHalf;
    private String availableTime;
    private String siteIdAll;
    private int allSite;

    public static ArrayList<GymReserveItem> transformJSONtoArrayList(JSONArray jsonArray) throws Exception {
        ArrayList<GymReserveItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            GymReserveItem item = new GymReserveItem();
            item.usedSite = obj.getInt("usedSite");
            item.enable = obj.getBoolean("enable");
            item.surplus = obj.getInt("surplus");
            item.siteIdHalf = obj.getString("siteIdHalf");
            item.availableTime = obj.getString("availableTime");
            item.siteIdAll = obj.getString("siteIdAll");
            item.allSite = obj.getInt("allSite");
            list.add(item);
        }
        return list;
    }
}
