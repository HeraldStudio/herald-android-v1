package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.CalendarUtils;

public class ExperimentBlockLayout extends LinearLayout {

    private long time = 0;

    public ExperimentBlockLayout(Context context, ExperimentItem item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.expandablelistview_childitem_experiment, null);
        TextView tv_name = (TextView) contentView.findViewById(R.id.tv_name);
        TextView tv_date = (TextView) contentView.findViewById(R.id.tv_date);
        TextView tv_day = (TextView) contentView.findViewById(R.id.tv_day);
        TextView tv_teacher = (TextView) contentView.findViewById(R.id.tv_teacher);
        TextView tv_address = (TextView) contentView.findViewById(R.id.tv_address);
        TextView tv_grade = (TextView) contentView.findViewById(R.id.tv_grade);
        tv_name.setText(item.name);
        tv_date.setText("实验日期：" + item.date);
        tv_day.setText("时间段：" + item.time);
        tv_teacher.setText("指导老师：" + item.teacher);
        tv_address.setText("实验地点：" + item.address);
        tv_grade.setText(item.grade.replace("null", ""));
        contentView.setLayoutParams(new LayoutParams(-1, -2));
        contentView.setBackground(ContextCompat.getDrawable(context, R.drawable.timeline_attached_block_bg));
        addView(contentView);

        String[] ymdStr = item.date.replace("年", "-").replace("月", "-").replace("日", "-").split("-");
        int year = Integer.valueOf(ymdStr[0]);
        int month = Integer.valueOf(ymdStr[1]);
        int date = Integer.valueOf(ymdStr[2]);
        int hour = Integer.valueOf(item.time.split(":")[0]);

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, date);
        cal = CalendarUtils.toSharpDay(cal);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        time = cal.getTimeInMillis();
    }

    public long getTime() {
        return time;
    }
}
