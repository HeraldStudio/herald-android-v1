package cn.seu.herald_android.factory;

import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.lecture.LectureBlockLayout;
import cn.seu.herald_android.app_module.lecture.LectureNoticeModel;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.helper.ApiHelper;

public class LectureCard {

    public static ApiRequest getRefresher() {
        return Cache.lectureNotices.getRefresher();
    }

    /**
     * 读取人文讲座预告缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        String cache = Cache.lectureNotices.getValue();
        try {
            JArr jsonArray = new JObj(cache).$a("content");
            ArrayList<View> lectures = new ArrayList<>();

            for (int i = 0; i < jsonArray.size(); i++) {
                JObj json_item = jsonArray.$o(i);
                String dateStr = json_item.$s("date").split("日")[0];
                String[] date = dateStr.replaceAll("年", "-").replaceAll("月", "-").split("-");
                String[] mdStr = {date[date.length - 2], date[date.length - 1]};

                int[] md = {
                        Integer.valueOf(mdStr[0]),
                        Integer.valueOf(mdStr[1])
                };
                Calendar time = Calendar.getInstance();
                if (time.get(Calendar.MONTH) + 1 == md[0] && time.get(Calendar.DAY_OF_MONTH) == md[1]) {
                    if (time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE) < 19 * 60) {

                        LectureBlockLayout block = new LectureBlockLayout(AppContext.instance,
                                new LectureNoticeModel(
                                        json_item.$s("date"),
                                        json_item.$s("topic"),
                                        json_item.$s("speaker"),
                                        json_item.$s("location")
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

                CardsModel item = new CardsModel(Module.lecture,
                        CardsModel.Priority.CONTENT_NO_NOTIFY,
                        "今天有新的人文讲座，有兴趣的同学欢迎参加"
                );
                item.attachedView = lectures;
                return item;
            }

            // 今天无人文讲座
            String desc = jsonArray.size() == 0 ? "暂无人文讲座预告信息" : "暂无新的人文讲座，点我查看以后的预告";
            if (!ApiHelper.isLogin()) {
                desc = "暂无最近讲座预告，登录可查询讲座记录";
            }
            return new CardsModel(Module.lecture, CardsModel.Priority.NO_CONTENT, desc);

        } catch (Exception e) {// JSONException, NumberFormatException
            return new CardsModel(Module.lecture,
                    CardsModel.Priority.CONTENT_NOTIFY, "人文讲座数据为空，请尝试刷新"
            );
        }
    }
}
