package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class CurriculumTimelineBlockLayout extends LinearLayout {

    String description;

    public CurriculumTimelineBlockLayout(Context context, ClassInfo item, String teacher) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null);
        TextView content = (TextView) contentView.findViewById(R.id.content);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        TextView subtitle = (TextView) contentView.findViewById(R.id.subtitle);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorCurriculumPrimary));

        title.setText(item.getClassName());
        subtitle.setText(teacher);
        content.setText(item.getTimePeriod() + " @ " + item.getPlace().replace("(单)", "").replace("(双)", ""));
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
