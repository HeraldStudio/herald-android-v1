package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
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

public class TimelineParser {

    /**
     * 读取课表缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getCurriculumItem(Context context, String cache) {

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
            for (int j = 0; j < array.length(); j++) {
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    // 上课时间
                    long startTime = today.getTimeInMillis();
                    startTime += CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getStartTime() - 1] * 60 * 1000;

                    // 下课时间，为了提前显示下节课，将下课前10分钟即认为已下课
                    long endTime = today.getTimeInMillis();
                    endTime += (CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getEndTime() - 1] + 35) * 60 * 1000;

                    String message;
                    long time;
                    // 如果前面课程都已下课，本课程还没上课，显示该课程的提示，时间为上课时间
                    if (now < startTime) {
                        time = startTime;
                        message = "下节课：";
                    } else if (now < endTime) { // 如果本课程正在上课，显示正在上课的提示，时间为现在
                        time = now;
                        message = "正在上课：";
                    } else { // 如果本课程已下课，扔给下个循环做处理
                        continue;
                    }

                    TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                            time, true, message + info.getClassName()
                    );

                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                    Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                    CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(context,
                            info, pair == null ? "获取失败" : pair.first);
                    block.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));

                    item.attachedView.add(block);

                    // 若今天课程还没结束，此处退出函数
                    return item;
                }
            }

            // 若今天没课，且在下午6点半之前，显示今天没课
            Calendar halfPastSix = CalendarUtils.toSharpDay(Calendar.getInstance());
            halfPastSix.set(Calendar.HOUR_OF_DAY, 18);
            halfPastSix.set(Calendar.MINUTE, 30);
            if (classCount == 0 && now < halfPastSix.getTimeInMillis()) {
                return new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                        now, false, "今天没有课程，娱乐之余请注意作息安排哦~"
                );
            }

            // 若今天没课但过了下午6点半，或者课程都结束了，显示明天课程
            // 枚举明天的课程
            dayDelta = (int) (today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24 + 1;
            week = dayDelta / 7 + 1;
            dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
            array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);

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
                    today.getTimeInMillis() + 1000 * 60 * 60 * 24,
                    classCount != 0,
                    classCount == 0 ? "明天没有课程，娱乐之余请注意作息安排哦~" : "明天有" + classCount + "节课"
            );

            item.attachedView = viewList;

            return item;
        } catch (JSONException e) {
            return new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                    now, false, "X_X 课表数据加载失败，请进入课表模块手动刷新"
            );
        }
    }


    /**
     * 读取实验缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getExperimentItem(Context context, String cache) {
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONObject json_content = new JSONObject(cache).getJSONObject("content");
            boolean todayHasExperiments = false;
            ArrayList<View> experiments = new ArrayList<>();

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
                                            now, true, "你有1个实验即将开始，请注意时间准时参加~"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果是已经开始还未结束的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int endStamp = startStamp + 3 * 60;
                                if (nowStamp >= startStamp && nowStamp < endStamp) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                    TimelineView.Item item1 = new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                                            now, true, "1个实验正在进行"
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
                                    experiments.clear();
                                    todayHasExperiments = true;
                                }

                                // 记录今天的实验
                                ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                experiments.add(block);
                            }

                            // 如果不是今天的实验但已经结束，跳过它
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    <= CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                continue;
                            }

                            // 如果至今还未发现今天有实验，则继续记录本周的实验
                            if (!todayHasExperiments) {
                                ExperimentBlockLayout block = new ExperimentBlockLayout(context, item);
                                experiments.add(block);
                            }
                        }
                    }
                }
            }

            // 解析完毕，下面做统计
            int N = experiments.size();

            // 今天和本周均无实验
            if (N == 0) {
                return new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                        now, false, "实验助手可以智能提醒你参加即将开始的实验"
                );
            }

            // 今天或本周有实验
            TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                    now, true, (todayHasExperiments ? "今天有" : "本周有") + N + "个实验，请注意准时参加"
            );
            item.attachedView = experiments;
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            return new TimelineView.Item(SettingsHelper.MODULE_EXPERIMENT,
                    now, false, "X_X 实验数据加载失败，请手动刷新"
            );
        }
    }

    /**
     * 读取人文讲座预告缓存，转换成对应的时间轴条目
     **/
    public static TimelineView.Item getLectureItem(Context context, String cache) {
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
                        time.getTimeInMillis(), true, "今天有新的人文讲座，有兴趣的同学欢迎来参加~"
                );
                item.attachedView = lectures;
                return item;
            }

            // 今天无人文讲座
            return new TimelineView.Item(SettingsHelper.MODULE_LECTURE,
                    now, false, "今天暂无人文讲座信息，点我查看以后的预告"
            );

        } catch (Exception e) {// JSONException, NumberFormatException
            return new TimelineView.Item(SettingsHelper.MODULE_LECTURE,
                    now, false, "X_X 人文讲座数据加载失败，请手动刷新"
            );
        }
    }
}
