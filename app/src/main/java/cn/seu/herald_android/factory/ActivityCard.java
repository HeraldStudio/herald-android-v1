package cn.seu.herald_android.factory;

import org.json.JSONObject;

import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_main.ActivitiesBlockLayout;
import cn.seu.herald_android.app_main.ActivitiesItem;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.network.ApiRequest;

public class ActivityCard {

    // 获取最新热门活动
    public static ApiRequest getRefresher() {
        return Cache.activitiesHot.getRefresher();
    }

    /**
     * 读取热门活动缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        String cache = Cache.activitiesHot.getValue();
        try {
            List<ActivitiesItem> activitiesItems = ActivitiesItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            if (activitiesItems.size() == 0) {
                return new CardsModel("校园活动","最近没有新的热门校园活动", CardsModel.Priority.NO_CONTENT, R.mipmap.ic_activity);
            } else {
                CardsModel item  = new CardsModel("校园活动","最近有新的热门校园活动，欢迎来参加~",
                        CardsModel.Priority.CONTENT_NOTIFY,R.mipmap.ic_activity);
                for (ActivitiesItem activitiesItem : activitiesItems) {
                    item.attachedView.add(new ActivitiesBlockLayout(AppContext.instance, activitiesItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            Cache.activitiesHot.clear();
            return new CardsModel("校园活动","热门活动数据为空，请尝试刷新",
                    CardsModel.Priority.CONTENT_NOTIFY,R.mipmap.ic_activity);
        }
    }
}
