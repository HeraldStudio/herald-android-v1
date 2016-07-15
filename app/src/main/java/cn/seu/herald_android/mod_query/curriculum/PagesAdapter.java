package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seu.herald_android.app_framework.AppContext;


/**
 * 水平滑动分页的适配器，修改时很容易出错，请慎重操作
 */
class PagesAdapter extends PagerAdapter {

    private List<View> viewList;

    private int thisWeek = 0;

    // 构造函数
    public PagesAdapter(Context context, String data, String sidebar) {
        try {
            // 初始化视图链表
            viewList = new ArrayList<>();

            int maxWeek = 0;

            // 读取json内容
            JSONObject content = new JSONObject(data);

            // 计算总周数
            for (String weekNum : CurriculumScheduleLayout.WEEK_NUMS) {
                JSONArray arr = content.getJSONArray(weekNum);
                for (int i = 0; i < arr.length(); i++) {
                    ClassModel info = new ClassModel(arr.getJSONArray(i));
                    if (info.getEndWeek() > maxWeek) maxWeek = info.getEndWeek();
                }
            }

            // 如果没课, 什么也不做
            if (maxWeek < 1) {
                AppContext.showMessage("暂无课程");
                return;
            }

            Map<String, Pair<String, String>> sidebarInfo = new HashMap<>();

            // 将课程的授课教师和学分信息放入键值对
            JSONArray sidebarArray = new JSONArray(sidebar);
            for (int i = 0; i < sidebarArray.length(); i++) {
                JSONObject obj = sidebarArray.getJSONObject(i);
                sidebarInfo.put(obj.getString("course"),
                        new Pair<>(obj.getString("lecturer"), obj.getString("credit")));
            }

            // 读取开学日期
            int startMonth = content.getJSONObject("startdate").getInt("month");
            int startDate = content.getJSONObject("startdate").getInt("day");
            Calendar cal = Calendar.getInstance();
            cal.set(cal.get(Calendar.YEAR), startMonth, startDate);

            // 如果开学日期比今天还晚，则是去年开学的。这里用while保证了thisWeek永远大于零
            while (cal.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
            }

            // 计算当前周
            thisWeek = (int) ((Calendar.getInstance().getTimeInMillis() - cal.getTimeInMillis())
                    / (1000 * 60 * 60 * 24 * 7) + 1);

            // 实例化各页
            if (maxWeek == 0) {
                CurriculumScheduleLayout schedule =
                        new CurriculumScheduleLayout(context,
                                content, sidebarInfo, 1,
                                false);
                viewList.add(schedule);
            } else for (int i = 1; i <= maxWeek; i++) {
                CurriculumScheduleLayout schedule =
                        new CurriculumScheduleLayout(context,
                                content, sidebarInfo, i,
                                i == thisWeek);
                viewList.add(schedule);
            }
        } catch (JSONException e) {
            Toast.makeText(context, "数据错误，请尝试刷新", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    public int getCurrentPage() {
        return Math.min(viewList.size() - 1, thisWeek - 1);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(viewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = viewList.get(position);
        container.addView(v);

        return v;
    }
}
