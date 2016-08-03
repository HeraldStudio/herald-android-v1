package cn.seu.herald_android.factory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.jwc.JwcBlockLayout;
import cn.seu.herald_android.app_module.jwc.JwcNoticeModel;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.framework.network.ApiSimpleRequest;
import cn.seu.herald_android.framework.network.Method;
import cn.seu.herald_android.helper.CacheHelper;

public class JwcCard {

    public static ApiRequest getRefresher() {
        return new ApiSimpleRequest(Method.POST).api("jwc").addUuid()
                .toCache("herald_jwc");
    }

    /**
     * 读取教务通知缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        String cache = CacheHelper.get("herald_jwc");
        try {
            JSONArray json_content = new JSONObject(cache)
                    .getJSONObject("content").getJSONArray("教务信息");

            ArrayList<JwcBlockLayout> allNotices = new ArrayList<>();

            for (int i = 0; i < json_content.length(); i++) {
                JSONObject json_item = json_content.getJSONObject(i);
                JwcNoticeModel item = new JwcNoticeModel(
                        json_item.getString("date"),
                        json_item.getString("href"),
                        json_item.getString("title"));

                Calendar cal = Calendar.getInstance();
                if (item.date.equals(new SimpleDateFormat("yyyy-MM-dd")
                        .format(cal.getTime()))) {
                    item.date = "今天";
                    JwcBlockLayout block = new JwcBlockLayout(AppContext.instance, item);
                    allNotices.add(block);
                } else {
                    cal.roll(Calendar.DAY_OF_MONTH, -1);
                    if (item.date.equals(new SimpleDateFormat("yyyy-MM-dd")
                            .format(cal.getTime()))) {
                        item.date = "昨天";
                        JwcBlockLayout block = new JwcBlockLayout(AppContext.instance, item);
                        allNotices.add(block);
                    }
                }
            }

            // 无教务信息
            if (allNotices.size() == 0) {
                return new CardsModel(Module.jwc,
                        CardsModel.Priority.NO_CONTENT, "最近没有新的核心教务通知");
            }

            CardsModel item = new CardsModel(Module.jwc,
                    CardsModel.Priority.CONTENT_NOTIFY, "最近有新的核心教务通知，有关同学请关注");
            item.attachedView.addAll(allNotices);
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新实验
            CacheHelper.set("herald_jwc", "");
            return new CardsModel(Module.experiment,
                    CardsModel.Priority.CONTENT_NOTIFY, "教务通知数据为空，请尝试刷新"
            );
        }
    }
}