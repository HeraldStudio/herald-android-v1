package cn.seu.herald_android.app_module.topic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import cn.seu.herald_android.R;

/**
 * 自定义floatingActionButton控件, 当RecyclerView下滑时自动隐藏, 上划时自动显示
 *
 * Created by corvo on 11/26/16.
 */
public class AnimatedFloatingActionButton extends FloatingActionButton{

    private static final int TRANSLATE_DURATION_MILLIS = 200;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private boolean mVisible;

    public AnimatedFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }


    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }


    private void toggle(final boolean visible, final boolean animate, boolean force) {

        if (mVisible != visible || force) {
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }

            int translationY = visible? 0: height + getMarginBottom();
            if (animate) {
                this.animate().setInterpolator(mInterpolator)
                        .setDuration(TRANSLATE_DURATION_MILLIS)
                        .translationY(translationY);
            } else {
                setTranslationY(translationY);
            }

        }
    }

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams)layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    public void attchToRecyclerView(@NonNull RecyclerView recyclerView) {

      recyclerView.addOnScrollListener(new RecyclerViewScrollDetector() {
          @Override
          void onScrollUp() {
            hide();
          }

          @Override
          void onScrollDown() {
              show();
          }

          @Override
          void setScrollThreashold() {
              setScrollThreashold(getResources().getDimensionPixelOffset(R.dimen.fab_scroll_threshold));

          }
      });
    }
}
