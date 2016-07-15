package cn.seu.herald_android.mod_query.pedetail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_framework.BaseActivity;

/**
 * MarkedCalendarView | 自定义日历控件
 * 负责显示日历，显示日历上的相关标记，响应用户点击等
 */
public class MarkedCalendarView extends FrameLayout {

    // 用于保存本页每个日期控件的链表
    private List<View> views;

    // 对话框中刷新按钮的点击事件
    public DialogInterface.OnClickListener refreshListener;

    // Constructor
    public MarkedCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 获取特定年月的天数，注意输入的month是从0开始的，与Calendar类的month表示方法一致
    private static int getDayCountForMonth(int year, int month) {
        return new int[]{31, (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) ? 29 : 28,
                31, 30, 31, 30, 31, 31, 30, 31, 30, 31}[month];
    }

    public static int getDayCountForMonth(int yearMonth) {
        return getDayCountForMonth(yearMonth / 12, yearMonth % 12);
    }

    // 初始化月历页面
    public void initialize(int yearMonth, List<PedetailRecordModel> page) {

        Calendar today = Calendar.getInstance();
        today.setFirstDayOfWeek(Calendar.SUNDAY);
        Calendar thisPage = Calendar.getInstance();
        thisPage.setFirstDayOfWeek(Calendar.SUNDAY);

        // 首先将本页的日期改为1，防止当天日期超出本页所在月的日期范围造成月份显示错误
        thisPage.set(Calendar.DATE, 1);

        // 调整本页所在年月
        thisPage.set(Calendar.YEAR, yearMonth / 12);
        thisPage.set(Calendar.MONTH, yearMonth % 12);

        // 初始化容器和链表
        removeAllViews();
        views = new ArrayList<>();

        // 建立子容器
        GridLayout gv = new GridLayout(getContext());
        gv.setColumnCount(7);
        int year = thisPage.get(Calendar.YEAR);
        int month = thisPage.get(Calendar.MONTH);

        // 本页所在月1号的星期（周日为0）
        int day = thisPage.get(Calendar.DAY_OF_WEEK);

        // 开头按1号的星期数增加空格
        for (int i = Calendar.SUNDAY; i < day; i++) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_pedetail__cell, null);
            TextView tv = (TextView) v.findViewById(R.id.textView);
            tv.setVisibility(INVISIBLE);
            gv.addView(v);
        }

        // 放置当月的日期
        for (int i = 1; i <= getDayCountForMonth(year, month); i++) {
            Calendar dt = Calendar.getInstance();
            dt.setFirstDayOfWeek(Calendar.SUNDAY);
            dt.set(year, month, i);
            View v = LayoutInflater.from(getContext()).inflate(R.layout.mod_que_pedetail__cell, null);

            // 当月当天，显示“今天”标记
            int todayYearMonth = today.get(Calendar.YEAR) * 12 + today.get(Calendar.MONTH);
            if (todayYearMonth == yearMonth && today.get(Calendar.DATE) == i) {
                v.findViewById(R.id.today).setVisibility(VISIBLE);
            }

            // 在格子中显示日期数
            TextView tv = (TextView) v.findViewById(R.id.textView);
            tv.setText(String.valueOf(i));

            // 将格子添加到容器和链表
            v.setOnClickListener(v1 -> {
                if (getContext() instanceof BaseActivity) {
                    ((BaseActivity) getContext()).showSnackBar("该日无跑操记录");
                }
            });
            gv.addView(v);
            views.add(v);
        }

        // 添加该页跑操记录
        for (int i = 0; i < page.size(); i++) {
            setMarked(page.get(i));
        }

        // 将容器添加到日历控件
        addView(gv);
    }

    // 为特定跑操记录添加标记，同时指定其弹出对话框中显示的数据
    private void setMarked(PedetailRecordModel info) {
        View v = views.get(info.getDateTime().get(Calendar.DATE) - 1);
        TextView tv = (TextView) v.findViewById(R.id.textView);
        tv.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.pedetail_grid_selector_highlight));
        tv.setTextColor(Color.WHITE);

        v.setOnClickListener(view -> {
            if (getContext() instanceof BaseActivity) {
                ((BaseActivity) getContext()).showSnackBar(info.toString());
            } else {
                new AlertDialog.Builder(getContext()).setTitle("跑操记录详情").setMessage(info.toString()).show();
            }
        });
    }
}
