package cn.seu.herald_android.factory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.exam.AddExamActivity;
import cn.seu.herald_android.app_module.exam.ExamBlockLayout;
import cn.seu.herald_android.app_module.exam.ExamModel;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.helper.ApiHelper;

public class ExamCard {

    public static ApiRequest getRefresher() {
        return Cache.exam.getRefresher();
    }

    /**
     * 读取考试缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        if (!ApiHelper.isLogin()) {
            return new CardsModel(Module.exam,
                    CardsModel.Priority.NO_CONTENT, "登录即可使用考试查询、智能提醒功能"
            );
        }

        // 教务处考试缓存
        String cache = Cache.exam.getValue();
        try {
            List<ExamModel> examList = new ArrayList<>();
            List<ExamModel> temp = ExamModel.transformJSONArrayToArrayList(new JSONObject(cache).getJSONArray("content"));
            List<ExamModel> defined = ExamModel.transformJSONArrayToArrayList(AddExamActivity.getCustomExamJSONArray());

            // 加入教务处的考试
            for (ExamModel examModel : temp) {
                if (examModel.getRemainingDays() >= 0) {
                    examList.add(examModel);
                }
            }
            // 加入本地自定义的考试
            for (ExamModel examModel : defined) {
                if (examModel.getRemainingDays() >= 0) {
                    examList.add(examModel);
                }
            }

            if (examList.size() == 0) {
                return new CardsModel(Module.exam,
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
                CardsModel item = new CardsModel(Module.exam,
                        CardsModel.Priority.CONTENT_NOTIFY, "你最近有" + examList.size() + "场考试，抓紧时间复习吧");
                for (ExamModel examModel : examList) {
                    item.attachedView.add(new ExamBlockLayout(AppContext.instance, examModel));
                }
                return item;
            }

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新考试
            e.printStackTrace();
            Cache.exam.clear();
            return new CardsModel(Module.exam,
                    CardsModel.Priority.CONTENT_NOTIFY, "考试数据为空，请尝试刷新"
            );
        }
    }
}
