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

    String description;

    public JwcBlockLayout(Context context, JwcNoticeModel item) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null);
        TextView title = (TextView) contentView.findViewById(R.id.title);
        TextView content = (TextView) contentView.findViewById(R.id.content);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJwcprimary));
        title.setMinLines(1);
        title.setMaxLines(2);

        title.setText(item.title);
        content.setText("发布时间：" + item.date);
        addView(contentView);

        setOnClickListener(v -> {
            Uri uri = Uri.parse(item.href);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        description = title.getText().toString() + "|"
                + content.getText().toString() + "|";
    }

    @Override
    public String toString() {
        return description;
    }
}
