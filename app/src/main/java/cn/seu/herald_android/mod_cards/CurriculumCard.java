package cn.seu.herald_android.mod_cards;

import android.util.Pair;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.seu.herald_android.app_framework.AppContext;
import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.curriculum.ClassModel;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleLayout;
import cn.seu.herald_android.mod_query.curriculum.CurriculumTimelineBlockLayout;

public class CurriculumCard {

    public static ApiRequest[] getRefresher() {
        return new ApiRequest[]{
                new ApiRequest().api("sidebar").addUUID()
                        .toCache("herald_sidebar", o -> o.getJSONArray("content")),

                new ApiRequest().api("curriculum").addUUID()
                        .toCache("herald_curriculum", o -> o.getJSONObject("content"))
        };
    }

    /**
     * 读取课表缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        final long now = Calendar.getInstance().getTimeInMillis();

        String cache = CacheHelper.get("herald_curriculum");
        if (!cache.equals("")) try {
            JSONObject jsonObject = new JSONObject(cache);
            // 读取侧栏信息
            String sidebar = CacheHelper.get("herald_sidebar");
            Map<String, Pair<String, String>> sidebarInfo = new HashMap<>();

            // 将课程的授课教师和学分信息放入键值对
            JSONArray sidebarArray = new JSONArray(sidebar);
            for (int i = 0; i < sidebarArray.length(); i++) {
                JSONObject obj = sidebarArray.getJSONObject(i);
                sidebarInfo.put(obj.getString("course"),
                        new Pair<>(obj.getString("lecturer"), obj.getString("credit")));
            }

            // 读取开学日期
            int startMonth = jsonObject.getJSONObject("startdate").getInt("month");
            int startDate = jsonObject.getJSONObject("startdate").getInt("day");
            Calendar termStart = Calendar.getInstance();
            termStart.set(termStart.get(Calendar.YEAR), startMonth, startDate);

            // 如果开学日期比今天还晚，则是去年开学的。这里用while保证了thisWeek永远大于零
            while (termStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                termStart.set(Calendar.YEAR, termStart.get(Calendar.YEAR) - 1);
            }
            termStart = CalendarUtils.toSharpDay(termStart);

            // 计算当前周
            Calendar today = Calendar.getInstance();
            today = CalendarUtils.toSharpDay(today);

            int dayDelta = (int) ((today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24);
            int week = dayDelta / 7 + 1;
            int dayOfWeek = dayDelta % 7; // 0代表周一，以此类推

            // 枚举今天的课程
            JSONArray array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            int classCount = 0;
            boolean classAlmostEnd = false;

            ArrayList<View> remainingClasses = new ArrayList<>();

            for (int j = 0; j < array.length(); j++) {
                try {
                    ClassModel info = new ClassModel(array.getJSONArray(j));
                    // 如果该课程本周上课
                    if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                        classCount++;
                        // 上课时间
                        long startTime = today.getTimeInMillis();
                        startTime += CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getStartTime() - 1] * 60 * 1000;

                        // 下课时间
                        long endTime = today.getTimeInMillis();
                        endTime += (CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getEndTime() - 1] + 45) * 60 * 1000;

                        // 快要下课的时间
                        long almostEndTime = today.getTimeInMillis();
                        almostEndTime += (CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getEndTime() - 1] + 35) * 60 * 1000;

                        // 如果是还没到时间的课，放在“你今天(还)有x节课”的列表里备用
                        // 只要没有快上课或正在上课的提醒导致中途退出循环的话，这个列表就会显示
                        if (now < startTime) {
                            if (now >= almostEndTime && now < endTime) {
                                classAlmostEnd = true;
                            }
                            info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                            Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                            CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(AppContext.currentContext.$get(),
                                    info, pair == null ? "获取失败" : pair.first);
                            remainingClasses.add(block);
                        }

                        // 快要上课的紧急提醒
                        if (now >= startTime - 15 * 60 * 1000 && now < startTime) {
                            CardsModel item = new CardsModel(SettingsHelper.Module.curriculum,
                                    CardsModel.Priority.CONTENT_NOTIFY, "即将开始上课，请注意时间，准时上课"
                            );
                            info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                            Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                            CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(AppContext.currentContext.$get(),
                                    info, pair == null ? "获取失败" : pair.first);

                            item.attachedView.add(block);
                            return item;
                        } else if (now >= startTime && now < almostEndTime) {
                            // 正在上课的提醒
                            CardsModel item = new CardsModel(SettingsHelper.Module.curriculum,
                                    CardsModel.Priority.CONTENT_NOTIFY, "正在上课中"
                            );
                            info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                            Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                            CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(AppContext.currentContext.$get(),
                                    info, pair == null ? "获取失败" : pair.first);

                            item.attachedView.add(block);
                            return item;
                        }
                    }
                } catch (Exception e) {
                    // 该课程信息不标准，例如辅修课等，无法被识别，则跳过
                }
            }
            // 此处退出循环有三种可能：可能是今天没课，可能是课与课之间或早上的没上课状态，也可能是课上完了的状态

            // 如果不是课上完了的状态
            if (remainingClasses.size() > 0) {
                boolean firstClass = remainingClasses.size() == classCount;
                CardsModel item = new CardsModel(SettingsHelper.Module.curriculum,
                        CardsModel.Priority.CONTENT_NO_NOTIFY,
                        (classAlmostEnd ? "快要下课了，" : "") +
                                (firstClass ? "你今天有" : "你今天还有") + remainingClasses.size() + "节课，点我查看详情"
                );
                item.attachedView = remainingClasses;
                return item;
            }

            // 课上完了的状态

            // 若今天没课，或者课上完了，显示明天课程
            // 枚举明天的课程
            dayDelta = (int) ((today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24) + 1;
            week = dayDelta / 7 + 1;
            dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
            array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            boolean todayHasClasses = classCount != 0;

            classCount = 0;
            ArrayList<View> viewList = new ArrayList<>();
            for (int j = 0; j < array.length(); j++) {
                ClassModel info = new ClassModel(array.getJSONArray(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];

                    Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                    CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(AppContext.currentContext.$get(),
                            info, pair == null ? "获取失败" : pair.first);
                    viewList.add(block);
                }
            }
            CardsModel item = new CardsModel(SettingsHelper.Module.curriculum,
                    // 若明天有课，则属于有内容不提醒状态；否则属于无内容状态
                    classCount == 0 ? CardsModel.Priority.NO_CONTENT : CardsModel.Priority.CONTENT_NO_NOTIFY,
                    // 如果明天没课
                    classCount == 0 ? (todayHasClasses ? "明天" : "今明两天都") + "没有课程，娱乐之余请注意作息安排哦"
                            // 如果明天有课
                            : (todayHasClasses ? "今天的课程已经结束，" : "今天没有课程，") + "明天有" + classCount + "节课"
            );

            item.attachedView = viewList;

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            // 清除出错的数据，使下次懒惰刷新时刷新课表
            CacheHelper.set("herald_curriculum", "");
        }
        return new CardsModel(SettingsHelper.Module.curriculum,
                CardsModel.Priority.CONTENT_NOTIFY, "课表数据为空，请尝试刷新"
        );
    }
}
