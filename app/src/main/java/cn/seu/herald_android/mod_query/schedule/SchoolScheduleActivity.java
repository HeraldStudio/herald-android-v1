package cn.seu.herald_android.mod_query.schedule;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class SchoolScheduleActivity extends BaseAppCompatActivity {

    private ImageView scheduleImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        //初始化函数
        init();
        //加载校车数据
        loadImage();
    }

    private void init() {
        //沉浸式布局
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorScheduleprimary));
        enableSwipeBack();
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            finish();
        });

        scheduleImage = (ImageView) findViewById(R.id.schedule_image);
    }

    private void loadImage() {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        Picasso.with(this).load("http://heraldstudio.com/static/images/xiaoli.jpg")
                .resize(width, Integer.MAX_VALUE).centerInside().into(scheduleImage);
    }
}
