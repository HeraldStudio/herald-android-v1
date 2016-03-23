package cn.seu.herald_android.app_main;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.seismic.ShakeDetector;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import cn.seu.herald_android.R;
import cn.seu.herald_android.custom.BaseAppCompatActivity;
import cn.seu.herald_android.helper.ApiHelper;
import cn.seu.herald_android.mod_wifi.NetworkLoginHelper;
import okhttp3.Call;

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
        shakeDetector.setSensitivity(20);
        shakeDetector.start(sensorManager);
    }

    private void init() {
        //Toolbar初始化
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimary));
        setTitle(" " + getTitle().toString().trim());

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
    }

    private void checkAuth() {
        OkHttpUtils
                .post()
                .url(ApiHelper.getApiUrl(ApiHelper.API_USER))
                .addParams("uuid", getApiHelper().getUUID())
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        //错误检测
                        getApiHelper().dealApiException(e);
                    }

                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json_res = new JSONObject(response);
                            if (json_res.getInt("code") == 200) {
                                //如果返回的状态码是200则说明uuid正确，则更新各类个人信息
                                //更新首页欢迎信息和侧边栏信息
                                JSONObject json_content = json_res.getJSONObject("content");
                                getApiHelper().setAuthCache("name", json_content.getString("name"));
                                getApiHelper().setAuthCache("sex", json_content.getString("sex"));
                                getApiHelper().setAuthCache("cardnum", json_content.getString("cardnum"));
                                getApiHelper().setAuthCache("schoolnum", json_content.getString("schoolnum"));
                            } else {
                                //如果返回的状态码不是200则说明uuid不对，需要重新授权,则注销当前登录
                                showMsg("登录信息已失效，请重新登录");
                                getApiHelper().doLogout();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
        TabLayout tabLayout = (TabLayout) findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < adapter.getCount(); i++) {
            int drawable = new int[]{
                    R.drawable.ic_home_24dp,
                    R.drawable.ic_view_module_24dp,
                    R.drawable.ic_person_24dp
            }[i];
            ImageView imageView = new ImageView(this);
            imageView.setImageDrawable(ContextCompat.getDrawable(this, drawable));
            tabLayout.getTabAt(i).setCustomView(imageView);
        }
    }

    public void syncModuleSettings() {
        cardsFragment.loadTimelineView(false);
        moduleListFragment.loadModuleList();
    }
}
