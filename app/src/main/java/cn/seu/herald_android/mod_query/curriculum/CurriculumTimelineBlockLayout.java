package cn.seu.herald_android.mod_query.curriculum;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class CurriculumTimelineBlockLayout extends LinearLayout {

    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;

    public CurriculumTimelineBlockLayout(Context context, ClassModel item, String teacher) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null));
        ButterKnife.bind(this);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorCurriculumPrimary));

        title.setText(item.getClassName());
        subtitle.setText(teacher);
        content.setText(item.getTimePeriod() + " @ " + item.getPlace().replace("(单)", "").replace("(双)", ""));
    }

    @Override
    public String toString() {
        return title.getText().toString() + "|" + subtitle.getText().toString() + "|" + content.getText().toString();
    }
}
