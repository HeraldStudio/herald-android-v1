package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.util.AttributeSet;

public class CustomCollapsingToolbarLayout extends CollapsingToolbarLayout {

    public CustomCollapsingToolbarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTitleEnabled(false);
    }
}
