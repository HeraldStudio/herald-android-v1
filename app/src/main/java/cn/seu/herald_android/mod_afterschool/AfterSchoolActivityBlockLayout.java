package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;
import cn.seu.herald_android.mod_timeline.TimelineItem;

/**
 * Created by heyon on 2016/5/10.
 */
public class AfterSchoolActivityBlockLayout extends LinearLayout {
    String description;
    AfterSchoolActivityItem item;

    public AfterSchoolActivityBlockLayout(Context context, AfterSchoolActivityItem item) {
        super(context);
        this.item = item;
        View contentView = LayoutInflater.from(context).inflate(R.layout.timeline_item_row, null);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        //TextView subtitle = (TextView) contentView.findViewById(R.id.subtitle);
        TextView content = (TextView) contentView.findViewById(R.id.content);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAfterSchoolPrimary));
        title.setText(item.title);
        title.setEllipsize(TextUtils.TruncateAt.END);


        content.setText(item.activity_time + " @ " + item.location);



        //子标题为活动是否开始
        //subtitle.setText(item.getTag());



        addView(contentView);

        description = item.title + "|"
                + item.getTag() + "|"
                + item.introduciton + "|";
    }

    @Override
    public String toString() {
        return description;
    }
}
