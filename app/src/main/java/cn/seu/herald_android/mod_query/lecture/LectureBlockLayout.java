package cn.seu.herald_android.mod_query.lecture;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class LectureBlockLayout extends LinearLayout {

    String description;

    public LectureBlockLayout(Context context, LectureNoticeItem item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null);

        TextView content = (TextView) contentView.findViewById(R.id.content);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        TextView subtitle = (TextView) contentView.findViewById(R.id.subtitle);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorLectureprimary));

        title.setText(item.getTopic());
        title.setMinLines(1);
        title.setMaxLines(2);
        title.setMaxEms(10);

        subtitle.setText(item.getSpeaker());

        //演讲人姓名最多显示5个字符
        subtitle.setMaxEms(5);

        content.setText(item.getDate());

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
