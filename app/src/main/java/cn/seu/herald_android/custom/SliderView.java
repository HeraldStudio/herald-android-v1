package cn.seu.herald_android.custom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import java.util.ArrayList;
import java.util.HashMap;

import cn.seu.herald_android.R;

public class SliderView extends SliderLayout implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    ArrayList<SliderViewItem> sliderViewItemArrayList;
    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setupWithArrayList(ArrayList<SliderViewItem> sliderViewItemArrayList){
        //利用arraylist进行初始化
        this.sliderViewItemArrayList = sliderViewItemArrayList;
        try{
            for(SliderViewItem sliderViewItem :sliderViewItemArrayList){
                addSlider(getDefultSliderViewWithUrl(
                        sliderViewItem.getTitle(),
                        sliderViewItem.getImageUrl(),
                        sliderViewItem.getUrl()));
            }
            //加载图片
        }catch (Exception e){
            e.printStackTrace();
            addSlider(getDefultSliderViewWithUrl("小猴偷米",
                    "http://android.heraldstudio.com/sliderview",
                    "http://android.heraldstudio.com/sliderview"));
        }

        //设置轮播选项
        setPresetTransformer(SliderLayout.Transformer.Default);
        //圆点位置
        setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //描述动画
        //sliderLayout.setCustomAnimation(new DescriptionAnimation());
        //切换间隔
        setDuration(5000);
    }

    private DefaultSliderView getDefultSliderViewWithUrl(String title,String imageUrl,String url){
        DefaultSliderView sliderView = new DefaultSliderView(getContext());
        // initialize a SliderLayout
        sliderView
                .image(imageUrl)
                .setScaleType(BaseSliderView.ScaleType.Fit)
                .setOnSliderClickListener(this);
        //add your extra information
        sliderView.bundle(new Bundle());
        sliderView.getBundle()
                .putString("extra", title);
        sliderView.getBundle()
                .putString("url",url);
        return  sliderView;
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
        //点击时如果url不为空则打开图片所代表的网页
        String url = slider.getBundle().getString("url");
        if (url==null||url.equals(""))
            return;
        Uri uri = Uri.parse(url);
        Intent intent = new  Intent(Intent.ACTION_VIEW, uri);
        getContext().startActivity(intent);
    }

    public static class SliderViewItem{
        String title;
        String imageUrl;
        String url;
        public SliderViewItem(String title, String imageUrl, String url) {
            this.title = title;
            this.imageUrl = imageUrl;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getUrl() {
            return url;
        }
    }
}
