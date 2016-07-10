package cn.seu.herald_android.mod_query.curriculum;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.AppContext;

/**
 * 课程表视图的实现
 * 注：本程序目前当日期改变时需要在主程序中手动重载，若不重载可能会出现显示错误等情况
 */
@SuppressLint("ViewConstructor")
public class CurriculumScheduleLayout extends FrameLayout {

    // 常量，我校一天的课时数
    private static final int PERIOD_COUNT = 13;
    // 常量，今天所在列与其他列的宽度比值
    private static final float TODAY_WEIGHT = 1.2f;
    // 星期在JSON中的表示值
    public static final String[] WEEK_NUMS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    // 星期在屏幕上的显示值
    public static final String[] WEEK_NUMS_CN = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    // 以上的星期依次在Calendar中对应的值
    private static final int[] WEEK_NUMS_CALENDAR = {
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY,
            Calendar.SUNDAY
    };
    // 每节课开始的时间，以(Hour * 60 + Minute)形式表示
    // 本程序假定每节课都是45分钟
    public static final int[] CLASS_BEGIN_TIME = {
            8 * 60, 8 * 60 + 50, 9 * 60 + 50, 10 * 60 + 40, 11 * 60 + 30,
            14 * 60, 14 * 60 + 50, 15 * 60 + 50, 16 * 60 + 40, 17 * 60 + 30,
            18 * 60 + 30, 19 * 60 + 20, 20 * 60 + 10
    };
    // 表示当前需要显示的列数，初始值为7，若周六或周日无课，对应地减去1或2
    private int columnsCount;
    // 表示周数，视图的宽度和高度
    private int week, width = 0, height = 0;
    // 是否当前周
    private boolean curWeek;
    // 表示当前学期课程信息的JSON对象
    private JSONObject obj;
    // 表示屏幕缩放率（平均每个dp中px的数量）
    private float density;
    // 当前时间的指示条（仅当本页为当前周、今天非休息日或有课时才会显示）
    private View timeHand;
    private BroadcastReceiver timeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTimeHand();
        }
    };
    // 保存当前学期侧栏的键值对
    private Map<String, Pair<String, String>> sidebar;

    // 本视图只需要手动创建，不会从xml中创建
    public CurriculumScheduleLayout(Context context, JSONObject obj,
                                    Map<String, Pair<String, String>> sidebar, int week,
                                    boolean curWeek) {
        super(context);
        this.obj = obj;
        this.sidebar = sidebar;
        this.week = week;
        this.curWeek = curWeek;
    }

    // 获取手机状态栏高度
    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    // 要显示在屏幕上时再进行添加view的操作，显著提高应用启动速度
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        try {
            // 获取屏幕缩放率、宽度和高度，并计算页面要占的高度（总高度-标题栏高度-系统顶栏高度）
            DisplayMetrics dm = getResources().getDisplayMetrics();
            density = dm.density;
            width = dm.widthPixels;
            height = dm.heightPixels - (int) (48 * density) - getStatusBarHeight(getContext());

            // 绘制表示各课时的水平分割线
            for (int i = 0; i < PERIOD_COUNT; i++) {
                View v = new View(getContext());
                v.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.curriculumstripeColor));
                v.setLayoutParams(new LayoutParams(-1, 1));
                v.setY((i + 1) * height / (PERIOD_COUNT + 1));
                addView(v);
            }

            // 取得一个Calendar用来表示当天
            Calendar today = Calendar.getInstance();
            today.setFirstDayOfWeek(Calendar.MONDAY);

            // 首先假设7天都有课
            columnsCount = 7;

            // 用Calendar星期格式表示的当天的星期
            int todayDayInCal = today.get(Calendar.DAY_OF_WEEK);

            // 用数组下标表示的当天的星期
            int todayDayInArr = 0;
            for (int j = 0; j < 7; j++) {
                if (WEEK_NUMS_CALENDAR[j] == todayDayInCal)
                    todayDayInArr = j;
            }

            // 双重列表，用每个子列表表示一天的课程
            List<List<ClassInfo>> listOfList = new ArrayList<>();

            // 是否有无法读取的课程, 如辅修课
            boolean hasInvalid = false;

            // 放两个循环是为了先把列数确定下来
            for (int i = 0; i < 7; i++) {

                // 用JSON中对应的String表示的该日星期
                JSONArray array = obj.getJSONArray(WEEK_NUMS[i]);

                // 剔除不属于本周的课程，并将对应的课程添加到对应星期的列表中
                List<ClassInfo> list = new ArrayList<>();
                for (int j = 0; j < array.length(); j++) {
                    try {
                        ClassInfo info = new ClassInfo(array.getJSONArray(j));
                        info.weekNum = WEEK_NUMS_CN[i];
                        int startWeek = info.getStartWeek();
                        int endWeek = info.getEndWeek();
                        if (endWeek >= week && startWeek <= week && info.isFitEvenOrOdd(week))
                            list.add(info);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        hasInvalid = true;
                    }
                }

                // 根据周六或周日无课的天数对列数进行删减
                if ((i >= 5) && list.size() == 0) {
                    columnsCount--;
                }

                // 将子列表添加到父列表
                listOfList.add(list);
            }

            // 有无效课程时的提示
            if (hasInvalid) {
                AppContext.showMessage("暂不支持导入辅修课，敬请期待后续版本。");
            }

            // 确定好实际要显示的列数后，将每列数据交给子函数处理
            for (int i = 0, j = 0; i < 7; i++) {
                List<ClassInfo> list = listOfList.get(i);
                if (list.size() != 0 || i < 5) {

                    setColumnData(
                            list, // 这一列的数据
                            j, // 该列在所有实际要显示的列中的序号
                            i, // 该列在所有列中的序号
                            i - todayDayInArr, // 该列的星期数与今天星期数之差
                            // 是否突出显示与今天同星期的列
                            curWeek && (todayDayInArr < 5 ||
                                    listOfList.get(todayDayInArr).size() != 0));
                    j++;
                }
            }

            // 如果是本周，定时刷新时间指示条
            if (curWeek) {
                refreshTimeHand();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_TIME_TICK);
                intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
                intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                getContext().registerReceiver(timeChangeReceiver, intentFilter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 绘制某一列的课表
    private void setColumnData(
            List<ClassInfo> list, // 该列的数据
            int columnIndex, // 该列在所有要显示的列中的序号
            int dayIndex, // 该列在所有列中的序号
            int dayDelta, // 该列的星期号与今天星期号之差
            boolean widenToday // 是否突出显示与今天同星期的列
    ) {
        int N = list.size();
        float addition = widenToday ? TODAY_WEIGHT - 1 : 0;

        // 绘制星期标题
        View v = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_curriculum__cell_week, null);
        TextView week = (TextView) v.findViewById(R.id.week);
        week.setText(WEEK_NUMS_CN[dayIndex]);
        v.setX((dayDelta > 0 ? columnIndex + addition : columnIndex) * width / (columnsCount + addition));
        v.setY(0);
        v.setLayoutParams(new LayoutParams(
                (int) ((dayDelta == 0 && widenToday ? TODAY_WEIGHT : 1) * width / (columnsCount + addition)),
                height / (PERIOD_COUNT + 1)));
        addView(v);

        // 显示当天星期标题下面的高亮条
        if (widenToday && dayDelta == 0) {
            v.findViewById(R.id.stripe).setVisibility(VISIBLE);
        }

        // 绘制每列的竖直分割线
        View v1 = new View(getContext());
        v1.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.curriculumstripeColor));
        v1.setLayoutParams(new LayoutParams(1, -1));
        v1.setX(v.getX());
        addView(v1);

        // 绘制每节课的方块
        for (int i = 0; i < N; i++) {
            ClassInfo info = list.get(i);

            CurriculumScheduleBlockLayout block = new CurriculumScheduleBlockLayout(
                    getContext(), info, sidebar.get(info.getClassName()),
                    widenToday && dayDelta == 0);

            // 向右偏移
            block.setX(
                    (dayDelta > 0 && widenToday ?
                            // 如果今天相同星期被突出显示了，而且本列在今天相同星期的右侧
                            // 为前面突出显示造成的偏移做补偿
                            columnIndex + TODAY_WEIGHT - 1 :
                            // 否则不需要补偿
                            columnIndex)
                            // 乘以每列的宽度
                            * width / (columnsCount + addition)
            );

            // 向下偏移
            block.setY((info.getStartTime()) * height / (PERIOD_COUNT + 1));

            // 宽度和高度
            block.setLayoutParams(new LayoutParams(
                    // 宽度，如果今天相同星期被突出显示，且本列就是今天相同星期，就加宽，否则不加宽
                    (int) ((dayDelta == 0 && widenToday ? TODAY_WEIGHT : 1)
                            // 乘以每列的宽度
                            * width / (columnsCount + addition)),
                    // 高度
                    info.getPeriodCount() * height / (PERIOD_COUNT + 1)));
            addView(block);
        }

        // 绘制时间指示条
        if (dayDelta == 0 && widenToday) {
            timeHand = new View(getContext());
            timeHand.setLayoutParams(new LayoutParams((int) (TODAY_WEIGHT * width / (columnsCount + TODAY_WEIGHT - 1)), (int) density * 2));
            timeHand.setX(columnIndex * width / (columnsCount + TODAY_WEIGHT - 1));
            timeHand.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.curriculumtimeHandColor));
            addView(timeHand);
        }
    }

    // 刷新时间指示条
    private void refreshTimeHand() {
        Calendar now = Calendar.getInstance();
        int time = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);

        // 初始位置在星期标题之下，第一节课之上
        float timeHandPosition = 1 / (float) (PERIOD_COUNT + 1);

        // 判断当前时间
        for (int i = 0; i < PERIOD_COUNT; i++) {
            if (CLASS_BEGIN_TIME[i] <= time && time < CLASS_BEGIN_TIME[i] + 45) {
                // 上课时间
                timeHandPosition = (i + (time - CLASS_BEGIN_TIME[i]) / 45f + 1) / (PERIOD_COUNT + 1);
                break;
            } else if (CLASS_BEGIN_TIME[i] + 45 <= time && (i == PERIOD_COUNT - 1 || time < CLASS_BEGIN_TIME[i + 1])) {
                // 下课时间
                timeHandPosition = (i + 2) / (float) (PERIOD_COUNT + 1);
                break;
            }
        }

        // 应用新的位置
        if (timeHand != null && height != 0) {
            timeHand.setY(height * timeHandPosition - (int) density);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeAllViews();
        // 防火防盗防泄漏
        if (curWeek) {
            getContext().unregisterReceiver(timeChangeReceiver);
        }
        super.onDetachedFromWindow();
    }
}
