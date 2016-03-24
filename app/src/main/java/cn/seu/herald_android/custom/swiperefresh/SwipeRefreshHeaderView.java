package cn.seu.herald_android.custom.swiperefresh;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.seu.herald_android.R;

public class SwipeRefreshHeaderView extends FrameLayout {

    private RelativeLayout contentView;

    private TextView rightText;

    public SwipeRefreshHeaderView(Context context) {
        super(context);
        contentView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.swipe_refresh_header, null);
        rightText = (TextView) contentView.findViewById(R.id.text_right);
        contentView.setLayoutParams(new LayoutParams(-1, -1));
        addView(contentView);
    }

    public void setImageDrawable(Drawable drawable) {
        ((ImageView) contentView.findViewById(R.id.swipe_image)).setImageDrawable(drawable);
    }

    public void notifyRefreshing() {
        rightText.setText("正在刷新");
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        contentView.setLayoutParams(new LayoutParams(-1, getHeight()));

        float dp = getResources().getDisplayMetrics().density;
        float refreshDistance = PullRefreshLayout.DRAG_MAX_DISTANCE * dp;
        rightText.setText(getHeight() > refreshDistance ? "松手刷新" : "下拉刷新");
        if (getParent() instanceof PullRefreshLayout) {
            if (((PullRefreshLayout) getParent()).isRefreshing()) {
                notifyRefreshing();
            }
        }
    }
}
