package cn.seu.herald_android.mod_query.emptyroom;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import cn.seu.herald_android.R;

public class EmptyRoomActivity extends AppCompatActivity {

    //用于选择快捷查询和完整查询的页面
    TabLayout tabLayout;
    //用于切换的ViewPager
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        init();
    }

    private void init() {
        //控件初始化
        tabLayout = (TabLayout) findViewById(R.id.tablayout_emptyroom);
        viewPager = (ViewPager) findViewById(R.id.emptyroom_viewpager);

        //适配器设置
        EmptyRoomViewPagerAdapter pagerAdapter = new EmptyRoomViewPagerAdapter(getSupportFragmentManager(), getBaseContext());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabsFromPagerAdapter(pagerAdapter);
    }

}
