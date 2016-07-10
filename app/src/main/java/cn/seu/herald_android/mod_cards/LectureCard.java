package cn.seu.herald_android.mod_cards;

import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.lecture.LectureBlockLayout;
import cn.seu.herald_android.mod_query.lecture.LectureNoticeItem;

public class LectureCard {

    public static ApiRequest getRefresher() {
        return new ApiRequest().url(ApiHelper.wechat_lecture_notice_url).addUUID()
                .toCache("herald_lecture_notices", o -> o);
    }

    /**
     * 读取人文讲座预告缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        String cache = CacheHelper.get("herald_lecture_notices");
        try {
            JSONArray jsonArray = new JSONObject(cache).getJSONArray("content");
            ArrayList<View> lectures = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json_item = jsonArray.getJSONObject(i);
                String dateStr = json_item.getString("date").split("日")[0];
                String[] date = dateStr.replaceAll("年", "-").replaceAll("月", "-").split("-");
                String[] mdStr = {date[date.length - 2], date[date.length - 1]};

                int[] md = {
                        Integer.valueOf(mdStr[0]),
                        Integer.valueOf(mdStr[1])
                };
                Calendar time = Calendar.getInstance();
                if (time.get(Calendar.MONTH) + 1 == md[0] && time.get(Calendar.DAY_OF_MONTH) == md[1]) {
                    if (time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE) < 19 * 60) {

                        LectureBlockLayout block = new LectureBlockLayout(AppContext.currentContext.get(),
                                new LectureNoticeItem(
                                        json_item.getString("date"),
                                        json_item.getString("topic"),
                                        json_item.getString("speaker"),
                                        json_item.getString("location")
                                ));
                        lectures.add(block);
                    }
                }
            }

            // 今天有人文讲座
            if (lectures.size() > 0) {
                Calendar time = Calendar.getInstance();
                time = CalendarUtils.toSharpDay(time);
                time.set(Calendar.HOUR_OF_DAY, 18);
                time.set(Calendar.MINUTE, 30);

                CardsModel item = new CardsModel(SettingsHelper.Module.lecture,
                        CardsModel.Priority.CONTENT_NO_NOTIFY,
                        "今天有新的人文讲座，有兴趣的同学欢迎参加"
                );
                item.attachedView = lectures;
                return item;
            }

            // 今天无人文讲座
            return new CardsModel(SettingsHelper.Module.lecture,
                    CardsModel.Priority.NO_CONTENT, jsonArray.length() == 0 ? "暂无人文讲座预告信息"
                    : "暂无新的人文讲座，点我查看以后的预告"
            );

        } catch (Exception e) {// JSONException, NumberFormatException
            return new CardsModel(SettingsHelper.Module.lecture,
                    CardsModel.Priority.CONTENT_NOTIFY, "人文讲座数据为空，请尝试刷新"
            );
        }
    }
}
