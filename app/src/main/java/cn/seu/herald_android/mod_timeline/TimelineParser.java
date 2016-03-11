package cn.seu.herald_android.mod_timeline;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.seu.herald_android.R;
import cn.seu.herald_android.helper.CacheHelper;
import cn.seu.herald_android.helper.SettingsHelper;
import cn.seu.herald_android.mod_query.curriculum.ClassInfo;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleBlockLayout;
import cn.seu.herald_android.mod_query.curriculum.CurriculumScheduleLayout;

public class TimelineParser {

    public static ArrayList<TimelineView.Item> parseCardAndAddToList(
            JSONArray jsonArray, ArrayList<TimelineView.Item> list) throws JSONException {

        int N = jsonArray.length();
        HashMap<String, Float> dayCostMap = new HashMap<>();
        for (int i = 0; i < N; i++) {
            String date = jsonArray.getJSONObject(i).getString("date").split(" ")[0];
            float cost = Float.valueOf(jsonArray.getJSONObject(i).getString("price"));
            // 充值暂不计算在内
            if (cost >= 0) continue;
            // 同一天的累加在一起
            if (dayCostMap.containsKey(date)) {
                Float flt = dayCostMap.get(date);
                dayCostMap.remove(date);
                dayCostMap.put(date, flt - cost);
            } else { // 不同天的新增一个键
                dayCostMap.put(date, -cost);
            }
        }
        float total = 0f;
        for (String date : dayCostMap.keySet()) {
            float cost = dayCostMap.get(date);
            total += cost;
        }
        // 求日均消费
        if (dayCostMap.size() < 1) return null;
        float average = total / dayCostMap.size();

        // 转换为时间轴项目
        for (String date : dayCostMap.keySet()) {
            float cost = dayCostMap.get(date);
            Calendar calendar = Calendar.getInstance();
            String[] ymd = date.split("/");
            int[] ymdInt = {Integer.valueOf(ymd[0]),
                    Integer.valueOf(ymd[1]),
                    Integer.valueOf(ymd[2])};
            calendar.set(ymdInt[0], ymdInt[1] - 1, ymdInt[2] + 1);
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 0);
            long time = calendar.getTimeInMillis();
            // 还没到时间的暂不显示
            if(time > Calendar.getInstance().getTimeInMillis()) continue;

            TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CARDEXTRA,
                    time, "昨天的一卡通消费已出账，共消费" + roundMoney(cost) + "元，" +
                    (cost == average ? "与一周平均消费相当，" :
                            (cost > average ? ("比一周平均消费高了" + roundMoney(cost - average) + "元，请注意节省开销哦~")
                                    : ("比一周平均消费低了" + roundMoney(average - cost) + "元，继续努力吧~")
                            )
                    )
            );

            list.add(item);
        }
        return list;
    }

    public static String roundMoney(float src) {
        int k = (int) (src * 100);
        return (k / 100) + "." + (k % 100);
    }

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
        int startDate =  jsonObject.getJSONObject("startdate").getInt("day");
        Calendar termStart = Calendar.getInstance();
        termStart.set(termStart.get(Calendar.YEAR), startMonth, startDate);

        // 如果开学日期比今天还晚，则是去年开学的。这里用while保证了thisWeek永远大于零
        while (termStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()){
            termStart.set(Calendar.YEAR, termStart.get(Calendar.YEAR) - 1);
        }
        termStart.set(Calendar.HOUR_OF_DAY, 0);
        termStart.set(Calendar.MINUTE, 0);

        // 计算当前周
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);

        Calendar lastWeek = Calendar.getInstance();
        lastWeek.setTimeInMillis(today.getTimeInMillis() - 7 * 24 * 60 * 60 * 1000);

        // 枚举7天前到今天的课程
        Calendar i = Calendar.getInstance();
        i.setTimeInMillis(Math.max(lastWeek.getTimeInMillis(), termStart.getTimeInMillis()));
        for(; i.getTimeInMillis() <= today.getTimeInMillis(); i.set(Calendar.DATE, i.get(Calendar.DATE)+1)){
            int dayDelta = (int)(i.getTimeInMillis() - termStart.getTimeInMillis()) / 1000 / 60 / 60 / 24;
            int week = dayDelta / 7 + 1;
            int dayOfWeek = dayDelta % 7; // 0代表周一，以此类推
            JSONArray array = jsonObject.getJSONArray(CurriculumScheduleLayout.WEEK_NUMS[dayOfWeek]);
            // 枚举星期相同的课程列表
            for(int j = 0; j < array.length(); j++) {
                ClassInfo info = new ClassInfo(array.getJSONArray(j));
                // 如果该课程本周上课
                if(info.getStartWeek() <= week && info.getEndWeek() >= week && info.isFitEvenOrOdd(week)){
                    // 计算开始时间，提前10分钟
                    long time = i.getTimeInMillis();
                    time += (CurriculumScheduleLayout.CLASS_BEGIN_TIME[info.getStartTime() - 1] - 10) * 60 * 1000;

                    // 没到提醒时间的跳过
                    if(time > Calendar.getInstance().getTimeInMillis()) continue;

                    TimelineView.Item item = new TimelineView.Item(SettingsHelper.MODULE_CURRICULUM,
                            time, "“" + info.getClassName() + "”课程将在十分钟后开始，地点在"
                            + info.getPlace().replace("(单)","").replace("(双)", "")
                            + "，请注意时间，按时上课哦~"
                    );

                    info.weekNum = CurriculumScheduleLayout.WEEK_NUMS_CN[dayOfWeek];
                    CurriculumScheduleBlockLayout block = new CurriculumScheduleBlockLayout(context,
                            info, sidebarInfo.get(info.getClassName()), false);
                    block.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));

                    int density = (int) context.getResources().getDisplayMetrics().density;
                    item.attachedView = block;

                    list.add(item);
                }
            }
        }

        return list;
    }
}
