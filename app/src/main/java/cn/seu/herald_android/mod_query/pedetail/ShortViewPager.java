package cn.seu.herald_android.mod_query.pedetail;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

public class ShortViewPager extends ViewPager {

    private Map<Integer, Integer> viewHeights = new HashMap<>();

    public ShortViewPager(Context context) {
        super(context);
    }

    public ShortViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        viewHeights = new HashMap<>();
        super.setAdapter(adapter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = 0;
        PagesAdapter adapter = (PagesAdapter) getAdapter();
        if (adapter != null) {
            View child = adapter.getViewForItem(getCurrentItem());
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight();
            if (h != height) height = h;

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onPageScrolled(int position, float offset, int offsetPixels) {

        float pos = position + offset;
        int leftPos = (int) Math.floor(pos);
        int rightPos = (int) Math.ceil(pos);

        if (getAdapter() != null) {
            int height = (int) (getChildHeight(leftPos) * (1 - offset) + getChildHeight(rightPos) * offset);
            setLayoutParams(new FrameLayout.LayoutParams(-1, height));
        }
        super.onPageScrolled(position, offset, offsetPixels);
    }

    private int getChildHeight(int position) {

        if (getAdapter() == null || !(getAdapter() instanceof PagesAdapter))
            return 0;

        if (viewHeights.get(position) == null) {
            PagesAdapter adapter = (PagesAdapter) getAdapter();
            View child = adapter.getViewForItem(position);
            int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            child.measure(spec, spec);

            int height = child.getMeasuredHeight();
            viewHeights.put(position, height);
            return height;
        } else {
            return viewHeights.get(position);
        }
    }
}