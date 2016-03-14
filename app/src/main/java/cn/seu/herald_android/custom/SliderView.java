package cn.seu.herald_android.custom;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import java.util.ArrayList;
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
            DefaultSliderView sliderView = new DefaultSliderView(getContext());
            // initialize a SliderLayout
            sliderView
                    .image(R.drawable.default_banner)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
            addSlider(sliderView);
        }

        //设置轮播选项
        setPresetTransformer(SliderLayout.Transformer.Default);
        //圆点位置
        setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //描述动画
        //sliderLayout.setCustomAnimation(new DescriptionAnimation());
        //切换间隔，暂时调成不切换
        setDuration(Long.MAX_VALUE);
    }

    private DefaultSliderView getDefultSliderViewWithUrl(String title,String imageUrl,String url){
        DefaultSliderView sliderView = new DefaultSliderView(getContext());
        // initialize a SliderLayout
        if(!imageUrl.equals("")){
            //如果图片url不为空的操作
            sliderView
                    .image(imageUrl)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
        }else{
            //为空则避免出现参数错误，返回默认的图片
            sliderView
                    .image(R.drawable.default_banner)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
        }
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
        for (ViewParent v = getParent(); v != null; v = v.getParent()) {
            if (v instanceof CustomSwipeRefreshLayout)
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
        try{
            Uri uri = Uri.parse(url);
            Intent intent = new  Intent(Intent.ACTION_VIEW, uri);
            getContext().startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
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
