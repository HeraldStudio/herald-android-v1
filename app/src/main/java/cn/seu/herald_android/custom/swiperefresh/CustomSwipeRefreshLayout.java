package cn.seu.herald_android.custom.swiperefresh;

import android.content.Context;
import android.util.AttributeSet;

import cn.seu.herald_android.framework.AppContext;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeColors(AppContext.getColorPrimary(context));
    }
}
