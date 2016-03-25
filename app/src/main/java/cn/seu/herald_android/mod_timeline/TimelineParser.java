package cn.seu.herald_android.mod_timeline;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.custom.ContextUtils;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.ServiceHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.curriculum.ClassInfo;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleLayout;
import cn.seu.herald_android.mod_query.curriculum.CurriculumTimelineBlockLayout;
import cn.seu.herald_android.mod_query.experiment.ExperimentBlockLayout;
import cn.seu.herald_android.mod_query.experiment.ExperimentItem;
import cn.seu.herald_android.mod_query.jwc.JwcBlockLayout;
import cn.seu.herald_android.mod_query.jwc.JwcItem;
import cn.seu.herald_android.mod_query.lecture.LectureBlockLayout;
import cn.seu.herald_android.mod_query.lecture.LectureNoticeItem;
import cn.seu.herald_android.mod_query.pedetail.PedetailActivity;

class TimelineParser {

    public static TimelineItem getPushMessageItem(TimelineView host) {
        ServiceHelper helper = new ServiceHelper(host.getContext());
        final long now = Calendar.getInstance().getTimeInMillis();

        //获取推送消息
        String pushMessage = helper.getPushMessageContent();
        if (!pushMessage.equals("")) {
            TimelineItem item = new TimelineItem("小猴提示", pushMessage,
                    now, TimelineItem.CONTENT_NOTIFY, R.mipmap.ic_pushmsg);

            String pushMessageUrl = helper.getPushMessageUrl();
            if (!pushMessageUrl.equals("") && !pushMessageUrl.equals("null")) {
                Uri uri = Uri.parse(pushMessageUrl);
                item.addButton(host.getContext(), "查看详情", (v) ->
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri)));
                item.setOnClickListener((v) ->
                        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri)));
            }
            return item;
        }

        return null;
    }

    public static TimelineItem getCheckVersionItem(TimelineView host) {
        ServiceHelper serviceHelper = new ServiceHelper(host.getContext());
        CacheHelper cacheHelper = new CacheHelper(host.getContext());
        final long now = Calendar.getInstance().getTimeInMillis();
        //如果版本有更新则提示更新版本
        int versionCode = ServiceHelper.getAppVersionCode(host.getContext());
        int newestCode = serviceHelper.getNewestVersionCode();

        if (versionCode < newestCode
                && !cacheHelper.getCache("herald_new_version_ignored").equals(String.valueOf(newestCode))) {
            //如果当前版本号小于最新版本，且用户没有忽略此版本，则提示更新
            String tip = "小猴偷米" + serviceHelper.getNewestVersionName() + "更新说明\n"
                    + serviceHelper.getNewestVersionDesc().replaceAll("\\\\n", "\n");
            TimelineItem item = new TimelineItem("版本升级", tip,
                    now, TimelineItem.CONTENT_NOTIFY, R.mipmap.ic_update);

            item.addButton(host.getContext(), "下载", (v) -> {
                Uri uri = Uri.parse(ServiceHelper.getServiceUrl(ServiceHelper.SERVICE_DOWNLOAD));
                host.getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
            });

            item.addButton(host.getContext(), "忽略此版本", (v) -> {
                ContextUtils.showMessage(host.getContext(), "已忽略此版本，你仍可在系统设置中找到此更新");
                cacheHelper.setCache("herald_new_version_ignored", String.valueOf(newestCode));
                host.loadContent(false);
            });

            return item;
        }

        return null;
    }

    /**
     * 读取课表缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getCurriculumItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_curriculum");
        final long now = Calendar.getInstance().getTimeInMillis();
        if(!cache.equals("")) try {
            JSONObject jsonObject = new JSONObject(cache);
            // 读取侧栏信息
            String sidebar = new CacheHelper(host.getContext()).getCache("herald_sidebar");
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
            int dayDelta = (int) ((today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24);
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
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                                info, pair == null ? "获取失败" : pair.first);
                        block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                SettingsHelper.moduleActions[SettingsHelper.MODULE_CURRICULUM])));
                        remainingClasses.add(block);
                    }

                    // 快要上课的紧急提醒
                    if (now >= startTime - 15 * 60 * 1000 && now < startTime) {
                        TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                                startTime, TimelineItem.CONTENT_NOTIFY, info.getClassName() + " 即将开始上课，请注意时间，准时上课"
                        );
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                                info, pair == null ? "获取失败" : pair.first);

                        block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                SettingsHelper.moduleActions[SettingsHelper.MODULE_CURRICULUM])));
                        item.attachedView.add(block);
                        return item;
                    } else if (now >= startTime && now < almostEndTime) {
                        // 正在上课的提醒
                        TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                                now, TimelineItem.CONTENT_NOTIFY, info.getClassName() + " 正在上课中"
                        );
                        info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                        Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                        CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                                info, pair == null ? "获取失败" : pair.first);

                        block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                SettingsHelper.moduleActions[SettingsHelper.MODULE_CURRICULUM])));
                        item.attachedView.add(block);
                        return item;
                    }
                }
            }
            // 此处退出循环有三种可能：可能是今天没课，可能是课与课之间或早上的没上课状态，也可能是课上完了的状态

            // 如果不是课上完了的状态
            if (remainingClasses.size() > 0) {
                boolean firstClass = remainingClasses.size() == classCount;
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                        now, TimelineItem.CONTENT_NO_NOTIFY,
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
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
                // 如果该课程本周上课
                if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                    classCount++;
                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];

                    Pair<String, String> pair = sidebarInfo.get(info.getClassName());
                    CurriculumTimelineBlockLayout block = new CurriculumTimelineBlockLayout(host.getContext(),
                            info, pair == null ? "获取失败" : pair.first);
                    block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                            SettingsHelper.moduleActions[SettingsHelper.MODULE_CURRICULUM])));
                    viewList.add(block);
                }
            }
            TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                    // 如果两天都没课，显示时间为今天，否则显示为明天
                    today.getTimeInMillis() + (classCount == 0 && !todayHasClasses ? 0 : 1000 * 60 * 60 * 24),
                    // 若明天有课，则属于有内容不提醒状态；否则属于无内容状态
                    classCount == 0 ? TimelineItem.NO_CONTENT : TimelineItem.CONTENT_NO_NOTIFY,
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
            new CacheHelper(host.getContext()).setCache("herald_curriculum", "");
        }
        return new TimelineItem(SettingsHelper.MODULE_CURRICULUM,
                now, TimelineItem.NO_CONTENT, "课表数据加载失败，请手动刷新"
        );
    }


    /**
     * 读取实验缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getExperimentItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_experiment");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONObject json_content = new JSONObject(cache).getJSONObject("content");
            boolean todayHasExperiments = false;
            // 时间未到的所有实验
            ArrayList<ExperimentBlockLayout> allExperiments = new ArrayList<>();
            // 今天的实验或当前周的实验。若今天无实验，则为当前周的实验
            ArrayList<ExperimentBlockLayout> currExperiments = new ArrayList<>();

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
                            ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                            block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                    SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
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
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                    block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                            SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                    TimelineItem item1 = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                                            now, TimelineItem.CONTENT_NOTIFY, "你有1个实验即将开始，请注意时间准时参加"
                                    );
                                    item1.attachedView.add(block);
                                    return item1;
                                }

                                // 如果是已经开始还未结束的实验，放弃之前所有操作，直接返回这个实验的提醒
                                int endStamp = startStamp + 3 * 60;
                                if (nowStamp >= startStamp && nowStamp < endStamp) {
                                    ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                    block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                            SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                    TimelineItem item1 = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                                            now, TimelineItem.CONTENT_NOTIFY, "1个实验正在进行"
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
                                ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                        SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
                                currExperiments.add(block);
                            }

                            // 如果不是今天的实验但已经结束，跳过它
                            if (CalendarUtils.toSharpDay(time).getTimeInMillis()
                                    <= CalendarUtils.toSharpDay(nowCal).getTimeInMillis()) {
                                continue;
                            }

                            // 如果至今还未发现今天有实验，则继续记录本周的实验
                            if (!todayHasExperiments) {
                                ExperimentBlockLayout block = new ExperimentBlockLayout(host.getContext(), item);
                                block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                        SettingsHelper.moduleActions[SettingsHelper.MODULE_EXPERIMENT])));
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
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                        now, M == 0 ? TimelineItem.NO_CONTENT : TimelineItem.CONTENT_NO_NOTIFY,
                        (M == 0 ? "你没有未完成的实验，" : ("本学期你还有" + M + "个实验，"))
                                + "实验助手可以智能提醒你参加即将开始的实验"
                );
                item.attachedView = new ArrayList<>();
                item.attachedView.addAll(allExperiments);
                return item;
            }

            // 今天或本周有实验
            TimelineItem item = new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineItem.CONTENT_NO_NOTIFY,
                    (todayHasExperiments ? "今天有" : "本周有") + N + "个实验，请注意准时参加"
            );
            item.attachedView = new ArrayList<>();
            item.attachedView.addAll(currExperiments);
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新实验
            new CacheHelper(host.getContext()).setCache("herald_experiment", "");
            return new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineItem.NO_CONTENT, "实验数据加载失败，请手动刷新"
            );
        }
    }

    /**
     * 读取人文讲座预告缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getLectureItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_lecture_notices");
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

                        LectureBlockLayout block = new LectureBlockLayout(host.getContext(), new LectureNoticeItem(
                                json_item.getString("date"),
                                json_item.getString("topic"),
                                json_item.getString("speaker"),
                                json_item.getString("location")
                        ));
                        block.setOnClickListener(v -> host.getContext().startActivity(new Intent(
                                SettingsHelper.moduleActions[SettingsHelper.MODULE_LECTURE])));
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

                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_LECTURE,
                        time.getTimeInMillis(), TimelineItem.CONTENT_NO_NOTIFY,
                        "今天有新的人文讲座，有兴趣的同学欢迎来参加"
                );
                item.attachedView = lectures;
                return item;
            }

            // 今天无人文讲座
            return new TimelineItem(SettingsHelper.MODULE_LECTURE,
                    now, TimelineItem.NO_CONTENT, "今天暂无人文讲座信息，点我查看以后的预告"
            );

        } catch (Exception e) {// JSONException, NumberFormatException
            return new TimelineItem(SettingsHelper.MODULE_LECTURE,
                    now, TimelineItem.NO_CONTENT, "人文讲座数据加载失败，请手动刷新"
            );
        }
    }

    /**
     * 读取跑操预报缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getPeForecastItem(TimelineView host) {
        CacheHelper helper = new CacheHelper(host.getContext());
        String date = helper.getCache("herald_pc_date");
        String forecast = helper.getCache("herald_pc_forecast");
        String record = helper.getCache("herald_pedetail");
        final long now = Calendar.getInstance().getTimeInMillis();

        Calendar nowCal = Calendar.getInstance();
        long today = CalendarUtils.toSharpDay(nowCal).getTimeInMillis();
        long startTime = today + PedetailActivity.FORECAST_TIME_PERIOD[0] * 60 * 1000;
        long endTime = today + PedetailActivity.FORECAST_TIME_PERIOD[1] * 60 * 1000;

        if (record.contains(new SimpleDateFormat("yyyy-MM-dd").format(nowCal.getTime()))) {
            helper.setCache("herald_pc_last_message", "true");
            return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineItem.CONTENT_NOTIFY, "你今天的跑操已经到账，点我查看详情"
            );
        }

        if (now >= startTime && !date.equals(String.valueOf(CalendarUtils.toSharpDay(nowCal).getTimeInMillis()))) {
            return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineItem.NO_CONTENT, "跑操预告刷新失败，请稍后重试"
            );
        }
        if (now < startTime) {
            // 跑操时间没到
            return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineItem.NO_CONTENT, "小猴会在早上跑操时间实时显示跑操预告"
            );
        } else if (now >= endTime) {
            // 跑操时间已过

            if (!forecast.contains("跑操")) {
                // 没有跑操预告信息
                return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineItem.NO_CONTENT, "今天没有跑操预告信息"
                );
            } else {
                // 有跑操预告信息但时间已过
                return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        startTime, TimelineItem.NO_CONTENT, forecast + "（已结束）"
                );
            }
        } else {
            // 还没有跑操预告信息
            if (!forecast.contains("跑操")) {
                return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                        now, TimelineItem.NO_CONTENT, "目前暂无跑操预报信息，请稍后刷新重试"
                );
            }

            // 有跑操预告信息
            return new TimelineItem(SettingsHelper.MODULE_PEDETAIL,
                    now, TimelineItem.CONTENT_NOTIFY, "小猴预测" + forecast
            );
        }
    }

    /**
     * 读取一卡通缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getCardItem(TimelineView host) {
        CacheHelper helper = new CacheHelper(host.getContext());
        String cache = helper.getCache("herald_card");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {

            JSONObject json_cache = new JSONObject(cache).getJSONObject("content");
            //获取余额并且设置
            String left = json_cache.getString("left");
            float extra = Float.valueOf(left);

            //若检测到超过上次忽略时的余额，认为已经充值过了，取消忽略充值提醒
            boolean isNumber = true;
            try {
                if (extra > Float.valueOf(helper.getCache("herald_card_charged"))) {
                    helper.setCache("herald_card_charged", "");
                    isNumber = false;
                }
            } catch (NumberFormatException e) {
                isNumber = false;
            }

            if (extra < 20) {
                // 若没有被忽略的充值提醒，或者超过上次忽略提醒时的余额，认为余额不足需要提醒
                if (!isNumber || extra > Float.valueOf(helper.getCache("herald_card_charged"))) {

                    TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                            now, TimelineItem.CONTENT_NOTIFY, "你的一卡通余额还有" + left + "元，提醒你及时充值"
                    );
                    item.addButton(host.getContext(), "在线充值", (v) -> {
                        Toast.makeText(host.getContext(), "注意：由于一卡通中心配置问题，充值之后需要刷卡消费一次，一卡通余额才能正常显示", Toast.LENGTH_LONG).show();
                        Uri uri = Uri.parse("http://58.192.115.47:8088/wechat-web/login/initlogin.html");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        host.getContext().startActivity(intent);

                        new AlertDialog.Builder(host.getContext()).setMessage("充值成功了吗？\n" +
                                "选择充值成功可以自动忽略本次充值提醒。")
                                .setPositiveButton("充值成功了", (d, w) -> {
                                    helper.setCache("herald_card_charged", String.valueOf(extra));
                                    ContextUtils.showMessage(host.getContext(), "已忽略本次充值提醒，为了让小猴下次" +
                                            "还能正常提醒你充值，本次到账后记得让小猴知道哦~");
                                    host.loadContent(false);
                                })
                                .setNegativeButton("还没有", null)
                                .show();
                    });
                    item.addButton(host.getContext(), "充值过了", (v) -> {
                        helper.setCache("herald_card_charged", String.valueOf(extra));
                        ContextUtils.showMessage(host.getContext(), "已忽略本次充值提醒，为了让小猴下次" +
                                "还能正常提醒你充值，本次到账后记得让小猴知道哦~");
                        host.loadContent(false);
                    });
                    return item;
                } else {
                    TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                            now, TimelineItem.CONTENT_NO_NOTIFY, "你的一卡通余额还有" + left + "元，但小猴记得你似乎已经充值过了~"
                    );
                    item.addButton(host.getContext(), "我还没充值", (v) -> {
                        helper.setCache("herald_card_charged", "");
                        ContextUtils.showMessage(host.getContext(), "已取消忽略充值提醒");
                        host.loadContent(false);
                    });
                    return item;
                }
            } else {
                TimelineItem item = new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                        now, TimelineItem.CONTENT_NO_NOTIFY, "你的一卡通余额还有" + left + "元"
                );
                item.addButton(host.getContext(), "在线充值", (v) -> {
                    Toast.makeText(host.getContext(), "注意：由于一卡通中心配置问题，充值之后需要刷卡消费一次，一卡通余额才能正常显示", Toast.LENGTH_LONG).show();
                    Uri uri = Uri.parse("http://58.192.115.47:8088/wechat-web/login/initlogin.html");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    host.getContext().startActivity(intent);
                });

                return item;
            }
        } catch (Exception e) {
            return new TimelineItem(SettingsHelper.MODULE_CARDEXTRA,
                    now, TimelineItem.NO_CONTENT, "一卡通余额数据加载失败，请手动刷新"
            );
        }
    }

    /**
     * 读取教务通知缓存，转换成对应的时间轴条目
     **/
    public static TimelineItem getJwcItem(TimelineView host) {
        String cache = new CacheHelper(host.getContext()).getCache("herald_jwc");
        final long now = Calendar.getInstance().getTimeInMillis();
        try {
            JSONArray json_content = new JSONObject(cache)
                    .getJSONObject("content").getJSONArray("教务信息");

            ArrayList<View> allNotices = new ArrayList<>();

            for (int i = 0; i < json_content.length(); i++) {
                JSONObject json_item = json_content.getJSONObject(i);
                JwcItem item = new JwcItem(
                        json_item.getString("date"),
                        json_item.getString("href"),
                        json_item.getString("title"));

                if (item.getDate().equals(new SimpleDateFormat("yyyy-MM-dd")
                        .format(Calendar.getInstance().getTime()))) {
                    JwcBlockLayout block = new JwcBlockLayout(host.getContext(), item);
                    allNotices.add(block);
                }
            }

            // 无教务信息
            if (allNotices.size() == 0) {
                return new TimelineItem(SettingsHelper.MODULE_JWC,
                        now, TimelineItem.NO_CONTENT, "今天没有新的重要教务通知");
            }

            TimelineItem item = new TimelineItem(SettingsHelper.MODULE_JWC,
                    now, TimelineItem.CONTENT_NOTIFY, "今天有新的重要教务通知，有关同学请关注");
            item.attachedView = allNotices;
            return item;

        } catch (Exception e) {// JSONException, NumberFormatException
            // 清除出错的数据，使下次懒惰刷新时刷新实验
            new CacheHelper(host.getContext()).setCache("herald_jwc", "");
            return new TimelineItem(SettingsHelper.MODULE_EXPERIMENT,
                    now, TimelineItem.NO_CONTENT, "教务通知加载失败，请手动刷新"
            );
        }
    }
}
