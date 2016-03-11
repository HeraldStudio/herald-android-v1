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

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.curriculum.ClassInfo;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleBlockLayout;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleLayout;

public class TimelineParser {

    public static ArrayList<TimelineView.Item> parseCurriculumAndAddToList(Context context,
                                                                           JSONObject jsonObject, ArrayList<TimelineView.Item> list) throws JSONException {

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
        final long now = Calendar.getInstance().getTimeInMillis();

        // 计算当前周
        Calendar today = Calendar.getInstance();
        today = CalendarUtils.toSharpDay(today);

        // 枚举今天的课程
        int dayDelta = (int) (today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24;
        int week = dayDelta / 7 + 1;
        int dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
        JSONArray array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
        for (int j = 0; j < array.length(); j++) {
            ClassInfo info = new ClassInfo(array.getJSONArray(j));
            // 如果该课程本周上课
            if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
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
                        time, message + info.getClassName()
                );

                info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                CurriculumScheduleBlockLayout block = new CurriculumScheduleBlockLayout(context,
                        info, sidebarInfo.get(info.getClassName()), false, true);
                block.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));

                int density = (int) context.getResources().getDisplayMetrics().density;
                item.attachedView.add(block);

                list.add(item);

                // 若今天课程还没结束，此处退出函数
                return list;
            }
        }

        // 若今天课程都结束了，显示明天课程
        // 枚举明天的课程
        dayDelta = (int) (today.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24 + 1;
        week = dayDelta / 7 + 1;
        dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
        array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);

        int classCount = 0;
        ArrayList<View> viewList = new ArrayList<>();
        for (int j = 0; j < array.length(); j++) {
            ClassInfo info = new ClassInfo(array.getJSONArray(j));
            // 如果该课程本周上课
            if (info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)) {
                classCount++;
                info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                CurriculumScheduleBlockLayout block = new CurriculumScheduleBlockLayout(context,
                        info, sidebarInfo.get(info.getClassName()), false, true);
                block.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
                viewList.add(block);
            }
        }
        TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                today.getTimeInMillis() + 1000 * 60 * 60 * 24,
                classCount == 0 ? "明天没有课程，娱乐之余请注意作息安排哦~" : "明天有" + classCount + "节课"
        );

        item.attachedView = viewList;

        list.add(item);

        return list;
    }
}
