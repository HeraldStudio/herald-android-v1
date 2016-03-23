package cn.seu.herald_android.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.baoyz.widget.PullRefreshLayout;

public class CustomSwipeRefreshLayout extends PullRefreshLayout {
    public boolean noScroll = false;

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN);
        setColor(Color.WHITE);
        setBackgroundColor(ContextCompat.getColor(context, ContextUtils.getColorPrimary(context)));
    }
}
