package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class PedetailTimelineBlockLayout extends LinearLayout {

    public PedetailTimelineBlockLayout(Context context, String desc, int number) {
        this(context, desc, String.valueOf(number));
    }

    public PedetailTimelineBlockLayout(Context context, String desc, String str) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.timeline_item_pe, null);
        TextView tv_desc = (TextView) contentView.findViewById(R.id.tv_desc);
        TextView tv_count = (TextView) contentView.findViewById(R.id.tv_count);
        tv_desc.setText(desc);
        tv_count.setText(str);
        contentView.setLayoutParams(new LayoutParams(-1, -2));
        contentView.setBackground(ContextCompat.getDrawable(context, R.drawable.timeline_attached_block_bg));
        addView(contentView);
    }
}
