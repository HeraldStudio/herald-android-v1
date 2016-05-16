package cn.seu.herald_android.app_main;

import android.content.Intent;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.seismic.ShakeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.helper.ApiRequest;
import cn.seu.herald_android.mod_modulemanager.ModuleManageActivity;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;
import me.majiajie.pagerbottomtabstrip.Controller;
import me.majiajie.pagerbottomtabstrip.PagerBottomTabLayout;
import me.majiajie.pagerbottomtabstrip.TabLayoutMode;
import me.majiajie.pagerbottomtabstrip.listener.OnTabItemSelectListener;

public class MainActivity extends BaseAppCompatActivity {

    CardsFragment cardsFragment = new CardsFragment();

    ModuleListFragment moduleListFragment = new ModuleListFragment();

    MyInfoFragment myInfoFragment = new MyInfoFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private boolean preventShake = false;

    private ShakeDetector shakeDetector = new ShakeDetector(() -> {
        //摇一摇自动登陆
        if (getSettingsHelper().getWifiAutoLogin() && !preventShake) {
            preventShake = true;
            new NetworkLoginHelper(this, true).checkAndLogin();
            new Handler().postDelayed(() -> preventShake = false, 2000);
        }
    });

    @Override
    protected void onPause() {
        super.onPause();
        shakeDetector.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //设置摇晃力度检测阈值
        shakeDetector.setSensitivity(20);
        shakeDetector.start(sensorManager);
    }

    private void init() {
        //Toolbar初始化
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        setTitle(" " + getTitle().toString().trim());

        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimary));
        //检查个人信息
        checkAuth();

        //放置三个Tab页面
        attachFragments();

        runMeasurementDependentTask(() -> {
            //刷新卡片视图
            cardsFragment.loadTimelineView(true);

            //刷新模块视图
            moduleListFragment.loadModuleList();
        });

        ImageButton ibtn = (ImageButton)findViewById(R.id.ibtn_add);
        if (ibtn!=null)
            ibtn.setOnClickListener(o->startActivity(new Intent(MainActivity.this, ModuleManageActivity.class)));
    }

    private void checkAuth() {
        new ApiRequest(this).api(ApiHelper.API_USER).addUUID().onFinish((success, code, response) -> {
            if (success) {
                if (code == 200) try {
                    JSONObject json_res = new JSONObject(response);
                    //如果返回的状态码是200则说明uuid正确，则更新各类个人信息
                    //更新首页欢迎信息和侧边栏信息
                    JSONObject json_content = json_res.getJSONObject("content");
                    getApiHelper().setAuthCache("name", json_content.getString("name"));
                    getApiHelper().setAuthCache("sex", json_content.getString("sex"));
                    getApiHelper().setAuthCache("cardnum", json_content.getString("cardnum"));
                    getApiHelper().setAuthCache("schoolnum", json_content.getString("schoolnum"));
                    myInfoFragment.refreshUsername();
                } catch (JSONException e) {
                    getApiHelper().dealApiException(e);
                }
                else {
                    //如果返回的状态码不是200则说明uuid不对，需要重新授权,则注销当前登录
                    showSnackBar("登录信息已失效，请重新登录");
                    getApiHelper().doLogout();
                }
            }
        }).run();
    }

    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        List<Fragment> fragments = Arrays.asList(
                cardsFragment, moduleListFragment, myInfoFragment
        );

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return new String[]{"首页", "模块", "我的"}[position];
        }
    };

    private void attachFragments() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        viewPager.setOffscreenPageLimit(adapter.getCount() - 1);//三个页面都不回收，提高流畅性
        viewPager.setAdapter(adapter);
        viewPager.setPageMargin((int) (3 * getResources().getDisplayMetrics().density));


        PagerBottomTabLayout pagerBottomTabLayout = (PagerBottomTabLayout)findViewById(R.id.main_tabs);
        Controller controller =
                pagerBottomTabLayout.builder()
                .addTabItem(R.drawable.ic_home_24dp, "首页")
                .addTabItem(R.drawable.ic_view_module_24dp, "模块")
                .addTabItem(R.drawable.ic_person_24dp, "设置")
                .build();

        controller.addTabItemClickListener(new OnTabItemSelectListener() {
            @Override
            public void onSelected(int index, Object tag) {
                viewPager.setCurrentItem(index,true);
            }

            @Override
            public void onRepeatClick(int index, Object tag) {

            }
        });

        //viewpage滑动时设置底部tab
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                controller.setSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



    }

    public void syncModuleSettings() {
        cardsFragment.loadTimelineView(false);
        moduleListFragment.loadModuleList();
    }
}
