package cn.seu.herald_android.app_module.topic;


import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import java.util.ArrayList;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.framework.BaseActivity;

/**
 * Created by corvo on 7/21/16.
 */
public class TopicActivity extends BaseActivity implements HotestFragment.onParentListener{

    AnimatedFloatingActionButton fab;
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mod_que_topic);


        fab = (AnimatedFloatingActionButton) findViewById(R.id.fab_add_comment);
        tabLayout = (TabLayout) findViewById(R.id.tab_topic);
        viewPager = (ViewPager) findViewById(R.id.viewpage_topic);

        fab.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("TopicActivity", "Button Clicked");
                revealEffectFab();
            }
        }, 300);
    }

    @Override
    protected void onResume() {
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        super.onResume();
    }

    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HotestFragment(), "ONE");
        adapter.addFragment(new HotestFragment(), "TWO");

        viewPager.setAdapter(adapter);
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
            Log.d("TopicActivity", "Refresh");
        }
        return super.onOptionsItemSelected(item);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    void revealEffectFab() {
        if (Build.VERSION.SDK_INT > 20){
            int cx = fab.getMeasuredWidth() / 2;
            int cy = fab.getMeasuredHeight() / 2;
            int finalRadius = Math.max(fab.getWidth(), fab.getHeight());

            Animator a = ViewAnimationUtils.createCircularReveal(fab, cx, cy, 0, finalRadius);
            a.setDuration(400);
            fab.setVisibility(View.VISIBLE);
            a.start();
        }
    }

    @Override
    public void deliveRecycler(RecyclerView recyclerView) {
        fab.attchToRecyclerView(recyclerView);
    }
}
