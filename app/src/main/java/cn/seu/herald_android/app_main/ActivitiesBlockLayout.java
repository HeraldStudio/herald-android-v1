package cn.seu.herald_android.app_main;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class ActivitiesBlockLayout extends LinearLayout {
    String description;
    ActivitiesItem item;

    public ActivitiesBlockLayout(Context context, ActivitiesItem item) {
        super(context);
        this.item = item;
        View contentView = LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        TextView content = (TextView) contentView.findViewById(R.id.content);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAfterSchoolPrimary));
        title.setText(item.title);
        title.setEllipsize(TextUtils.TruncateAt.END);

        content.setText(item.activity_time + " @ " + item.location);

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
