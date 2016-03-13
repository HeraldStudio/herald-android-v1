package cn.seu.herald_android.custom;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import java.util.HashMap;

public class SliderView extends SliderLayout implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        HashMap<String,String> url_maps = new HashMap<>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        //加载图片
        for(String name : url_maps.keySet()){
            DefaultSliderView sliderView = new DefaultSliderView(getContext());
            // initialize a SliderLayout
            sliderView
                    .image(url_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);

            //add your extra information
            sliderView.bundle(new Bundle());
            sliderView.getBundle()
                    .putString("extra",name);

            addSlider(sliderView);
        }
        //设置轮播选项
        setPresetTransformer(SliderLayout.Transformer.Default);
        //圆点位置
        setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //描述动画
        //sliderLayout.setCustomAnimation(new DescriptionAnimation());
        //切换间隔
        setDuration(4000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        for(ViewParent v = getParent(); v != null; v = v.getParent()) {
            if(v instanceof CustomSwipeRefreshLayout)
                ((CustomSwipeRefreshLayout) v).noScroll = ev.getAction() == MotionEvent.ACTION_MOVE;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {

    }
}
