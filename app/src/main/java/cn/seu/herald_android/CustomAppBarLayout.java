package cn.seu.herald_android;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

/**
 * Created by vhyme on 2016/3/8.
 */
public class CustomAppBarLayout extends AppBarLayout {

    public CustomAppBarLayout(Context context) {
        super(context);
    }

    public CustomAppBarLayout(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(0);
    }
}
