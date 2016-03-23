package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class CurriculumTimelineBlockLayout extends LinearLayout {

    public CurriculumTimelineBlockLayout(Context context, ClassInfo item, String teacher) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.timeline_item_curriculum, null);
        TextView tv_time = (TextView) contentView.findViewById(R.id.tv_time);
        TextView tv_name = (TextView) contentView.findViewById(R.id.tv_name);
        TextView tv_address = (TextView) contentView.findViewById(R.id.tv_address);
        TextView tv_teacher = (TextView) contentView.findViewById(R.id.tv_teacher);
        tv_name.setText(item.getClassName());
        tv_time.setText("上课时间：" + item.getTimePeriod());
        tv_address.setText("地点：" + item.getPlace().replace("(单)", "").replace("(双)", ""));
        tv_teacher.setText("教师：" + teacher);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        contentView.setBackground(ContextCompat.getDrawable(context, R.drawable.timeline_attached_block_bg));
        addView(contentView);
    }
}
