package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import cn.seu.herald_android.framework.AppContext;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColorSchemeColors(AppContext.getColorPrimary(context));
    }

    public boolean innerScrolling = false;

    @Override
    public boolean canChildScrollUp() {
        return super.canChildScrollUp() || innerScrolling;
    }
}
