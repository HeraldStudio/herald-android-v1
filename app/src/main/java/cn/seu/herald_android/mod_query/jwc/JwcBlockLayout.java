package cn.seu.herald_android.mod_query.jwc;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;

public class JwcBlockLayout extends LinearLayout {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;

    public JwcBlockLayout(Context context, JwcNoticeModel item) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null));
        ButterKnife.bind(this);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJwcprimary));
        title.setMinLines(1);
        title.setMaxLines(2);

        title.setText(item.title);
        content.setText("发布时间：" + item.date);

        setOnClickListener(v -> {
            Uri uri = Uri.parse(item.href);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @Override
    public String toString() {
        return title.getText().toString() + "|"
                + content.getText().toString() + "|";
    }
}
