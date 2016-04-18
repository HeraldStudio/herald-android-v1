package cn.seu.herald_android.custom.swiperefresh;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import cn.seu.herald_android.custom.ContextUtils;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeColors(ContextCompat.getColor(context, ContextUtils.getColorPrimary(context)));
    }
}
