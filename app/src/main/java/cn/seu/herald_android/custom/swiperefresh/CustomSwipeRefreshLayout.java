package cn.seu.herald_android.custom.swiperefresh;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import cn.seu.herald_android.R;

public class CustomSwipeRefreshLayout extends PullRefreshLayout {
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColor(ContextCompat.getColor(context, R.color.colorSwipeRefreshIcon));
        setBackgroundColor(ContextCompat.getColor(context, R.color.colorSwipeRefreshHeaderShadow));
    }
}
