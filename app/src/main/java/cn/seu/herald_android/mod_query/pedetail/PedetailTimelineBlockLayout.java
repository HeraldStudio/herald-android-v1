package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class PedetailTimelineBlockLayout extends LinearLayout {

    String description;

    public PedetailTimelineBlockLayout(Context context, int count, int remain, int remainDays) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_pe, null);
        TextView _count = (TextView) contentView.findViewById(R.id.tv_count);
        TextView _remain = (TextView) contentView.findViewById(R.id.tv_remain);
        TextView _remainDays = (TextView) contentView.findViewById(R.id.tv_remain_days);
        _count.setText(String.valueOf(count));
        _remain.setText(String.valueOf(remain));
        _remainDays.setText(String.valueOf(remainDays));
        addView(contentView);

        description = _count.getText().toString() + "|"
                + _remain.getText().toString() + "|"
                + _remainDays.getText().toString() + "|";
    }

    @Override
    public String toString() {
        return description;
    }
}
