package cn.seu.herald_android.custom;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

import java.util.ArrayList;

import cn.seu.herald_android.R;
import cn.seu.herald_android.app_secondary.WebModuleActivity;


public class SliderView extends SliderLayout implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private ArrayList<SliderViewItem> itemList = null;

    public SliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setupWithArrayList(ArrayList<SliderViewItem> sliderViewItemArrayList) {

        if (itemList != null && itemList.size() == sliderViewItemArrayList.size()) {
            boolean equal = true;

            for (int i = 0; i < itemList.size(); i++) {
                if (!itemList.get(i).equals(sliderViewItemArrayList.get(i))) equal = false;
            }

            if (equal) return;
        }
        itemList = sliderViewItemArrayList;

        // 利用 ArrayList 进行初始化
        try {
            removeAllSliders();
            for (SliderViewItem sliderViewItem : sliderViewItemArrayList) {
                addSlider(getDefaultSliderViewWithUrl(
                        sliderViewItem.getTitle(),
                        sliderViewItem.getImageUrl(),
                        sliderViewItem.getUrl()));
            }
            // 加载图片
        } catch (Exception e) {
            e.printStackTrace();
            DefaultSliderView sliderView = new DefaultSliderView(getContext());
            // initialize a SliderLayout
            sliderView
                    .image(R.drawable.default_herald)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
            addSlider(sliderView);
        }

        // 设置轮播选项
        setPresetTransformer(SliderLayout.Transformer.Default);
        // 圆点位置
        setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        // 描述动画
        // sliderLayout.setCustomAnimation(new DescriptionAnimation());
        // 切换间隔
        setDuration(5000);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        stopAutoCycle();
    }

    private DefaultSliderView getDefaultSliderViewWithUrl(String title, String imageUrl, String url) {
        DefaultSliderView sliderView = new DefaultSliderView(getContext());
        // initialize a SliderLayout
        if (!imageUrl.equals("")) {
            // 如果图片url不为空的操作
            sliderView
                    .image(imageUrl)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
        } else {
            // 为空则避免出现参数错误，返回默认的图片
            sliderView
                    .image(R.drawable.default_herald)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                    .setOnSliderClickListener(this);
        }
        // add your extra information
        sliderView.bundle(new Bundle());
        sliderView.getBundle()
                .putString("title", title);
        sliderView.getBundle()
                .putString("url", url);
        return sliderView;
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
        // 点击时如果url不为空则打开图片所代表的网页
        String url = slider.getBundle().getString("url");
        if (url != null && !url.equals("")) {
            String title = slider.getBundle().getString("title");
            WebModuleActivity.startWebModuleActivity(title, url, R.style.WebShowTheme);
        }
    }

    public static class SliderViewItem {
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SliderViewItem)) return false;
            SliderViewItem item = (SliderViewItem) o;
            return imageUrl.equals(item.imageUrl) && title.equals(item.title) && url.equals(item.url);
        }
    }
}
