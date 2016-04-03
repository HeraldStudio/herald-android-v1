package cn.seu.herald_android.custom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class CustomRecyclerView extends RecyclerView {
    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addItemDecoration(new SimpleDividerItemDecoration(context));
    }
}
