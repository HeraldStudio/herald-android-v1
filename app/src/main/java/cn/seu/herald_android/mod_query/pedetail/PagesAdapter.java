package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seu.herald_android.R;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public class PagesAdapter extends PagerAdapter {

    private Map<Integer, List<ExerciseInfo>> multiMonths;

    private List<View> viewList;

    private List<Integer> subCounts;

    public PagesAdapter(Map<Integer, List<ExerciseInfo>> months, Context context, Runnable refresh) {
        // 复制这个键值对
        multiMonths = new HashMap<>(months);
        viewList = new ArrayList<>();
        subCounts = new ArrayList<>();

        // 为了使每页的起始点与前一页的结束点相同，定义一个外部变量用来在不同页面之间做中转
        float lastDayValue = 0;

        // 将键的集合转换为列表并排序
        List<Integer> keyList = new ArrayList<>(multiMonths.keySet());
        Collections.sort(keyList);

        for (Integer k : keyList) {

            List<ExerciseInfo> page = multiMonths.get(k);

            // 实例化页面视图
            View v = LayoutInflater.from(context).inflate(R.layout.viewpager_pedetail, null);

            // 设置月份标题
            TextView tv = (TextView) v.findViewById(R.id.month_title);
            tv.setText(getMonthTitle(k));

            // 初始化月历视图
            MarkedCalendarView calendar = (MarkedCalendarView) v.findViewById(R.id.calendar);
            calendar.refreshListener = ((dialog, which) -> refresh.run());
            calendar.initialize(k, page);

            // 绘制折线图
            List<PointValue> mPointValues = new ArrayList<>();
            int M = page.size();

            subCounts.add(M);

            // 新建一个数组，表示该月每一天的活动情况，初始化为零数组
            int dateCount = MarkedCalendarView.getDayCountForMonth(k);
            float[] values = new float[dateCount];
            for (int j = 0; j < dateCount; j++) {
                values[j] = 0;
            }

            // 计算活跃度并填入数据
            for (int j = 0; j < M; j++) {
                Calendar c = page.get(j).getDateTime();
                int time = c.get(Calendar.HOUR) * 60 + c.get(Calendar.MINUTE) - 400;
                if (time < 0) time = 0;
                if (time > 40) time = 40;
                float value = 1 - time / 40.0f;
                values[c.get(Calendar.DATE) - 1] = value;
            }

            // 先绘制上一页的最后一个数据，然后存储本页的最后一个数据
            mPointValues.add(new PointValue(0, lastDayValue));
            lastDayValue = values[dateCount - 1];

            // 将数组中的数据转换为数据表
            for (int j = 0; j < dateCount; j++) {
                PointValue point = new PointValue(j + 1, values[j]);
                mPointValues.add(point);
            }

            // 创建图表
            LineChartView chart = (LineChartView) v.findViewById(R.id.chart);
            chart.setInteractive(false);
            List<Line> lines = new ArrayList<>();

            // 绘制水平分割线
            for (float j = 0; j <= 1.1f; j += 0.4f) {
                List<PointValue> values1 = new ArrayList<>();
                values1.add(new PointValue(0, j));
                values1.add(new PointValue(dateCount, j));
                lines.add(new Line(values1)
                        .setColor(ContextCompat.getColor(context, R.color.colorPedetailprimary))
                        .setStrokeWidth(1)
                        .setHasPoints(false));
            }

            // 绘制竖直分割线
            Calendar cal = Calendar.getInstance();
            cal.set(k / 12, k % 12, 1);
            for (int j = 1; j <= dateCount; j++) {
                cal.set(Calendar.DATE, j);
                List<PointValue> values1 = new ArrayList<>();
                values1.add(new PointValue(j, -0.1f));
                values1.add(new PointValue(j, 1.1f));
                lines.add(new Line(values1)
                        .setColor(ContextCompat.getColor(context, R.color.colorPedetailprimary))
                        .setStrokeWidth(1)
                        .setHasPoints(false));

                // 在周六周日之间添加标记线
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    List<PointValue> values2 = new ArrayList<>();
                    values2.add(new PointValue(j - 0.5f, -0.1f));
                    values2.add(new PointValue(j - 0.5f, 0.15f));
                    lines.add(new Line(values2).setStrokeWidth(1).setHasPoints(false).setColor(Color.WHITE));
                }
            }

            // 将数据表转换为折线图
            lines.add(new Line(mPointValues)
                    .setColor(Color.WHITE)
                    .setStrokeWidth(2)
                    .setCubic(false)
                    .setFilled(true)
                    .setAreaTransparency(128)
                    .setHasPoints(false));

            // 加一条“7”字形的透明线，将显示区域扩大到(0<=x<=dateCount, -0.1<=y<=1.1)
            List<PointValue> higherValues = new ArrayList<>();
            higherValues.add(new PointValue(0, 1.1f));
            higherValues.add(new PointValue(dateCount, 1.1f));
            higherValues.add(new PointValue(dateCount, -0.1f));
            lines.add(new Line(higherValues).setColor(Color.TRANSPARENT).setHasPoints(false));

            // 绘制折线图
            LineChartData data = new LineChartData();
            data.setLines(lines);
            data.setBaseValue(-0.1f);//设置填充颜色的基线为y=-0.1，即在y=-0.1与折线之间进行填充
            chart.setLineChartData(data);

            // 向容器中添加该页
            viewList.add(v);
        }
    }

    @Override
    public int getCount() {
        return multiMonths.size();
    }

    public int getSubCount(int index) {
        return subCounts.get(index);
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

    public View getViewForItem(int position) {
        return viewList.get(position);
    }

    public String getMonthTitle(int yearMonth) {
        return yearMonth / 12 + "年" + (yearMonth % 12 + 1) + "月";
    }
}
