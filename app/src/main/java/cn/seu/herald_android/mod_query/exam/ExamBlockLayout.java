package cn.seu.herald_android.mod_query.exam;

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

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorExamprimary));
        title.setMaxEms(10);
        count.setTextColor(ContextCompat.getColor(getContext(), R.color.colorExamprimary));

        content.setText(item.time + " @ " + item.location);
        title.setText(item.course);
        if (item.hour.equals("续一秒") || item.hour.equals("+1s") || item.teacher.equals("长者")) {
            subtitle.setText("时长：" + "61s");
        } else {
            subtitle.setText(item.hour + "分钟");
        }

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
