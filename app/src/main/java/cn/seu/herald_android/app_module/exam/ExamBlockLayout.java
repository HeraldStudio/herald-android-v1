package cn.seu.herald_android.app_module.exam;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class ExamBlockLayout extends LinearLayout {

    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;
    @BindView(R.id.num)
    TextView count;

    public ExamBlockLayout(Context context, ExamModel item) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null));
        ButterKnife.bind(this);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorExamPrimary));
        title.setMaxEms(10);
        count.setTextColor(ContextCompat.getColor(getContext(), R.color.colorExamPrimary));

        String location = "";
        if (!item.location.trim().equals("")) {
            location = " @ " + item.location;
        }

        content.setText(item.time + location);
        title.setText(item.course);

        String hour = "";
        if (!item.hour.trim().equals("")) {
            hour = " @ " + item.hour;
        }
        subtitle.setText(hour);

        try {
            int remainingDays = item.getRemainingDays();
            count.setText(remainingDays + "天");
        } catch (Exception e) {
            count.setText("");
        }
    }

    @Override
    public String toString() {
        return title.getText().toString() + "|"
                + subtitle.getText().toString() + "|"
                + content.getText().toString() + "|"
                + count.getText().toString() + "|";
    }
}
