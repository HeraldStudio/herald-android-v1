package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    public boolean noScroll = false;

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        return noScroll || super.canChildScrollUp();
    }
}
