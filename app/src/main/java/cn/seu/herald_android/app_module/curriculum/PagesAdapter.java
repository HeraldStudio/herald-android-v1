package cn.seu.herald_android.app_module.curriculum;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seu.herald_android.framework.AppContext;
import cn.seu.herald_android.framework.json.JArr;
import cn.seu.herald_android.framework.json.JObj;

/**
 * 水平滑动分页的适配器，修改时很容易出错，请慎重操作
 */
class PagesAdapter extends PagerAdapter {

    private List<View> viewList;

    private int thisWeek = 0;

    // 构造函数
    public PagesAdapter(Context context, String data, String sidebar) {
        // 初始化视图链表
        viewList = new ArrayList<>();

        int maxWeek = 0;

        // 读取json内容
        JObj content = new JObj(data);

        // 计算总周数
        for (String weekNum : CurriculumScheduleLayout.WEEK_NUMS) {
            JArr arr = content.$a(weekNum);
            for (int i = 0; i < arr.size(); i++) {
                ClassModel info = new ClassModel(arr.$a(i));
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
        JArr sidebarArray = new JArr(sidebar);
        for (int i = 0; i < sidebarArray.size(); i++) {
            JObj obj = sidebarArray.$o(i);
            sidebarInfo.put(obj.$s("course"),
                    new Pair<>(obj.$s("lecturer"), obj.$s("credit")));
        }

        // 读取开学日期
        int startMonth = content.$o("startdate").$i("month");
        int startDate = content.$o("startdate").$i("day");
        Calendar beginOfTerm = Calendar.getInstance();
        beginOfTerm.set(beginOfTerm.get(Calendar.YEAR), startMonth, startDate);

        // 如果开学日期比今天晚了超过两个月，则认为是去年开学的。这里用while保证了thisWeek永远大于零
        while (beginOfTerm.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() > (long) 60 * 86400 * 1000) {
            beginOfTerm.set(Calendar.YEAR, beginOfTerm.get(Calendar.YEAR) - 1);
        }

        // 为了保险，检查开学日期的星期，不是周一的话往前推到周一
        long oldTimeMillis = beginOfTerm.getTimeInMillis();
        long daysAfterMonday = beginOfTerm.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        beginOfTerm.setTimeInMillis(oldTimeMillis - daysAfterMonday * 86400 * 1000);

        // 计算当前周
        thisWeek = (int) ((Calendar.getInstance().getTimeInMillis() - beginOfTerm.getTimeInMillis())
                / (1000 * 60 * 60 * 24 * 7) + 1);

        // 实例化各页
        if (maxWeek == 0) {
            CurriculumScheduleLayout schedule =
                    new CurriculumScheduleLayout(context,
                            content, sidebarInfo, 1,
                            false, null);
            viewList.add(schedule);
        } else for (int i = 1; i <= maxWeek; i++) {
            CurriculumScheduleLayout schedule =
                    new CurriculumScheduleLayout(context,
                            content, sidebarInfo, i,
                            i == thisWeek, beginOfTerm);
            viewList.add(schedule);
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
