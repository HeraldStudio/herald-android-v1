package cn.seu.herald_android.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.experiment.ExperimentBlockLayout;
import cn.seu.herald_android.app_module.experiment.ExperimentModel;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.helper.ApiHelper;

public class ExperimentCard {

    public static ApiRequest getRefresher() {
        return Cache.experiment.getRefresher();
    }

    /**
     * 读取实验缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        if (!ApiHelper.isLogin()) {
            return new CardsModel(Module.experiment,
                    CardsModel.Priority.NO_CONTENT, "登录即可使用实验查询、智能提醒功能"
            );
        }

        String cache = Cache.experiment.getValue();
        try {
            JObj json_content = new JObj(cache).$o("content");
            boolean todayHasExperiments = false;
            // 时间未到的所有实验
            ArrayList<ExperimentBlockLayout> allExperiments = new ArrayList<>();
            // 今天的实验或当前周的实验。若今天无实验，则为当前周的实验
            ArrayList<ExperimentBlockLayout> currExperiments = new ArrayList<>();

            for (String key : json_content.keySet()) {
                String jsonArray_str = json_content.$s(key);
                if (!jsonArray_str.equals("")) {
                    // 如果有实验则加载数据和子项布局
                    JArr jsonArray = new JArr(jsonArray_str);
                    for (int j = 0; j < jsonArray.size(); j++) {
                        JObj jsonObject = jsonArray.$o(j);
                        ExperimentModel item = new ExperimentModel(
                                jsonObject.$s("name"),
                                jsonObject.$s("Date"),
                                jsonObject.$s("Day"),
                                jsonObject.$s("Teacher"),
                                jsonObject.$s("Address"),
                                jsonObject.$s("Grade")
                        );
                        String[] ymdStr = item.getDate()
                                .split("日")[0].replace("年", "-").replace("月", "-").split("-");
                        int[] ymd = {
                                Integer.valueOf(ymdStr[0]),
                                Integer.valueOf(ymdStr[1]),
                                Integer.valueOf(ymdStr[2])
                        };
                        Calendar time = Calendar.getInstance();
                        time.set(ymd[0], ymd[1] - 1, ymd[2]);
                        time = CalendarUtils.toSharpDay(time);

                        // 没开始的实验全部单独记录下来
                        if (time.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                            ExperimentBlockLayout block = new ExperimentBlockLayout(AppContext.instance, item);
                            allExperiments.add(block);
                        }

                        // 属于同一周
                        if (CalendarUtils.toSharpWeek(time).getTimeInMillis()
                                == CalendarUtils.toSharpWeek(Calendar.getInstance()).getTimeInMillis()) {
                            // 如果发现今天有实验
                            Calendar nowCal = Calendar.getInstance();
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    == CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                // 如果是半小时之内快要开始的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int nowStamp = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE);
                                int startStamp = item.getBeginStamp();
                                if (nowStamp < startStamp && nowStamp >= startStamp - 30) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(AppContext.instance, item);
                                    CardsModel item1 = new CardsModel(Module.experiment,
                                            CardsModel.Priority.CONTENT_NOTIFY, "你有1个实验即将开始，请注意时间准时参加"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果是已经开始还未结束的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int endStamp = startStamp + 3 * 60;
                                if (nowStamp >= startStamp && nowStamp < endStamp) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(AppContext.instance, item);
                                    CardsModel item1 = new CardsModel(Module.experiment,
                                            CardsModel.Priority.CONTENT_NOTIFY, "1个实验正在进行"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果这个实验已经结束，跳过它
                                if (nowStamp >= endStamp) {
                                    continue;
                                }

                                // 如果是第一次发现今天有实验，则清空列表（之前放在列表里的都不是今天的）
                                // 然后做标记，以后不再记录不是今天的实验
                                if (!todayHasExperiments) {
                                    currExperiments.clear();
                                    todayHasExperiments = true;
                                }

                                // 记录今天的实验
                                ExperimentBlockLayout block = new ExperimentBlockLayout(AppContext.instance, item);
                                currExperiments.add(block);
                            }

                            // 如果不是今天的实验但已经结束，跳过它
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    <= CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                continue;
                            }

                            // 如果至今还未发现今天有实验，则继续记录本周的实验
                            if (!todayHasExperiments) {
                                ExperimentBlockLayout block = new ExperimentBlockLayout(AppContext.instance, item);
                                currExperiments.add(block);
                            }
                        }
                    }
                }
            }

            // 解析完毕，下面做统计
            int N = currExperiments.size();
            int M = allExperiments.size();

            Comparator<ExperimentBlockLayout> experimentsViewComparator =
                    (lhs, rhs) -> (int) ((lhs.getTime() - rhs.getTime()) / 1000 / 60 / 60);

            Collections.sort(currExperiments, experimentsViewComparator);
            Collections.sort(allExperiments, experimentsViewComparator);

            // 今天和本周均无实验
            if (N == 0) {
                CardsModel item = new CardsModel(Module.experiment,
                        M == 0 ? CardsModel.Priority.NO_CONTENT : CardsModel.Priority.CONTENT_NO_NOTIFY,
                        (M == 0 ? "你没有未完成的实验，" : ("本学期你还有" + M + "个实验，"))
                                + "实验助手可以智能提醒你参加即将开始的实验"
                );
                item.attachedView = new ArrayList<>();
                item.attachedView.addAll(allExperiments);
                return item;
            }

            // 今天或本周有实验
            CardsModel item = new CardsModel(Module.experiment,
                    CardsModel.Priority.CONTENT_NO_NOTIFY,
                    (todayHasExperiments ? "今天有" : "本周有") + N + "个实验，请注意准时参加"
            );
            item.attachedView = new ArrayList<>();
            item.attachedView.addAll(currExperiments);
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新实验
            Cache.experiment.clear();
            return new CardsModel(Module.experiment,
                    CardsModel.Priority.CONTENT_NOTIFY, "实验数据为空，请尝试刷新"
            );
        }
    }
}
