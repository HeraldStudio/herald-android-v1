package cn.seu.herald_android.factory;

import android.util.Pair;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.seu.herald_android.app_main.CardsModel;
import cn.seu.herald_android.app_module.curriculum.ClassModel;
import cn.seu.herald_android.app_module.curriculum.CurriculumBlockLayout;
import cn.seu.herald_android.app_module.curriculum.CurriculumScheduleLayout;
import cn.seu.herald_android.consts.Cache;
import cn.seu.herald_android.consts.Module;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.framework.network.ApiRequest;
import cn.seu.herald_android.helper.ApiHelper;

public class CurriculumCard {

    public static ApiRequest getRefresher() {
        return Cache.curriculum.getRefresher().parallel(
                Cache.curriculumSidebar.getRefresher()
        );
    }

    /**
     * 读取课表缓存，转换成对应的时间轴条目
     **/
    public static CardsModel getCard() {
        if (!ApiHelper.isLogin()) {
            return new CardsModel(Module.curriculum,
                    CardsModel.Priority.NO_CONTENT, "登录即可使用课表查询、智能提醒功能"
            );
        }

        final long now = Calendar.getInstance().getTimeInMillis();

        String cache = Cache.curriculum.getValue();
        if (!cache.equals("")) try {
            JObj jsonObject = new JObj(cache);
            // 读取侧栏信息
            String sidebar = Cache.curriculumSidebar.getValue();
            Map<String, Pair<String, String>> sidebarInfo = new HashMap<>();

            // 将课程的授课教师和学分信息放入键值对
            JArr sidebarArray = new JArr(sidebar);
            for (int i = 0; i < sidebarArray.size(); i++) {
                JObj obj = sidebarArray.$o(i);
                sidebarInfo.put(obj.$s("course"),
                        new Pair<>(obj.$s("lecturer"), obj.$s("credit")));
            }

            // 读取开学日期
            int startMonth = jsonObject.$o("startdate").$i("month");
            int startDate = jsonObject.$o("startdate").$i("day");
            Calendar termStart = Calendar.getInstance();
            termStart.set(termStart.get(Calendar.YEAR), startMonth, startDate);

            // 如果开学日期比今天晚了超过两个月，则认为是去年开学的。这里用while保证了thisWeek永远大于零
            while (termStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() > (long) 60 * 86400 * 1000) {
                termStart.set(Calendar.YEAR, termStart.get(Calendar.YEAR) - 1);
            }

            // 为了保险，检查开学日期的星期，不是周一的话往前推到周一
            long oldTimeMillis = termStart.getTimeInMillis();
            long daysAfterMonday = termStart.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            termStart.setTimeInMillis(oldTimeMillis - daysAfterMonday * 86400 * 1000);

            // 计算当前周
            Calendar today = Calendar.getInstance();
            today = CalendarUtils.toSharpDay(today);

            int dayDelta = (int) ((today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24);
            if (dayDelta < -1) {
                return new CardsModel(Module.curriculum,
                        CardsModel.Priority.CONTENT_NO_NOTIFY, "还没有开学，点击预览新学期课表~"
                );
            } else if (dayDelta == -1) {
                return new CardsModel(Module.curriculum,
                        CardsModel.Priority.CONTENT_NOTIFY, "明天就要开学了，点击预览新学期课表~"
                );
            }

            int week = dayDelta / 7 + 1;
            int dayOfWeek = dayDelta % 7; // 0代表周一，以此类推

            // 枚举今天的课程
            JArr array = jsonObject.$a(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            int classCount = 0;
            boolean classAlmostEnd = false;

            ArrayList<View> remainingClasses = new ArrayList<>();

            for (int j = 0; j < array.size(); j++) {
                try {
                    ClassModel info = new ClassModel(array.$a(j));
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
                            CurriculumBlockLayout block = new CurriculumBlockLayout(AppContext.instance,
                                    info, pair == null ? "获取失败" : pair.first);
                            remainingClasses.add(block);
                        }

                        // 快要上课的紧急提醒
                        if (now >= startTime - 15 * 60 * 1000 && now < startTime) {
                            CardsModel item = new CardsModel(Module.curriculum,
                                    CardsModel.Priority.CONTENT_NOTIFY, "即将开始上课，请注意时间，准时上课"
                            );
                            info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                            Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                            CurriculumBlockLayout block = new CurriculumBlockLayout(AppContext.instance,
                                    info, pair == null ? "获取失败" : pair.first);

                            item.attachedView.add(block);
                            return item;
                        } else if (now >= startTime && now < almostEndTime) {
                            // 正在上课的提醒
                            CardsModel item = new CardsModel(Module.curriculum,
                                    CardsModel.Priority.CONTENT_NOTIFY, "正在上课中"
                            );
                            info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                            Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                            CurriculumBlockLayout block = new CurriculumBlockLayout(AppContext.instance,
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
                CardsModel item = new CardsModel(Module.curriculum,
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
            array = jsonObject.$a(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            boolean todayHasClasses = classCount != 0;

            classCount = 0;
            ArrayList<View> viewList = new ArrayList<>();
            for (int j = 0; j < array.size(); j++) {
                ClassModel info = new ClassModel(array.$a(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];

                    Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                    CurriculumBlockLayout block = new CurriculumBlockLayout(AppContext.instance,
                            info, pair == null ? "获取失败" : pair.first);
                    viewList.add(block);
                }
            }
            CardsModel item = new CardsModel(Module.curriculum,
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
            Cache.curriculum.clear();
        }
        return new CardsModel(Module.curriculum,
                CardsModel.Priority.CONTENT_NOTIFY, "课表数据为空，请尝试刷新"
        );
    }
}
