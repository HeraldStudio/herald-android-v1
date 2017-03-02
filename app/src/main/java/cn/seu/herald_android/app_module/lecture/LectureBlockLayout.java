package cn.seu.herald_android.app_module.lecture;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class LectureBlockLayout extends LinearLayout {

    @BindView(R.id.content)
    TextView content;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.subtitle)
    TextView subtitle;

    public LectureBlockLayout(Context context, LectureNoticeModel item) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null));
        ButterKnife.bind(this);

        title.setText(item.getTopic());
        title.setMinLines(1);
        title.setMaxLines(2);
        title.setMaxEms(10);

        subtitle.setText(item.getSpeaker());

        // 演讲人姓名最多显示5个字符
        subtitle.setMaxEms(5);

        content.setText(item.getDate());
    }

    @Override
    public String toString() {
        return title.getText().toString() + "|"
                + subtitle.getText().toString() + "|"
                + content.getText().toString() + "|";
    }
}
