package cn.seu.herald_android.mod_cards;

import org.json.JSONObject;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_main.ActivitiesBlockLayout;
import cn.seu.herald_android.app_main.ActivitiesItem;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;

public class ActivityCard {

    //获取最新热门活动
    public static ApiRequest getRefresher() {
        return new ApiRequest()
                .get()
                .url("http://115.28.27.150/herald/api/v1/huodong/get?type=hot")
                .toCache("herald_afterschoolschool_hot", o -> o);
    }

    /**
     * 读取热门活动缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        String cache = CacheHelper.get("herald_afterschoolschool_hot");
        try {
            List<ActivitiesItem> activitiesItems = ActivitiesItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            if (activitiesItems.size() == 0) {
                return new CardsModel("校园活动","最近没有新的热门校园活动", CardsModel.Priority.NO_CONTENT, R.mipmap.ic_activity);
            } else {
                CardsModel item  = new CardsModel("校园活动","最近有新的热门校园活动，欢迎来参加~",
                        CardsModel.Priority.CONTENT_NOTIFY,R.mipmap.ic_activity);
                for (ActivitiesItem activitiesItem : activitiesItems) {
                    item.attachedView.add(new ActivitiesBlockLayout(AppContext.currentContext.$get(), activitiesItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            CacheHelper.set("herald_afterschoolschool_hot", "");
            return new CardsModel("校园活动","热门活动数据为空，请尝试刷新",
                    CardsModel.Priority.CONTENT_NOTIFY,R.mipmap.ic_activity);
        }
    }
}
