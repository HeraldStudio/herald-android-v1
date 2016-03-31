package cn.seu.herald_android.mod_query.jwc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class JwcBlockLayout extends LinearLayout {
    private int importance = 0;

    public JwcBlockLayout(Context context, JwcItem item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.timeline_item_jwc, null);
        TextView tv_name = (TextView) contentView.findViewById(R.id.tv_name);
        TextView tv_date = (TextView) contentView.findViewById(R.id.tv_date);
        importance = item.title.contains("!") || item.title.contains("重要") || item.title.contains("提醒") ? 1 : 0;
        if (importance == 1) {
            tv_name.getPaint().setFakeBoldText(true);
        }
        tv_name.setText(item.title);
        tv_date.setText(item.date);
        contentView.setLayoutParams(new LayoutParams(-1, -2));
        contentView.setBackground(ContextCompat.getDrawable(context, R.drawable.timeline_attached_block_bg));
        addView(contentView);

        setOnClickListener(v -> {
            Uri uri = Uri.parse(item.href);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    public int getImportance() {
        return importance;
    }
}
