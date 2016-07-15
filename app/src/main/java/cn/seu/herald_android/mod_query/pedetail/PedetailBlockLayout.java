package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class PedetailBlockLayout extends LinearLayout {

    @BindView(R.id.tv_count)
    TextView count;
    @BindView(R.id.tv_remain)
    TextView remain;
    @BindView(R.id.tv_remain_days)
    TextView remainDays;

    public PedetailBlockLayout(Context context, int count, int remain, int remainDays) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_pe, null));
        ButterKnife.bind(this);

        this.count.setText(String.valueOf(count));
        this.remain.setText(String.valueOf(remain));
        this.remainDays.setText(String.valueOf(remainDays));
    }

    @Override
    public String toString() {
        return count.getText().toString() + "|"
                + remain.getText().toString() + "|"
                + remainDays.getText().toString() + "|";
    }
}
