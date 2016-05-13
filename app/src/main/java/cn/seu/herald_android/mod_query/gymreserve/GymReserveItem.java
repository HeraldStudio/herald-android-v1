package cn.seu.herald_android.mod_query.gymreserve;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class GymReserveItem implements Serializable{
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

    public GymReserveItem(String name, int allowHalf, int fullMinUsers, int fullMaxUsers, int halfMinUsers, int halfMaxUsers, int sportId) {
        this.name = name;
        this.allowHalf = allowHalf;
        this.fullMinUsers = fullMinUsers;
        this.fullMaxUsers = fullMaxUsers;
        this.halfMinUsers = halfMinUsers;
        this.halfMaxUsers = halfMaxUsers;
        this.sportId = sportId;
    }

    public static ArrayList<GymReserveItem> transformJSONtoArrayList(JSONArray jsonArray) throws JSONException {
        ArrayList<GymReserveItem> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            GymReserveItem item = new GymReserveItem(
                    obj.getString("name"),
                    obj.getInt("allowHalf"),
                    obj.getInt("fullMinUsers"),
                    obj.getInt("fullMinUsers"),
                    obj.getInt("halfMinUsers"),
                    obj.getInt("halfMaxUsers"),
                    obj.getInt("id")
            );
            list.add(item);
        }
        return list;
    }




}


