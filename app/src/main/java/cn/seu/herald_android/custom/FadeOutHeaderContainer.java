package cn.seu.herald_android.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

public class FadeOutHeaderContainer<T extends View> extends FrameLayout {

    private View mask;
    private T view;

    private int maskColor = Color.WHITE;

    public FadeOutHeaderContainer(Context context) {
        super(context);
    }

    public FadeOutHeaderContainer<T> maskColor(int color) {
        maskColor = color;
        return this;
    }

    public FadeOutHeaderContainer<T> append(T v) {
        removeAllViews();
        addView(view = v);

        mask = new View(getContext());
        mask.setLayoutParams(new LayoutParams(-1, -1));
        mask.setBackgroundColor(maskColor);
        mask.setAlpha(0);
        addView(mask);
        return this;
    }

    protected int getScrollTop() {
        return Math.min(-getTop(), getHeight());
    }

    public void syncFadeState() {
        if (getHeight() == 0) return;
        mask.setAlpha(getScrollTop() / (float) getHeight());
    }

    public void syncScrollState() {
        if (getHeight() == 0) return;
        view.setTop(getScrollTop() / 2);
    }

    public T getView() {
        return view;
    }
}
