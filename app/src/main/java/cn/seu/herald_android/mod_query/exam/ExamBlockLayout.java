package cn.seu.herald_android.mod_query.exam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class ExamBlockLayout extends LinearLayout {

    public ExamBlockLayout(Context context, ExamItem item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.timeline_item_exam, null);
        TextView tv_time = (TextView) contentView.findViewById(R.id.tv_time);
        TextView tv_course = (TextView) contentView.findViewById(R.id.tv_course);
        TextView tv_location = (TextView) contentView.findViewById(R.id.tv_location);
        TextView tv_teacher = (TextView) contentView.findViewById(R.id.tv_teacher);
        TextView tv_hour = (TextView) contentView.findViewById(R.id.tv_hour);
        TextView tv_numtitle = (TextView) contentView.findViewById(R.id.tv_numtitle);
        TextView tv_num = (TextView) contentView.findViewById(R.id.tv_num);

        tv_time.setText("时间：" + item.time);
        tv_course.setText(item.course);
        tv_location.setText("地点：" + item.location);
        tv_teacher.setText("教师：" + item.teacher);
        tv_hour.setText("时长：" + item.hour + "分钟");

        try {
            int remainingDays = item.getRemainingDays();
            tv_num.setText(String.valueOf(remainingDays));

            if (remainingDays < 0) {
                tv_numtitle.setText("已结束");
            } else if (remainingDays == 0) {
                tv_numtitle.setText("今天考试");
            } else {
                tv_numtitle.setText("剩余天数");
            }
        } catch (Exception e) {
            tv_numtitle.setText("");
            tv_num.setText("");
        }

        addView(contentView);
    }
}
