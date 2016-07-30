package cn.seu.herald_android.app_module.jwc;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.seu.herald_android.R;
import cn.seu.herald_android.app_secondary.WebModuleActivity;

public class JwcBlockLayout extends LinearLayout {

    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.content)
    TextView content;

    public JwcBlockLayout(Context context, JwcNoticeModel item) {
        super(context);
        addView(LayoutInflater.from(context).inflate(R.layout.app_main__fragment_cards__item_row, null));
        ButterKnife.bind(this);

        title.setTextColor(ContextCompat.getColor(getContext(), R.color.colorJwcPrimary));
        title.setMinLines(1);
        title.setMaxLines(2);

        title.setText(item.title);
        content.setText("发布时间：" + item.date);

        setOnClickListener(v -> WebModuleActivity.startWebModuleActivity(item.title, item.href, R.style.JwcTheme));
    }

    @Override
    public String toString() {
        return title.getText().toString() + "|"
                + content.getText().toString() + "|";
    }
}
