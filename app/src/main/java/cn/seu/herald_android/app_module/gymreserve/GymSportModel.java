package cn.seu.herald_android.app_module.gymreserve;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

public class GymSportModel implements Serializable {
    public HashMap<String,Integer> ic_maps;
    // 运动名字
    String name;
    // 是否允许半场,为0则不允许，为1则允许
    int allowHalf;
    // 全场最小预约人数请求
    int fullMinUsers;
    // 全场最大预约人数请求;
    int fullMaxUsers;
    // 半场最小预约人数请求
    int halfMinUsers;
    // 全场最大预约人数请求;
    int halfMaxUsers;
    // 运动标识的id
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

    public static ArrayList<GymSportModel> transformJSONtoArrayList(JArr jsonArray) {
        ArrayList<GymSportModel> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj obj = jsonArray.$o(i);
            GymSportModel item = new GymSportModel(
                    obj.$s("name"),
                    obj.$i("allowHalf"),
                    obj.$i("fullMinUsers"),
                    obj.$i("fullMaxUsers"),
                    obj.$i("halfMinUsers"),
                    obj.$i("halfMaxUsers"),
                    obj.$i("id")
            );
            list.add(item);
        }
        return list;
    }

    public static String[] transformJSONtoStringArray(JArr jsonArray) {
        String[] items = new String[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            JObj obj = jsonArray.$o(i);
            items[i] = obj.$s("dayInfo");
        }
        return items;
    }
}


