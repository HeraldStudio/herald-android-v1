package cn.seu.herald_android.mod_afterschool;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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
        TextView subtitle = (TextView) contentView.findViewById(R.id.subtitle);
        TextView content = (TextView) contentView.findViewById(R.id.content);


        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAfterSchoolPrimary));

        content.setText(item.start_time + " ~ " + item.end_time + " @ " + item.location);
        title.setText(item.title);
        subtitle.setText(item.assiciation);


        addView(contentView);

        description = title.getText().toString() + "|"
                + subtitle.getText().toString() + "|"
                + content.getText().toString() + "|";
    }

    @Override
    public String toString() {
        return description;
    }
}
