package cn.seu.herald_android.mod_cards;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.exam.ExamBlockLayout;
import cn.seu.herald_android.mod_query.exam.ExamItem;

public class ExamCard {

    public static ApiRequest getRefresher() {
        return new ApiRequest().api("exam").addUUID()
                .toCache("herald_exam", o -> o);
    }

    /**
     * 读取考试缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        //教务处考试缓存
        String cache = CacheHelper.get("herald_exam");
        //自定义考试缓存
        String definedcache = CacheHelper.get("herald_exam_definedexam");
        if (definedcache.equals("")) {
            definedcache = "[]";
        }
        try {
            List<ExamItem> examList = new ArrayList<>();
            List<ExamItem> temp = ExamItem.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            List<ExamItem> defined = ExamItem.transformJSONArrayToArrayList(new JSONArray(definedcache));
            //加入教务处的考试
            for (ExamItem examItem : temp) {
                if (examItem.getRemainingDays() >= 0) {
                    examList.add(examItem);
                }
            }
            //加入本地自定义的考试
            for (ExamItem examItem : defined) {
                if (examItem.getRemainingDays() >= 0) {
                    examList.add(examItem);
                }
            }

            if (examList.size() == 0) {
                return new CardsModel(SettingsHelper.Module.exam,
                        CardsModel.Priority.NO_CONTENT, "最近没有新的考试安排");
            } else {
                Collections.sort(examList, (e1, e2) -> {
                    int remainingDays1 = 0, remainingDays2 = 0;
                    try {
                        remainingDays1 = e1.getRemainingDays();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        remainingDays2 = e2.getRemainingDays();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return remainingDays1 - remainingDays2;
                });
                CardsModel item = new CardsModel(SettingsHelper.Module.exam,
                        CardsModel.Priority.CONTENT_NOTIFY, "你最近有" + examList.size() + "场考试，抓紧时间复习吧");
                for (ExamItem examItem : examList) {
                    item.attachedView.add(new ExamBlockLayout(AppContext.currentContext.get(), examItem));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            CacheHelper.set("herald_exam", "");
            return new CardsModel(SettingsHelper.Module.exam,
                    CardsModel.Priority.CONTENT_NOTIFY, "考试数据为空，请尝试刷新"
            );
        }
    }
}
