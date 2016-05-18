package cn.seu.herald_android.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

/**
 * Created by heyon on 2016/5/16.
 */
public class ExpandableGridView extends GridView {

    boolean expanded = false;
    public boolean isExpanded()
    {
        return expanded;
    }

    public ExpandableGridView(Context context, boolean expanded) {
        super(context);
        this.expanded = expanded;
    }

    public ExpandableGridView(Context context, AttributeSet attrs, boolean expanded) {
        super(context, attrs);
        this.expanded = expanded;
    }

    public ExpandableGridView(Context context, AttributeSet attrs, int defStyleAttr, boolean expanded) {
        super(context, attrs, defStyleAttr);
        this.expanded = expanded;
    }


    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {

        if (isExpanded())
        {
            // Calculate entire height by providing a very large height hint.
            // But do not use the highest 2 bits of this integer; those are
            // reserved for the MeasureSpec mode.
            int expandSpec = View.MeasureSpec.makeMeasureSpec(
                    Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }

}