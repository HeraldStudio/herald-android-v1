package cn.seu.herald_android.mod_query.schedule;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;

public class SchoolScheduleActivity extends BaseAppCompatActivity {

    private ImageView scheduleImage;

    // 由于Picasso库限制，无法通过简单的方法清空缓存，因此采用加时间参数的方式，
    // 每次刷新换一个参数，如果要加载缓存就用上一次的参数
    // TODO 此处可能导致应用缓存膨胀，可考虑换用 UniversalImageLoader
    private String cacheTime = null;

    public static final String SCHEDULE_URL = "http://heraldstudio.com/static/images/xiaoli.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        //初始化函数
        init();
        //加载校历数据，若无缓存则联网更新
        loadImage(cacheTime == null || cacheTime.equals(""));
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

        cacheTime = getCacheHelper().getCache("herald_schedule_cache_time");
        scheduleImage = (ImageView) findViewById(R.id.schedule_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sync, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            loadImage(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadImage(boolean refresh) {

        String newCacheTime = cacheTime;
        if (refresh) {
            // 生成一个新的时间参数
            newCacheTime = String.valueOf(System.currentTimeMillis());
        }

        showProgressDialog();
        int width = getResources().getDisplayMetrics().widthPixels;

        final String newCacheTimeFinal = newCacheTime;

        // 强制联网刷新
        Picasso.with(this).load(SCHEDULE_URL + "?t=" + newCacheTime)
                .resize(width, Integer.MAX_VALUE).centerInside().into(scheduleImage, new Callback() {
            @Override
            public void onSuccess() {
                // 强制联网刷新成功

                hideProgressDialog();
                // if (refresh) showSnackBar("刷新成功");

                // 保存当前缓存的时间参数，供下次使用
                getCacheHelper().setCache("herald_schedule_cache_time", newCacheTimeFinal);
            }

            @Override
            public void onError() {
                // 强制联网刷新失败

                // 加载缓存
                Picasso.with(SchoolScheduleActivity.this).load(SCHEDULE_URL + "?t=" + cacheTime)
                        .resize(width, Integer.MAX_VALUE).centerInside().into(scheduleImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        // 加载缓存成功

                        hideProgressDialog();
                        if (refresh) showSnackBar("刷新失败，已加载缓存");
                    }

                    @Override
                    public void onError() {
                        // 加载缓存失败

                        hideProgressDialog();
                        showSnackBar("刷新失败，请重试");
                    }
                });
            }
        });
    }
}
