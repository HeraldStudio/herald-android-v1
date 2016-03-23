package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

public class CustomAppBarLayout extends AppBarLayout {

    public CustomAppBarLayout(Context context) {
        super(context);
    }

    public CustomAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(0);
    }
}
