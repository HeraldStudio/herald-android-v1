package cn.seu.herald_android.mod_query.experiment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class ExperimentBlockLayout extends LinearLayout {
    public ExperimentBlockLayout(Context context, ExperimentItem item){
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.expandablelistview_childitem_experiment,null);
        TextView tv_name = (TextView)contentView.findViewById(R.id.tv_name);
        TextView tv_date = (TextView)contentView.findViewById(R.id.tv_date);
        TextView tv_day = (TextView)contentView.findViewById(R.id.tv_day);
        TextView tv_teacher = (TextView)contentView.findViewById(R.id.tv_teacher);
        TextView tv_address = (TextView)contentView.findViewById(R.id.tv_address);
        TextView tv_grade = (TextView)contentView.findViewById(R.id.tv_grade);
        tv_name.setText(item.name);
        tv_date.setText("实验日期："+item.date);
        tv_day.setText("时间段："+item.time);
        tv_teacher.setText("指导老师："+item.teacher);
        tv_address.setText("实验地点："+item.address);
        tv_grade.setText(item.grade.replace("null", ""));
        contentView.setLayoutParams(new LayoutParams(-1, -2));
        contentView.setBackground(context.getResources().getDrawable(R.drawable.timeline_attached_block_bg));
        addView(contentView);
    }
}
