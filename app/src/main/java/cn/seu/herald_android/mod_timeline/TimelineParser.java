package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.curriculum.ClassInfo;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleLayout;
import cn.seu.herald_android.mod_query.curriculum.CurriculumTimelineBlockLayout;
import cn.seu.herald_android.mod_query.experiment.ExperimentBlockLayout;
import cn.seu.herald_android.mod_query.experiment.ExperimentItem;
import cn.seu.herald_android.mod_query.lecture.LectureBlockLayout;
import cn.seu.herald_android.mod_query.lecture.LectureNoticeItem;
import cn.seu.herald_android.mod_query.pedetail.PedetailActivity;

public class TimelineParser {

    /**
     * 读取课表缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getCurriculumItem(Context context) {
        String cache = new CacheHelper(context).getCache("herald_curriculum");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONObject jsonObject = new JSONObject(cache);
            // 读取侧栏信息
            String sidebar = new CacheHelper(context).getCache("herald_sidebar");
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

            // 枚举今天的课程
            int dayDelta = (int) (today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24;
            int week = dayDelta / 7 + 1;
            int dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
            JSONArray array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            int classCount = 0;
            boolean classAlmostEnd = false;

            ArrayList<View> remainingClasses = new ArrayList<>();

            for (int j = 0; j < array.length(); j++) {
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
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
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(context,
                                info, pair == null ? "获取失败" : pair.first);
                        remainingClasses.add(block);
                    }

                    // 快要上课的紧急提醒
                    if (now >= startTime - 15 * 60 * 1000 && now < startTime) {
                        TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                                startTime, TimelineView.Item.CONTENT_NOTIFY, info.getClassName() + " 即将开始上课，请注意时间，准时上课"
                        );
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(context,
                                info, pair == null ? "获取失败" : pair.first);

                        item.attachedView.add(block);
                        return item;
                    } else if (now >= startTime && now < almostEndTime) {
                        // 正在上课的提醒
                        TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                                now, TimelineView.Item.CONTENT_NOTIFY, info.getClassName() + " 正在上课中"
                        );
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(context,
                                info, pair == null ? "获取失败" : pair.first);

                        item.attachedView.add(block);
                        return item;
                    }
                }
            }
            // 此处退出循环有三种可能：可能是今天没课，可能是课与课之间或早上的没上课状态，也可能是课上完了的状态

            // 如果不是课上完了的状态
            if (remainingClasses.size() > 0) {
                boolean firstClass = remainingClasses.size() == classCount;
                TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                        now, TimelineView.Item.CONTENT_NO_NOTIFY,
                        (classAlmostEnd ? "快要下课了，" : "") +
                                (firstClass ? "你今天有" : "你今天还有") + remainingClasses.size() + "节课，点我查看详情"
                );
                item.attachedView = remainingClasses;
                return item;
            }

            // 课上完了的状态

            // 若今天没课，或者课上完了，显示明天课程
            // 枚举明天的课程
            dayDelta = (int) (today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24 + 1;
            week = dayDelta / 7 + 1;
            dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
            array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            boolean todayHasClasses = classCount != 0;

            classCount = 0;
            ArrayList<View> viewList = new ArrayList<>();
            for (int j = 0; j < array.length(); j++) {
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];

                    Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                    CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(context,
                            info, pair == null ? "获取失败" : pair.first);
                    block.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
                    viewList.add(block);
                }
            }
            TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                    // 如果两天都没课，显示时间为今天，否则显示为明天
                    today.getTimeInMillis() + (classCount == 0 && !todayHasClasses ? 0 : 1000 * 60 * 60 * 24),
                    // 若明天有课，则属于有内容不提醒状态；否则属于无内容状态
                    classCount == 0 ? TimelineView.Item.NO_CONTENT : TimelineView.Item.CONTENT_NO_NOTIFY,
                    // 如果明天没课
                    classCount == 0 ? (todayHasClasses ? "明天" : "今明两天都") + "没有课程，娱乐之余请注意作息安排哦"
                            // 如果明天有课
                            : (todayHasClasses ? "今天的课程已经结束，" : "今天没有课程，") + "明天有" + classCount + "节课"
            );

            item.attachedView = viewList;

            return item;
        } catch (Exception e) {
            // 清除出错的数据，使下次懒惰刷新时刷新课表
            new CacheHelper(context).setCache("herald_curriculum", "");
            return new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                    now, TimelineView.Item.NO_CONTENT, "课表数据加载失败，请手动刷新"
            );
        }
    }


    /**
     * 读取实验缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getExperimentItem(Context context) {
        String cache = new CacheHelper(context).getCache("herald_experiment");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONObject json_content = new JSONObject(cache).getJSONObject("content");
            boolean todayHasExperiments = false;
            // 时间未到的所有实验
            ArrayList<View> allExperiments = new ArrayList<>();
            // 今天的实验或当前周的实验。若今天无实验，则为当前周的实验
            ArrayList<View> currExperiments = new ArrayList<>();

            for (int i = 0; i < json_content.length(); i++) {
                String jsonArray_str = json_content.getString(json_content.names().getString(i));
                if (!jsonArray_str.equals("")) {
                    //如果有实验则加载数据和子项布局
                    JSONArray jsonArray = new JSONArray(jsonArray_str);
                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(j);
                        ExperimentItem item = new ExperimentItem(
                                jsonObject.getString("name"),
                                jsonObject.getString("Date"),
                                jsonObject.getString("Day"),
                                jsonObject.getString("Teacher"),
                                jsonObject.getString("Address"),
                                jsonObject.getString("Grade")
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
                            ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
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
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                    TimelineView.Item item1 = new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                                            now, TimelineView.Item.CONTENT_NOTIFY, "你有1个实验即将开始，请注意时间准时参加"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果是已经开始还未结束的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int endStamp = startStamp + 3 * 60;
                                if (nowStamp >= startStamp && nowStamp < endStamp) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                    TimelineView.Item item1 = new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                                            now, TimelineView.Item.CONTENT_NOTIFY, "1个实验正在进行"
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
                                ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                currExperiments.add(block);
                            }

                            // 如果不是今天的实验但已经结束，跳过它
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    <= CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                continue;
                            }

                            // 如果至今还未发现今天有实验，则继续记录本周的实验
                            if (!todayHasExperiments) {
                                ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                currExperiments.add(block);
                            }
                        }
                    }
                }
            }

            // 解析完毕，下面做统计
            int N = currExperiments.size();
            int M = allExperiments.size();

            // 今天和本周均无实验
            if (N == 0) {
                TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                        now, M == 0 ? TimelineView.Item.NO_CONTENT : TimelineView.Item.CONTENT_NO_NOTIFY,
                        (M == 0 ? "你没有未完成的实验，" : ("本学期你还有" + M + "个实验，"))
                                + "实验助手可以智能提醒你参加即将开始的实验"
                );
                item.attachedView = allExperiments;
                return item;
            }

            // 今天或本周有实验
            TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineView.Item.CONTENT_NO_NOTIFY,
                    (todayHasExperiments ? "今天有" : "本周有") + N + "个实验，请注意准时参加"
            );
            item.attachedView = currExperiments;
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新实验
            new CacheHelper(context).setCache("herald_experiment", "");
            return new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineView.Item.NO_CONTENT, "实验数据加载失败，手动刷新"
            );
        }
    }

    /**
     * 读取人文讲座预告缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getLectureItem(Context context) {
        String cache = new CacheHelper(context).getCache("herald_lecture_notices");
        final long now = Calendar.getInstance().getTimeInMillis();
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
                    if (time.get(Calendar.HOUR_OF_DAY) * 60 + time.get(Calendar.MINUTE) < 18 * 60 + 30) {

                        LectureBlockLayout block = new LectureBlockLayout(context, new LectureNoticeItem(
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

                TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_LECTURE,
                        time.getTimeInMillis(), TimelineView.Item.CONTENT_NO_NOTIFY,
                        "今天有新的人文讲座，有兴趣的同学欢迎来参加"
                );
                item.attachedView = lectures;
                return item;
            }

            // 今天无人文讲座
            return new TimelineView.Item(SettingsHelper.MODULE_LECTURE,
                    now, TimelineView.Item.NO_CONTENT, "今天暂无人文讲座信息，点我查看以后的预告"
            );

        } catch (Exception e) {// JSONException, NumberFormatException
            return new TimelineView.Item(SettingsHelper.MODULE_LECTURE,
                    now, TimelineView.Item.NO_CONTENT, "人文讲座数据加载失败，请手动刷新"
            );
        }
    }

    /**
     * 读取跑操预报缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getPeForecastItem(Context context) {
        CacheHelper helper = new CacheHelper(context);
        String date = helper.getCache("herald_pc_date");
        String forecast = helper.getCache("herald_pc_forecast");
        final long now = Calendar.getInstance().getTimeInMillis();

        Calendar nowCal = Calendar.getInstance();
        long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
        long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;
        long endTime = today + PedetailActivity.FORECAST_TIME_PERIOD[1] * 60 * 1000;

        if (now >= startTime && !date.equals(String.valueOf(CalendarUtils.toSharpDay(nowCal).getTimeInMillis()))) {
            return new TimelineView.Item(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineView.Item.NO_CONTENT, "跑操预告刷新失败，请稍后重试"
            );
        }
        if (now < startTime) {
            // 跑操时间没到
            return new TimelineView.Item(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineView.Item.NO_CONTENT, "小猴会在早上跑操时间实时显示跑操预告"
            );
        } else if (now >= endTime) {
            // 跑操时间已过

            if (!forecast.contains("跑操")) {
                // 没有跑操预告信息
                return new TimelineView.Item(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineView.Item.NO_CONTENT, "今天没有跑操预告信息"
                );
            } else {
                // 有跑操预告信息
                return new TimelineView.Item(SettingsHelper.MODULE_PEDETAIL,
                        startTime, TimelineView.Item.NO_CONTENT, "小猴预测" + forecast
                );
            }
        } else {
            // 还没有跑操预告信息
            if (!forecast.contains("跑操")) {
                return new TimelineView.Item(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineView.Item.NO_CONTENT, "目前暂无跑操预报信息，请稍后刷新重试"
                );
            }

            // 有跑操预告信息
            return new TimelineView.Item(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineView.Item.CONTENT_NOTIFY, "小猴预测" + forecast
            );
        }
    }

    /**
     * 读取一卡通缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getCardItem(Context context) {
        CacheHelper helper = new CacheHelper(context);
        String cache = helper.getCache("herald_card");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {

            JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
            //获取余额并且设置
            String left = json_cache.getString("left");
            float extra = Float.valueOf(left);
            if (extra < 20) {
                return new TimelineView.Item(SettingsHelper.MODULE_CARDEXTRA,
                        now, TimelineView.Item.CONTENT_NOTIFY, "你的一卡通余额还有" + left + "元，提醒你及时充值"
                );
            } else {
                return new TimelineView.Item(SettingsHelper.MODULE_CARDEXTRA,
                        now, TimelineView.Item.CONTENT_NO_NOTIFY, "你的一卡通余额还有" + left + "元"
                );
            }
        } catch (Exception e) {
            return new TimelineView.Item(SettingsHelper.MODULE_CARDEXTRA,
                    now, TimelineView.Item.NO_CONTENT, "一卡通余额数据加载失败，请手动刷新"
            );
        }
    }
}
