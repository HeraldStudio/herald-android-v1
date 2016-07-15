package cn.seu.herald_android.mod_query.gymreserve;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cn.seu.herald_android.R;

public class GymSportModel implements Serializable {
    public HashMap<String,Integer> ic_maps;
    //运动名字
    String name;
    //是否允许半场,为0则不允许，为1则允许
    int allowHalf;
    //全场最小预约人数请求
    int fullMinUsers;
    //全场最大预约人数请求;
    int fullMaxUsers;
    //半场最小预约人数请求
    int halfMinUsers;
    //全场最大预约人数请求;
    int halfMaxUsers;
    //运动标识的id
    int sportId;

    public GymSportModel(String name, int allowHalf, int fullMinUsers, int fullMaxUsers, int halfMinUsers, int halfMaxUsers, int sportId) {
        this.name = name;
        this.allowHalf = allowHalf;
        this.fullMinUsers = fullMinUsers;
        this.fullMaxUsers = fullMaxUsers;
        this.halfMinUsers = halfMinUsers;
        this.halfMaxUsers = halfMaxUsers;
        this.sportId = sportId;
        ic_maps = new HashMap<>();
        ic_maps.put("篮球", R.drawable.ic_sport_basketball);
        ic_maps.put("乒乓球",R.drawable.ic_sport_tabletennis);
        ic_maps.put("排球",R.drawable.ic_sport_volleyball);
        ic_maps.put("健身",R.drawable.ic_sport_fitness);
        ic_maps.put("跆拳道",R.drawable.ic_sport_dao);
        ic_maps.put("武术",R.drawable.ic_sport_kungfu);
        ic_maps.put("舞蹈",R.drawable.ic_sport_dance);
        ic_maps.put("羽毛球",R.drawable.ic_sport_adminton);
    }

    public static ArrayList<GymSportModel> transformJSONtoArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<GymSportModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            GymSportModel item = new GymSportModel(
                    obj.getString("name"),
                    obj.getInt("allowHalf"),
                    obj.getInt("fullMinUsers"),
                    obj.getInt("fullMaxUsers"),
                    obj.getInt("halfMinUsers"),
                    obj.getInt("halfMaxUsers"),
                    obj.getInt("id")
            );
            list.add(item);
        }
        return list;
    }

    public static String[] transformJSONtoStringArray(JSONArray jsonArray) throws JSONException {
        String[] items = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            items[i] = obj.getString("dayInfo");
        }
        return items;
    }
}


