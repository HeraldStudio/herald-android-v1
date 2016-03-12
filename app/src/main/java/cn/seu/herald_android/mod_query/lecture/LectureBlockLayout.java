package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class LectureBlockLayout extends LinearLayout {
    public LectureBlockLayout(Context context, LectureNoticeItem item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.recyclerviewitem_lecture_notice, null);
        TextView tv_date = (TextView)contentView.findViewById(R.id.tv_date);
        TextView tv_location = (TextView)contentView.findViewById(R.id.tv_location);
        TextView tv_speaker = (TextView)contentView.findViewById(R.id.tv_speaker);
        TextView tv_topic = (TextView)contentView.findViewById(R.id.tv_topic);
        tv_location.setText(item.getLocation());
        tv_date.setText(item.getDate());
        tv_speaker.setText(item.getSpeaker());
        tv_topic.setText(item.getTopic());
        contentView.findViewById(R.id.line_divider).setVisibility(GONE);
        float dp = context.getResources().getDisplayMetrics().density;
        contentView.setLayoutParams(new LayoutParams(-1, -2));
        contentView.setBackground(context.getResources().getDrawable(R.drawable.timeline_attached_block_bg));
        addView(contentView);
    }
}
