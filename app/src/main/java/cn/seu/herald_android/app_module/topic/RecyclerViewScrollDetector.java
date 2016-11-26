package cn.seu.herald_android.app_module.topic;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by corvo on 11/26/16.
 */

public abstract class RecyclerViewScrollDetector extends RecyclerView.OnScrollListener {
    private int mScrollThreashold;

    abstract void onScrollUp();

    abstract void onScrollDown();

    abstract void setScrollThreashold();

    /**
     * 在recyclerview 滑动过程中监听滑动变化
     * @param recyclerView
     * @param dx
     * @param dy
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        boolean isSignificantDelta = Math.abs(dy) > mScrollThreashold;
        if (isSignificantDelta) {
            if (dy > 0) {
                onScrollUp();
            }
            else {
                onScrollDown();
            }
        }
    }

    public void setScrollThreashold(int scrollThreashold) {
        mScrollThreashold = scrollThreashold;
    }
}
